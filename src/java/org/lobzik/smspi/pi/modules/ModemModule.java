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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import org.lobzik.smspi.pi.event.Event;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.entity.Parameter;
import org.lobzik.smspi.pi.MessageStatus;
import org.lobzik.tools.sms.UCS2;

/**
 *
 * @author lobzik
 */
public class ModemModule extends Thread implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static ModemModule instance = null;
    private static Connection conn = null;
    private static final int MODEM_TIMEOUT = 10000;
    private static final int USSD_TIMEOUT = 50000;
    private static Logger log = null;

    private static boolean run = true;
    private static String smscNumber = "+79262909090";



    private static String lastRecieved = "";

    private static CommPort commPort = null;
    private static final ModemSerialReader serialReader = new ModemSerialReader();
    private static final ModemSerialWriter serialWriter = new ModemSerialWriter();
    private static final int REPLIES_BUFFER_SIZE = 100;
    private static final Queue<String> recievedLines = new ConcurrentLinkedQueue();
    //private static final int CHECK_BALANCE_EVERY = 10;

    private static int modemOkRepliesCount = 0;
    private static int modemWriteErrorCount = 0;
    private static String suggestedResponse = null;

    private static final AtomicBoolean modemBusy = new AtomicBoolean(false);

    private static final AtomicBoolean doCheckBalance = new AtomicBoolean(true);

    public static final String regex = "[0-9\\.]+ *р\\.";//будет в настройках
    public static final String replacer = "р.";//будет в настройках

    private ModemModule() { //singleton
    }

    public static ModemModule getInstance() {

        if (instance == null) {
            instance = new ModemModule(); //lazy init
            log = Logger.getLogger(instance.MODULE_NAME);

            Appender appender = ConnJDBCAppender.getAppenderInstance(AppData.dataSource, instance.MODULE_NAME);
            log.addAppender(appender);

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
        EventManager.subscribeForEventType(this, Event.Type.SYSTEM_EVENT);

        try {

            conn = DBTools.openConnection(BoxCommonData.dataSourceName);

            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(BoxCommonData.MODEM_INFO_PORT);

            commPort = portIdentifier.open(this.getClass().getName(), MODEM_TIMEOUT);

            SerialPort serialPort = (SerialPort) commPort;
            serialPort.setSerialPortParams(115200,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            serialReader.setInputStream(serialPort.getInputStream());
            serialReader.start();
            serialWriter.setOutputStream(serialPort.getOutputStream());
            serialWriter.start();
            log.debug("Configuring modem");

            waitForCommand("ATE0\r");
            waitForCommand("AT+CMGF=0\r");

            waitForCommand("AT+COPS=3,0\r");
            waitForCommand("AT+COPS?\r");
            String operator = parseCOPSReply(recievedLines);
            HashMap opData = new HashMap();
            opData.put("name", operator);
            Event event = new Event("operator_detected", opData, Event.Type.SYSTEM_EVENT);

            AppData.eventManager.newEvent(event);

            log.debug("Operator is " + operator);

            int operatorparamId = AppData.parametersStorage.resolveAlias("MODEM_OPERATOR");
            if (operatorparamId > 0) {
                Parameter p = AppData.parametersStorage.getParameter(operatorparamId);
                Measurement m = new Measurement(p, operator);

                HashMap eventData = new HashMap();
                eventData.put("parameter", p);
                eventData.put("measurement", m);
                event = new Event("operator_detected", eventData, Event.Type.PARAMETER_UPDATED);

                AppData.eventManager.newEvent(event);

            }

            recievedLines.clear();
            waitForCommand("AT+CREG=2\r");
            waitForCommand("AT+CREG?\r");
            HashMap cellId = parseCREGReply(recievedLines);
            event = new Event("cellid_detected", cellId, Event.Type.SYSTEM_EVENT);

            AppData.eventManager.newEvent(event);

            log.debug("Cell ID is " + cellId);
            int cellIdparamId = AppData.parametersStorage.resolveAlias("MODEM_CELLID");
            if (cellIdparamId > 0) {
                Parameter p = AppData.parametersStorage.getParameter(cellIdparamId);
                Measurement m = new Measurement(p, cellId.toString());

                HashMap eventData = new HashMap();
                eventData.put("parameter", p);
                eventData.put("measurement", m);
                event = new Event("Cell ID updated", eventData, Event.Type.PARAMETER_UPDATED);

                AppData.eventManager.newEvent(event);

            }

            waitForCommand("AT+CSCA?\r");
            smscNumber = parseCSCAReply(recievedLines);
            log.debug("SMSC is " + smscNumber);

            String myNumber = checkNumber();
            int myNumberparamId = AppData.parametersStorage.resolveAlias("MODEM_NUMBER");
            if (myNumberparamId > 0 && myNumber.length() == 11) {
                Parameter p = AppData.parametersStorage.getParameter(myNumberparamId);
                Measurement m = new Measurement(p, myNumber);

                HashMap eventData = new HashMap();
                eventData.put("parameter", p);
                eventData.put("measurement", m);
                event = new Event("Mobile number updated", eventData, Event.Type.PARAMETER_UPDATED);

                AppData.eventManager.newEvent(event);

            }
            int iter = -1;
            while (run) {
                try {
                    iter++;
                    String sSQL = "select * from sms_outbox where status=" + MessageStatus.STATUS_NEW + " or status=" + MessageStatus.STATUS_ERROR_SENDING;

                    List<HashMap> smsToSendList = DBSelect.getRows(sSQL, conn);

                    while (!smsToSendList.isEmpty()) {
                        HashMap smsToSend = smsToSendList.remove(0);
                        log.info("Processing SMS id " + smsToSend.get("id"));
                        int tries = Tools.parseInt(smsToSend.get("tries_cnt"), 0);
                        tries++;
                        if (tries >= BoxSettingsAPI.getInt("MaxAttemptsToSend")) {
                            log.error("Sending attempts exceeded!");
                            smsToSend.put("status", MessageStatus.STATUS_ERROR_ATTEMPTS_EXCEEDED);
                            smsToSend.put("tries_cnt", tries);
                            DBTools.updateRow("sms_outbox", smsToSend, conn);
                            continue;
                        }
                        Date msgDate = (Date) smsToSend.get("date");
                        Date validBefore = (Date) smsToSend.get("valid_before");

                        if ((validBefore != null && System.currentTimeMillis() > validBefore.getTime())
                                || (System.currentTimeMillis() > BoxSettingsAPI.getInt("OutgoingMessageMaxAge") * 1000l + msgDate.getTime())) {
                            log.error("Message " + smsToSend.get("id") + " is too old! :(");
                            smsToSend.put("status", MessageStatus.STATUS_ERROR_TOO_OLD);
                            smsToSend.put("tries_cnt", tries);
                            DBTools.updateRow("sms_outbox", smsToSend, conn);
                            continue;
                        }

                        smsToSend.put("status", MessageStatus.STATUS_SENDING);
                        smsToSend.put("tries_cnt", tries);
                        DBTools.updateRow("sms_outbox", smsToSend, conn);
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

                        waitForCommand("AT+CMGS=" + j + "\r");

                        waitForCommand(pdu + "\032");

                        if (lastRecieved.equalsIgnoreCase("OK")) {
                            smsToSend.put("status", MessageStatus.STATUS_SENT);
                            smsToSend.put("date_sent", new Date());
                            log.info("Successfully sent");
                            DBTools.updateRow("sms_outbox", smsToSend, conn);
                        } else {
                            smsToSend.put("status", MessageStatus.STATUS_ERROR_SENDING);

                            DBTools.updateRow("sms_outbox", smsToSend, conn);
                            log.error("Error sending: " + lastRecieved);
                        }
                    }

                    recievedLines.clear();

                    if (doCheckBalance.get()) {//iter % CHECK_BALANCE_EVERY == 0) {
                        doCheckBalance.set(false);
                        Double myBalance = checkBalance();
                        myNumberparamId = AppData.parametersStorage.resolveAlias("MODEM_BALANCE");
                        if (myNumberparamId > 0 && myBalance >= 0) {
                            Parameter p = AppData.parametersStorage.getParameter(myNumberparamId);
                            Measurement m = new Measurement(p, myBalance);

                            HashMap eventData = new HashMap();
                            eventData.put("parameter", p);
                            eventData.put("measurement", m);
                            AppData.eventManager.newEvent(new Event("Balance updated", eventData, Event.Type.PARAMETER_UPDATED));
                        }
                    }

                    waitForCommand("AT+CSQ\r");
                    int db = parseCSQReply(recievedLines);
                    log.debug("RSSI = " + db + " dBm");

                    int paramId = AppData.parametersStorage.resolveAlias("MODEM_RSSI");
                    if (paramId > 0) {
                        Parameter p = AppData.parametersStorage.getParameter(paramId);
                        Measurement m = new Measurement(p, Tools.parseDouble(db + "", null));

                        HashMap eventData = new HashMap();
                        eventData.put("parameter", p);
                        eventData.put("measurement", m);
                        event = new Event("RSSI updated", eventData, Event.Type.PARAMETER_UPDATED);

                        AppData.eventManager.newEvent(event);

                    }

                    HashMap rssiData = new HashMap();
                    rssiData.put("RSSI", db);
                    event = new Event("modem_mode_updated", rssiData, Event.Type.SYSTEM_EVENT);
                    AppData.eventManager.newEvent(event);

                    recievedLines.clear();
                    log.debug("Polling for new messages");
                    waitForCommand("AT+CMGL=4\r");
                    if (lastRecieved.equals("OK")) {
                        int cnt = recieveMessages(recievedLines);
                        if (cnt > 0) {
                            log.info("Recieved " + cnt + " messages, clearing modem inbox");
                            waitForCommand("AT+CMGD=0,4\r");
                        }
                    }

                    synchronized (this) {
                        try {
                            wait();//wait for timer 
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

    private boolean waitForUSSD() {
        modemBusy.set(true);
        suggestedResponse = "+CUSD:";
        long waitStart = System.currentTimeMillis();
        synchronized (this) {
            try {
                wait(USSD_TIMEOUT);
            } catch (InterruptedException ie) {
            }
        }
        if (System.currentTimeMillis() - waitStart >= MODEM_TIMEOUT) {
            log.error("USSD TIMEOUT");
            modemBusy.set(false);
            return false;

        }
        modemBusy.set(false);
        return true;
    }

    private void waitForCommand(String command) throws Exception {
        waitForCommand(command, MODEM_TIMEOUT);
    }

    private void waitForCommand(String command, int timeout) throws Exception {

        modemBusy.set(true);
        suggestedResponse = null;
        log.debug("Sending " + command);
        long waitStart = System.currentTimeMillis();
        if (command != null) {
            serialWriter.send(command);
        }

        synchronized (this) {
            try {
                wait(timeout);
            } catch (InterruptedException ie) {
            }
        }
        if (System.currentTimeMillis() - waitStart >= MODEM_TIMEOUT) {
            log.error("MODEM TIMEOUT");
        }
        modemBusy.set(false);
    }

    @Override
    public void handleEvent(Event e) {

        switch (e.getType()) {
            case TIMER_EVENT:
                switch (e.name) {
                    case "internal_sensors_poll": {
                        if (!modemBusy.get()) {
                            synchronized (this) {
                                notify();
                            }
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

                    case "check_balance":
                        doCheckBalance.set(true);

                        break;
                }
                break;

            case USER_ACTION:
                switch (e.name) {
                    case "send_sms":
                        log.debug("Sending sms ");
                        sendMessage((String) e.data.get("recipient"), (String) e.data.get("message"));
                        break;

                    case "modem_command":
                        log.debug("User Modem Command ");
                        try {
                            waitForCommand((String) e.data.get("modem_command"));
                        } catch (Exception ex) {
                            log.error(ex.getMessage());
                        }
                        break;

                    case "check_outbox":
                        if (!modemBusy.get()) {
                            synchronized (this) {
                                notify();
                            }
                        }
                }
                break;

            case SYSTEM_EVENT:
                if (e.name.equals("check_number")) {
                    checkNumber();
                }
                if (e.name.equals("check_balance")) {
                    checkBalance();
                }
                break;

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
        line = line.trim();
        if ((suggestedResponse == null && (line.equals("OK") || line.contains("ERROR") || line.equals(">") || line.contains("+CMTI:")))
                || (suggestedResponse != null && line.contains(suggestedResponse))) {
            if (modemOkRepliesCount < Integer.MAX_VALUE) {
                modemOkRepliesCount++;
                modemWriteErrorCount = 0;
            }
            synchronized (this) {
                notify();
            }
        }
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
                    dbMessage.put("status", MessageStatus.STATUS_NEW);
                    int id = DBTools.insertRow("sms_inbox", dbMessage, conn);
                    log.info("Recieved SMS from " + message.getOriginator() + " id = " + id);
                    HashMap eventData = new HashMap();
                    eventData.put("sender", message.getOriginator());
                    eventData.put("text", message.getNativeText());
                    Event e = new Event("sms_recieved", eventData, Event.Type.USER_ACTION);

                    AppData.eventManager.newEvent(e);

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
        if (!recipient.matches("\\+[0-9]{7,15}")) {
            log.error("Invalid recipient (must be like +71234567890)");
            return -1;
        }
        HashMap message = new HashMap();
        message.put("message", text);
        message.put("recipient", recipient); //TODO проверка на формат телефона!
        message.put("date", new Date());
        message.put("status", MessageStatus.STATUS_NEW);
        int msgId = -1;
        try {
            msgId = DBTools.insertRow("sms_outbox", message, conn);
            if (!modemBusy.get()) {
                synchronized (this) {
                    notify();
                }
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

        public ModemSerialReader() {
        }

        public void setInputStream(InputStream is) {
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

    public static class ModemSerialWriter extends Thread {

        OutputStream os;
        private static final Queue<String> linesToSend = new ConcurrentLinkedQueue();

        public ModemSerialWriter() {
        }

        public void setOutputStream(OutputStream os) {
            this.os = os;
        }

        public void send(String command) {
            linesToSend.add(command);
            synchronized (this) {
                notify();
            }
        }

        @Override
        public void run() {
            log.debug("Modem writer started");
            OutputStreamWriter osw = new OutputStreamWriter(os);
            while (run) {
                if (linesToSend.size() > 0) {
                    String command = linesToSend.poll();
                    try {
                        osw.write(command);
                        osw.flush();
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
                        log.error(ioe.getMessage());
                    }
                } else {
                    synchronized (this) {
                        try {
                            wait();
                        } catch (InterruptedException ie) {

                        }
                    }
                }
            }

        }
    }

    public String checkNumber() {
        String myNumber = "";
        try {
            modemBusy.set(true);
            log.debug("Checking number");
            waitForCommand("AT^USSDMODE=0\r");
            recievedLines.clear();
            waitForCommand("AT+CUSD=1,\"*205#\",15\r"); //MEGAFON-specific!!
            if (waitForUSSD()) {
                myNumber = parseUSSDUCS2numReply(recievedLines);
                myNumber = myNumber.replaceAll(" ", "").replaceAll("-", "");
                Matcher m = Pattern.compile("\\d{11}").matcher(myNumber);
                while (m.find()) {
                    myNumber = m.group();
                }

                log.debug("Recieved number " + myNumber);
            }
        } catch (Exception eee) {
        }
        modemBusy.set(false);
        return myNumber;

    }

    public Double checkBalance() {
        Double balance = -1D;
        try {
            log.debug("Checking balance");
            modemBusy.set(true);

            waitForCommand("AT^USSDMODE=0\r");
            recievedLines.clear();
            waitForCommand("AT+CUSD=1,\"*100#\",15\r"); //MEGAFON-specific!!

            if (waitForUSSD()) {
                String balanceString = parseUSSDUCS2numReply(recievedLines);

                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(balanceString);
                if (matcher.find()) {
                    String sVal = matcher.group(0);
                    sVal = sVal.replaceAll(replacer, "").trim();
                    balance = Tools.parseDouble(sVal, 0);
                }

                log.debug("Recieved balance " + balanceString);
            }
        } catch (Exception eee) {
        }

        modemBusy.set(false);

        return balance;

    }

    private String parseUSSDUCS2numReply(Queue<String> replyLines) {
        String number = "";
        while (!replyLines.isEmpty()) {

            String pdu = replyLines.poll();
            if (pdu.contains("+CUSD: ")) {
                try {
                    pdu = pdu.substring(pdu.indexOf("\"") + 1);
                    pdu = pdu.substring(0, pdu.indexOf("\""));

                    if (pdu.length() > 16) {
                        UCS2 ucs = new UCS2();
                        number = ucs.decode(pdu);

                    }
                    break;
                } catch (Exception e) {
                    log.debug("err " + e.toString());
                }
            }
        }
        return number;
    }

}
