/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi.modules;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.lobzik.smspi.pi.event.Event;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Appender;

import org.apache.log4j.Logger;
import org.lobzik.smspi.pi.AppData;
import org.lobzik.smspi.pi.BoxCommonData;
import org.lobzik.smspi.pi.BoxSettingsAPI;
import org.lobzik.smspi.pi.ConnJDBCAppender;
import org.lobzik.smspi.pi.event.EventManager;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;
import org.lobzik.tools.sms.CIncomingMessage;
import org.lobzik.tools.sms.CMessage;
import org.lobzik.tools.sms.COutgoingMessage;

/**
 *
 * @author lobzik
 */
public class ModemModule extends Thread implements Module {

    public static boolean test = false;

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static ModemModule instance = null;
    private static Connection conn = null;
    private static final int MODEM_TIMEOUT = 10000;

    private static Logger log = null;

    private static boolean run = true;
    private static String smscNumber = "+79262909090";

    public static final int STATUS_NEW = 0;
    public static final int STATUS_SENT = 1;
    public static final int STATUS_READ = 2;
    public static final int STATUS_ERROR = -1;

    private static String lastRecieved = "";

    private static CommPort commPort = null;
    private static ModemSerialReader serialReader = null;
    private static final int REPLIES_BUFFER_SIZE = 100;
    private static final Queue<String> recievedLines = new ConcurrentLinkedQueue();

    private static int modemOkRepliesCount = 0;
    private static int modemWriteErrorCount = 0;

    private ModemModule() { //singleton
    }

    public static ModemModule getInstance() {

        if (instance == null) {
            instance = new ModemModule(); //lazy init
            log = Logger.getLogger(instance.MODULE_NAME);
            if (!test) {
                Appender appender = ConnJDBCAppender.getAppenderInstance(AppData.dataSource, instance.MODULE_NAME);
                log.addAppender(appender);
            }
        }
        return instance;
    }

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void run() {
        setName(this.getClass().getSimpleName() + "-Thread");
        log.info("Starting " + getName() + " on " + BoxCommonData.MODEM_INFO_PORT);
        EventManager.subscribeForEventType(this, Event.Type.TIMER_EVENT);
        EventManager.subscribeForEventType(this, Event.Type.USER_ACTION);
        try {
            if (test) {
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/hs?useUnicode=true&amp;characterEncoding=utf8&user=hsuser&password=hspass");
            } else {
                conn = DBTools.openConnection(BoxCommonData.dataSourceName);
            }
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(BoxCommonData.MODEM_INFO_PORT);

            commPort = portIdentifier.open(this.getClass().getName(), MODEM_TIMEOUT);

            SerialPort serialPort = (SerialPort) commPort;
            serialPort.setSerialPortParams(115200,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            OutputStreamWriter outWriter = new OutputStreamWriter(serialPort.getOutputStream());
            serialReader = new ModemSerialReader(serialPort.getInputStream());
            serialReader.start();
            log.debug("Configuring modem");
            waitForCommand("ATE0\r", outWriter);
            waitForCommand("AT+CMGF=0\r", outWriter);
            //TODO other init
/*
            log.debug("Checking number");
            waitForCommand("AT^USSDMODE=0\r", outWriter);
            recievedLines.clear();
            waitForCommand("AT+CUSD=1,\"*205#\",15\r", outWriter); //MEGAFON-specific!!
            waitForCommand(null, outWriter, 4 * MODEM_TIMEOUT);
            String number = parseUSSDnumReply(recievedLines);
            log.debug("Recieved number " + number);
             */

            waitForCommand("AT+COPS=3,0\r", outWriter);
            waitForCommand("AT+COPS?\r", outWriter);
            String operator = parseCOPSReply(recievedLines);
            HashMap opData = new HashMap();
            opData.put("name", operator);
            Event event = new Event("operator_detected", opData, Event.Type.SYSTEM_EVENT);
            if (!test) {
                AppData.eventManager.newEvent(event);
            }
            log.debug("Operator is " + operator);

            recievedLines.clear();
            waitForCommand("AT+CREG=2\r", outWriter);
            waitForCommand("AT+CREG?\r", outWriter);
            HashMap cellId = parseCREGReply(recievedLines);
            event = new Event("cellid_detected", cellId, Event.Type.SYSTEM_EVENT);
            if (!test) {
                AppData.eventManager.newEvent(event);
            }
            log.debug("Cell ID is " + cellId);

            waitForCommand("AT+CSCA?\r", outWriter);
            smscNumber = parseCSCAReply(recievedLines);
            log.debug("SMSC is " + smscNumber);

            while (run) {
                try {

                    String sSQL = "select * from sms_outbox where status=" + STATUS_NEW;

                    List<HashMap> smsToSendList = DBSelect.getRows(sSQL, conn);
                    while (!smsToSendList.isEmpty()) {
                        HashMap smsToSend = smsToSendList.remove(0);
                        log.info("Sending SMS id " + smsToSend.get("id"));
                        smsToSend.put("status", STATUS_ERROR);
                        DBTools.updateRow("sms_outbox", smsToSend, conn);//сразу ему ставим статус с ошибкой, чтобы если что не гонялось по кругу
                        COutgoingMessage outMsg = new COutgoingMessage();

                        outMsg.setMessageEncoding(CMessage.MESSAGE_ENCODING_UNICODE);
                        outMsg.setRecipient((String) smsToSend.get("recipient"));
                        outMsg.setText((String) smsToSend.get("message"));
                        String pdu = outMsg.getPDU(smscNumber);
                        int j = pdu.length();
                        j /= 2;
                        if (smscNumber.length() == 0) {
                            j--;
                        } else {
                            j -= ((smscNumber.length() - 1) / 2);
                            j -= 2;
                        }
                        j--;
                        recievedLines.clear();

                        waitForCommand("AT+CMGS=" + j + "\r", outWriter);

                        waitForCommand(pdu + "\032", outWriter);

                        if (lastRecieved.equalsIgnoreCase("OK")) {
                            smsToSend.put("status", STATUS_SENT);
                            log.info("Successfully sent");
                            DBTools.updateRow("sms_outbox", smsToSend, conn);
                        } else {

                            log.error("Error sending: " + lastRecieved);
                        }
                    }

                    recievedLines.clear();
                    waitForCommand("AT+CSQ\r", outWriter);
                    int db = parseCSQReply(recievedLines);
                    log.debug("RSSI = " + db + " dBm");
                    HashMap rssiData = new HashMap();
                    rssiData.put("RSSI", db);
                    event = new Event("modem_mode_updated", rssiData, Event.Type.SYSTEM_EVENT);
                    AppData.eventManager.newEvent(event);

                    recievedLines.clear();
                    log.debug("Polling for new messages");
                    waitForCommand("AT+CMGL=4\r", outWriter);
                    if (lastRecieved.equals("OK")) {
                        int cnt = recieveMessages(recievedLines);
                        if (cnt > 0) {
                            log.info("Recieved " + cnt + " messages, clearing modem inbox");
                            waitForCommand("AT+CMGD=0,4\r", outWriter);
                        }
                    }
                    synchronized (this) {
                        try {
                            if (test) {
                                wait(10000);
                            } else {
                                wait();//wait for timer 
                            }
                        } catch (InterruptedException ie) {
                        }
                    }

                } catch (Exception e) {
                    log.error(e.getMessage());
                    synchronized (this) {
                        try {
                            wait(MODEM_TIMEOUT);
                        } catch (InterruptedException ie) {
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void waitForCommand(String command, OutputStreamWriter outWriter) throws Exception {
        waitForCommand(command, outWriter, MODEM_TIMEOUT);
    }

    private void waitForCommand(String command, OutputStreamWriter outWriter, int timeout) throws Exception {
        log.debug("Sending " + command);
        if (command != null) {
            try {
                outWriter.write(command);
                outWriter.flush();
            } catch (IOException ioe) {
                modemWriteErrorCount++;
                if (modemOkRepliesCount > 10 && modemWriteErrorCount > 3) { //если нормально работал и перестал - значит хана
                    modemOkRepliesCount = 0;
                    String message = "Modem port lost!";
                    log.fatal(message);
                    HashMap cause = new HashMap();
                    cause.put("cause", message);
                    Event reboot = new Event("modem_and_system_reboot", cause, Event.Type.SYSTEM_EVENT);
                    AppData.eventManager.newEvent(reboot);
                }
                throw ioe;
            }
        }
        long waitStart = System.currentTimeMillis();
        synchronized (this) {
            try {
                wait(timeout);
            } catch (InterruptedException ie) {
            }
        }
        if (System.currentTimeMillis() - waitStart >= MODEM_TIMEOUT) {
            log.error("MODEM TIMEOUT");
        }
    }

    @Override
    public void handleEvent(Event e) {

        switch (e.getType()) {
            case TIMER_EVENT:
                switch (e.name) {
                    case "internal_sensors_poll": {
                        synchronized (this) {
                            notify(); //TODO вообще паршиво, т.к. тред может ждать ответа от модема, а его пробудят невовремя - нужна синхронизация по иному объекту
                        }
                    }
                    break;

                    case "send_test_sms":
                        String testRecipient = BoxSettingsAPI.get("SMSTestRecipient");
                        String testText = BoxSettingsAPI.get("SMSTestText");
                        if (testRecipient != null && testRecipient.length() > 5 && testText != null && testText.length() > 0) {
                            log.info("Sending TEST SMS ");
                            testText = testText.replace("%DATE%", System.currentTimeMillis() + " ms");
                            sendMessage(testRecipient, testText);
                            
                        }

                        break;

                }
                break;

            case USER_ACTION:
                if (e.name.equals("send_sms")) {
                    log.debug("Sending sms ");
                    sendMessage((String) e.data.get("recipient"), (String) e.data.get("message"));

                }
                break;

            /*           case BEHAVIOR_EVENT:
                if (e.name.equals("send_sms")) {
                    boolean doSendSms = Tools.parseBoolean(BoxSettingsAPI.get("SMSNotifications"), false);
//                    Notification n = (Notification) e.data.get("Notification");
                    if (n != null && doSendSms) {
                        for (String login : AppData.usersPublicKeysCache.getLogins()) {
                            String number = login.replace("(", "").replace(")", "").replaceAll("-", "");
                            log.debug("Sending sms ");
                            sendMessage(number, n.text);
                        }
                    }
                }
                break;*/
        }
    }

    public static void finish() {
        DBTools.closeConnection(conn);
    }

    public void lineRecieved(String line) {
        if (line.length() == 0) {
            return;
        }
        lastRecieved = line;
        recievedLines.add(line);

        while (recievedLines.size() > REPLIES_BUFFER_SIZE) {
            recievedLines.poll();
        }

        if (line.equals("OK") || line.contains("ERROR") || line.equals(">") || line.contains("+CMTI:") || line.contains("+CUSD:")) {
            if (modemOkRepliesCount < Integer.MAX_VALUE) {
                modemOkRepliesCount++;
                modemWriteErrorCount = 0;
            }
            synchronized (this) {
                notify();
            }
        }
    }

    private String parseUSSDnumReply(Queue<String> replyLines) {
        String number = "";
        while (!replyLines.isEmpty()) {

            String pdu = replyLines.poll();
            if (pdu.contains("+CUSD")) {
                try {
                    pdu = pdu.substring(pdu.indexOf("\"") + 1);
                    pdu = pdu.substring(0, pdu.indexOf("\""));

                    CIncomingMessage message = new CIncomingMessage(pdu, 1);
                    number = message.getText();
                    break;
                } catch (Exception e) {
                }
            }
        }
        return number;
    }

    private String parseCOPSReply(Queue<String> replyLines) {
        String operator = "";
        while (!replyLines.isEmpty()) {

            String s = replyLines.poll();
            if (s.contains("+COPS:")) {
                try {
                    s = s.substring(s.indexOf("\"") + 1);
                    s = s.substring(0, s.indexOf("\""));
                    operator = s;
                    break;
                } catch (Exception e) {
                }
            }
        }
        return operator;
    }

    private String parseCSCAReply(Queue<String> replyLines) {
        String smsc = smscNumber;
        while (!replyLines.isEmpty()) {

            String s = replyLines.poll();
            if (s.contains("+CSCA:")) {
                try {
                    s = s.substring(s.indexOf("\"") + 1);
                    s = s.substring(0, s.indexOf("\""));
                    smsc = s;
                    break;
                } catch (Exception e) {
                }
            }
        }
        return smsc;
    }

    private HashMap parseCREGReply(Queue<String> replyLines) {

        HashMap cellId = new HashMap();
        while (!replyLines.isEmpty()) {

            String s = replyLines.poll();
            if (s.contains("+CREG:")) {
                try {
                    s = s.substring(s.indexOf("\"") + 1);
                    String lac = s.substring(0, s.indexOf("\""));
                    cellId.put("LAC", lac);
                    s = s.substring(s.indexOf("\",\"") + 3);
                    String cid = s.substring(0, s.indexOf("\""));
                    cellId.put("CID", cid);
                    break;
                } catch (Exception e) {
                }
            }
        }
        return cellId;
    }

    private int parseCSQReply(Queue<String> replyLines) {
        int db = -113;
        while (!replyLines.isEmpty()) {

            String s = replyLines.poll();
            if (s.contains("+CSQ:")) {
                try {
                    s = s.substring(s.indexOf("Q: ") + 3);
                    s = s.substring(0, s.indexOf(","));
                    db += 2 * (Tools.parseInt(s, 0));
                    break;
                } catch (Exception e) {
                }
            }
        }
        return db;
    }

    private int recieveMessages(Queue<String> replyLines) {
        boolean incoming = false;
        int cnt = 0;
        for (String replyLine : replyLines) {
            if (incoming) {
                try {
                    cnt++;
                    CIncomingMessage message = new CIncomingMessage(replyLine, 1);
                    HashMap dbMessage = new HashMap();
                    dbMessage.put("message", message.getNativeText());
                    dbMessage.put("date", message.getDate());
                    dbMessage.put("sender", message.getOriginator());
                    dbMessage.put("status", STATUS_NEW);
                    int id = DBTools.insertRow("sms_inbox", dbMessage, conn);
                    log.info("Recieved SMS from " + message.getOriginator() + " id = " + id);
                    HashMap eventData = new HashMap();
                    eventData.put("sender", message.getOriginator());
                    eventData.put("text", message.getNativeText());
                    Event e = new Event("sms_recieved", eventData, Event.Type.USER_ACTION);
                    if (!test) {
                        AppData.eventManager.newEvent(e);
                    }

                } catch (Exception e) {
                    log.error("Error while getting SMS: " + e.getMessage());
                }
            }
            incoming = false;
            if (replyLine.contains("+CMGL:") || replyLine.contains("+CMGR:")) {
                incoming = true;
            }
        }
        return cnt;
    }

    private int sendMessage(String recipient, String text) {
        HashMap message = new HashMap();
        message.put("message", text);
        message.put("recipient", recipient); //TODO проверка на формат телефона!
        message.put("date", new Date());
        message.put("status", STATUS_NEW);
        int msgId = -1;
        try {
            msgId = DBTools.insertRow("sms_outbox", message, conn);
            synchronized (this) {
                notify();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return msgId;
    }

    public void exit() {
        run = false;
        synchronized (this) {
            notify();
        }
    }

    public static class ModemSerialReader extends Thread {

        InputStream is;
        int maxLineLength = 1000;

        public ModemSerialReader(InputStream is) {
            this.is = is;
        }

        @Override
        public void run() {
            log.debug("Modem reader started");
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                while (run) {
                    //String response = br.readLine();
                    StringBuilder sb = new StringBuilder();
                    while (run) {
                        int b = br.read();
                        sb.append((char) b);
                        if (b == 13 || b == 10 || b == 62) {
                            break;
                        }

                    }
                    String response = sb.toString();
                    response = response.trim();
                    if (response.length() > 0) {
                        if (response.contains("ERROR")) {
                            log.error("Modem response:" + response);
                        } else {
                            log.debug("Modem response:" + response);
                        }

                        instance.lineRecieved(response);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
