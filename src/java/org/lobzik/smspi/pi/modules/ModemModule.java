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
import java.io.UnsupportedEncodingException;
import org.lobzik.smspi.pi.event.Event;

import java.sql.Connection;
import java.sql.DriverManager;
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
    public static final int STATUS_ERROR_SENDING = -1;
    public static final int STATUS_ERROR_TOO_OLD = -2;
    public static final int STATUS_ERROR_ATTEMPTS_EXCEEDED = -3;
    public static final int STATUS_SENDING = 3;

    private static String lastRecieved = "";

    private static CommPort commPort = null;
    private static ModemSerialReader serialReader = null;
    private static OutputStreamWriter outWriter = null;
    private static final int REPLIES_BUFFER_SIZE = 100;
    private static final Queue<String> recievedLines = new ConcurrentLinkedQueue();

    private static int modemOkRepliesCount = 0;
    private static int modemWriteErrorCount = 0;

    private static final AtomicBoolean modemBusy = new AtomicBoolean(false);

    public static final String regex = "[0-9\\.]+ *р\\.";//будет в настройках
    public static final String replacer = "р.";//будет в настройках
    
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
        EventManager.subscribeForEventType(this, Event.Type.SYSTEM_EVENT);

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

            outWriter = new OutputStreamWriter(serialPort.getOutputStream());
            serialReader = new ModemSerialReader(serialPort.getInputStream());
            serialReader.start();
            log.debug("Configuring modem");
            modemBusy.set(true);
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
            
            int operatorparamId = AppData.parametersStorage.resolveAlias("MODEM_OPERATOR");
            if (operatorparamId > 0) {
                Parameter p = AppData.parametersStorage.getParameter(operatorparamId);
                Measurement m = new Measurement(p, operator);
                if (!test) {
                    HashMap eventData = new HashMap();
                    eventData.put("parameter", p);
                    eventData.put("measurement", m);
                    event = new Event("operator_detected", eventData, Event.Type.PARAMETER_UPDATED);

                    AppData.eventManager.newEvent(event);
                }
            }
            
            
            recievedLines.clear();
            waitForCommand("AT+CREG=2\r", outWriter);
            waitForCommand("AT+CREG?\r", outWriter);
            HashMap cellId = parseCREGReply(recievedLines);
            event = new Event("cellid_detected", cellId, Event.Type.SYSTEM_EVENT);
            if (!test) {
                AppData.eventManager.newEvent(event);
            }
            log.debug("Cell ID is " + cellId);
            int cellIdparamId = AppData.parametersStorage.resolveAlias("MODEM_CELLID");
            if (cellIdparamId > 0) {
                Parameter p = AppData.parametersStorage.getParameter(cellIdparamId);
                Measurement m = new Measurement(p, cellId.toString());
                if (!test) {
                    HashMap eventData = new HashMap();
                    eventData.put("parameter", p);
                    eventData.put("measurement", m);
                    event = new Event("Cell ID updated", eventData, Event.Type.PARAMETER_UPDATED);

                    AppData.eventManager.newEvent(event);
                }
            }
                    
            waitForCommand("AT+CSCA?\r", outWriter);
            smscNumber = parseCSCAReply(recievedLines);
            log.debug("SMSC is " + smscNumber);

            String myNumber = checkNumber();
            int myNumberparamId = AppData.parametersStorage.resolveAlias("MODEM_NUMBER");
            if (myNumberparamId > 0 && myNumber.length()==11) {
                Parameter p = AppData.parametersStorage.getParameter(myNumberparamId);
                Measurement m = new Measurement(p, cellId.toString());
                if (!test) {
                    HashMap eventData = new HashMap();
                    eventData.put("parameter", p);
                    eventData.put("measurement", m);
                    event = new Event("Mobile number updated", eventData, Event.Type.PARAMETER_UPDATED);

                    AppData.eventManager.newEvent(event);
                }
            }
                        
            
            while (run) {
                try {

                    String sSQL = "select * from sms_outbox where status=" + STATUS_NEW + " or status=" + STATUS_ERROR_SENDING;

                    List<HashMap> smsToSendList = DBSelect.getRows(sSQL, conn);
                    modemBusy.set(true);
                    while (!smsToSendList.isEmpty()) {
                        HashMap smsToSend = smsToSendList.remove(0);
                        log.info("Processing SMS id " + smsToSend.get("id"));
                        int tries = Tools.parseInt(smsToSend.get("tries_cnt"), 0);
                        tries++;
                        if (tries >= BoxSettingsAPI.getInt("MaxAttemptsToSend")) {
                            log.error("Sending attempts exceeded!");
                            smsToSend.put("status", STATUS_ERROR_ATTEMPTS_EXCEEDED);
                            smsToSend.put("tries_cnt", tries);
                            DBTools.updateRow("sms_outbox", smsToSend, conn);
                            continue;
                        }
                        Date msgDate = (Date) smsToSend.get("date");
                        if (System.currentTimeMillis() > BoxSettingsAPI.getInt("OutgoingMessageMaxAge")*1000l + msgDate.getTime()) {
                            log.error("Message too old!");
                            smsToSend.put("status", STATUS_ERROR_TOO_OLD);
                            smsToSend.put("tries_cnt", tries);
                            DBTools.updateRow("sms_outbox", smsToSend, conn);
                            continue;
                        }

                        smsToSend.put("status", STATUS_SENDING);
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

                        waitForCommand("AT+CMGS=" + j + "\r", outWriter);

                        waitForCommand(pdu + "\032", outWriter);

                        if (lastRecieved.equalsIgnoreCase("OK")) {
                            smsToSend.put("status", STATUS_SENT);
                            smsToSend.put("date_sent", new Date());
                            log.info("Successfully sent");
                            DBTools.updateRow("sms_outbox", smsToSend, conn);
                        } else {
                            smsToSend.put("status", STATUS_ERROR_SENDING);

                            DBTools.updateRow("sms_outbox", smsToSend, conn);
                            log.error("Error sending: " + lastRecieved);
                        }
                    }

                    recievedLines.clear();
                    waitForCommand("AT+CSQ\r", outWriter);
                    int db = parseCSQReply(recievedLines);
                    log.debug("RSSI = " + db + " dBm");
                    
                    int paramId = AppData.parametersStorage.resolveAlias("MODEM_RSSI");
                    if (paramId > 0) {
                        Parameter p = AppData.parametersStorage.getParameter(paramId);
                        Measurement m = new Measurement(p, Tools.parseDouble(db + "", null));
                        if (!test) {
                            HashMap eventData = new HashMap();
                            eventData.put("parameter", p);
                            eventData.put("measurement", m);
                            event = new Event("RSSI updated", eventData, Event.Type.PARAMETER_UPDATED);

                            AppData.eventManager.newEvent(event);
                        }
                    }
                    
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
                        modemBusy.set(false);
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
                            Double myBalabce = checkBalance();
                            int myNumberparamId = AppData.parametersStorage.resolveAlias("MODEM_BALANCE");
                            if (myNumberparamId > 0 && myBalabce>=0) {
                                Parameter p = AppData.parametersStorage.getParameter(myNumberparamId);
                                Measurement m = new Measurement(p, myBalabce);
                                if (!test) {
                                    HashMap eventData = new HashMap();
                                    eventData.put("parameter", p);
                                    eventData.put("measurement", m);
                                    Event event = new Event("Balance updated", eventData, Event.Type.PARAMETER_UPDATED);

                                    AppData.eventManager.newEvent(event);
                                }
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
                
            case SYSTEM_EVENT:
                if (e.name.equals("check_number")) {
                    checkNumber();
                }
                if (e.name.equals("check_balance")) {
                    checkBalance();
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
    
    public String checkNumber() {
        String myNumber = "";
        try {
            log.debug("Checking number");
            waitForCommand("AT^USSDMODE=0\r", outWriter);
            recievedLines.clear();
            waitForCommand("AT+CUSD=1,\"*205#\",15\r", outWriter); //MEGAFON-specific!!
            waitForCommand(null, outWriter, 4 * MODEM_TIMEOUT);
            myNumber = parseUSSDUCS2numReply(recievedLines);
            myNumber=myNumber.replaceAll(" ", "").replaceAll("-", "");
            Matcher m = Pattern.compile("\\d{11}").matcher(myNumber);
            while (m.find()) {
                myNumber = m.group();
            }
            
            log.debug("Recieved number " + myNumber);
        }
        catch (Exception eee)
        {}
        return myNumber;

    }
    
    public Double checkBalance() {
        Double balance = -1D;
        try {
            log.debug("Checking number");
            waitForCommand("AT^USSDMODE=0\r", outWriter);
            recievedLines.clear();
            waitForCommand("AT+CUSD=1,\"*100#\",15\r", outWriter); //MEGAFON-specific!!
            waitForCommand(null, outWriter, 4 * MODEM_TIMEOUT);
            String balanceString = parseUSSDUCS2numReply(recievedLines);

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(balanceString);
            if (matcher.find()) {
                String sVal = matcher.group(0);
                System.out.println(sVal);
                sVal = sVal.replaceAll(replacer, "").trim();
                System.out.println(sVal);
                balance = Tools.parseDouble(sVal, 0);
            }

                log.debug("Recieved number " + balanceString);
            }
        catch (Exception eee)
        {}
        return balance;

    }
        private String parseUSSDUCS2numReply(Queue<String> replyLines) {
        String number = "";
        while (!replyLines.isEmpty()) {

            String pdu = replyLines.poll();
            if (pdu.contains("+CUSD")) {
                try {
                    pdu = pdu.substring(pdu.indexOf("\"") + 1);
                    pdu = pdu.substring(0, pdu.indexOf("\""));

                    if (pdu.length()>16){
                        Usc2 usc = new Usc2();
                        number=usc.decode(pdu);
                        
                    }
                    break;
                } catch (Exception e) {
                                log.debug("err " + e.toString());
                }
            }
        }
        return number;
    }
        
    String encodeAsUCS2(String test) throws UnsupportedEncodingException{

        byte[] bytes = test.getBytes("UTF-16BE");

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();


    }
       
    
    public String ucs2ToUTF8(byte[] ucs2Bytes) throws UnsupportedEncodingException{  
        String unicode = new String(ucs2Bytes, "UTF-16");  
        String utf8 = new String(unicode.getBytes("UTF-8"), "Cp1251");  
        return utf8;  
    } 
    
    
    public class Usc2{
          public HashMap alphabet = new HashMap();
          
          public Usc2(){
            alphabet.put("0410", "А");
            alphabet.put("0411", "Б");
            alphabet.put("0412", "В");
            alphabet.put("0413", "Г");
            alphabet.put("0414", "Д");
            alphabet.put("0415", "Е");
            alphabet.put("0416", "Ж");
            alphabet.put("0417", "З");
            alphabet.put("0418", "И");
            alphabet.put("0419", "Й");
            alphabet.put("041A", "К");
            alphabet.put("041B", "Л");
            alphabet.put("041C", "М");
            alphabet.put("041D", "Н");
            alphabet.put("041E", "О");
            alphabet.put("041F", "П");
            alphabet.put("0420", "Р");
            alphabet.put("0421", "С");
            alphabet.put("0422", "Т");
            alphabet.put("0423", "У");
            alphabet.put("0424", "Ф");
            alphabet.put("0425", "Х");
            alphabet.put("0426", "Ц");
            alphabet.put("0427", "Ч");
            alphabet.put("0428", "Ш");
            alphabet.put("0429", "Щ");
            alphabet.put("042A", "Ъ");
            alphabet.put("042B", "Ы");
            alphabet.put("042C", "Ь");
            alphabet.put("042D", "Э");
            alphabet.put("042E", "Ю");
            alphabet.put("042F", "Я");
            alphabet.put("0430", "а");
            alphabet.put("0431", "б");
            alphabet.put("0432", "в");
            alphabet.put("0433", "г");
            alphabet.put("0434", "д");
            alphabet.put("0435", "е");
            alphabet.put("0436", "ж");
            alphabet.put("0437", "з");
            alphabet.put("0438", "и");
            alphabet.put("0439", "й");
            alphabet.put("043A", "к");
            alphabet.put("043B", "л");
            alphabet.put("043C", "м");
            alphabet.put("043D", "н");
            alphabet.put("043E", "о");
            alphabet.put("043F", "п");
            alphabet.put("0440", "р");
            alphabet.put("0441", "с");
            alphabet.put("0442", "т");
            alphabet.put("0443", "у");
            alphabet.put("0444", "ф");
            alphabet.put("0445", "х");
            alphabet.put("0446", "ц");
            alphabet.put("0447", "ч");
            alphabet.put("0448", "ш");
            alphabet.put("0449", "щ");
            alphabet.put("044A", "ъ");
            alphabet.put("044B", "ы");
            alphabet.put("044C", "ь");
            alphabet.put("044D", "э");
            alphabet.put("044E", "ю");
            alphabet.put("044F", "я");
            alphabet.put("0401", "Ё");
            alphabet.put("0451", "ё");
            alphabet.put("002E", ".");
            alphabet.put("002C", ",");
            alphabet.put("0021", "!");
            alphabet.put("0022", "\"");
            alphabet.put("2116", "№");
            alphabet.put("003B", ";");
            alphabet.put("0025", "%");
            alphabet.put("003A", ": ");
            alphabet.put("003F", "?");
            alphabet.put("002A", "*");
            alphabet.put("0028", "(");
            alphabet.put("0029", ");");
            alphabet.put("002F", "/");
            alphabet.put("0030", "0");
            alphabet.put("0031", "1");
            alphabet.put("0032", "2");
            alphabet.put("0033", "3");
            alphabet.put("0034", "4");
            alphabet.put("0035", "5");
            alphabet.put("0036", "6");
            alphabet.put("0037", "7");
            alphabet.put("0038", "8");
            alphabet.put("0039", "9");
            alphabet.put("002B", "+");
            alphabet.put("002D", "-");
            alphabet.put("003D", "=");
            alphabet.put("2C00", ",");
            alphabet.put("0020", " ");
            alphabet.put("4100", "A");
            alphabet.put("4200", "B");
            alphabet.put("4300", "C");
            alphabet.put("4400", "D");
            alphabet.put("4500", "E");
            alphabet.put("4600", "F");
            alphabet.put("4700", "G");
            alphabet.put("4800", "H");
            alphabet.put("4900", "I");
            alphabet.put("4A00", "J");
            alphabet.put("4B00", "K");
            alphabet.put("4C00", "L");
            alphabet.put("4D00", "M");
            alphabet.put("4E00", "N");
            alphabet.put("4F00", "O");
            alphabet.put("5000", "P");
            alphabet.put("5100", "Q");
            alphabet.put("5200", "R");
            alphabet.put("5300", "S");
            alphabet.put("5400", "T");
            alphabet.put("5500", "U");
            alphabet.put("5600", "V");
            alphabet.put("5700", "W");
            alphabet.put("5800", "X");
            alphabet.put("5900", "Y");
            alphabet.put("5A00", "Z");
            alphabet.put("6100", "a");
            alphabet.put("6200", "b");
            alphabet.put("6300", "c");
            alphabet.put("6400", "d");
            alphabet.put("6500", "e");
            alphabet.put("6600", "f");
            alphabet.put("6700", "g");
            alphabet.put("6800", "h");
            alphabet.put("6900", "i");
            alphabet.put("6A00", "j");
            alphabet.put("6B00", "k");
            alphabet.put("6C00", "l");
            alphabet.put("6D00", "m");
            alphabet.put("6E00", "n");
            alphabet.put("6F00", "o");
            alphabet.put("7000", "p");
            alphabet.put("7100", "q");
            alphabet.put("7200", "r");
            alphabet.put("7300", "s");
            alphabet.put("7400", "t");
            alphabet.put("7500", "u");
            alphabet.put("7600", "v");
            alphabet.put("7700", "w");
            alphabet.put("7800", "x");
            alphabet.put("7900", "y");
            alphabet.put("7A00", "z");
            }
          
          public String decode(String input){
              String result="";
              int i=0;
              try{
                 for (i = 0; i < input.length(); i += 4) 
                 {
                     String sstr = input.substring(i, i+4).toUpperCase();
                     String c ="";
                     try{
                         c=(String)alphabet.get(sstr);
                     }                       
                     catch(Exception e){}
                     if (c !=null)
                         result+=c;
                 }
                 
              }
              catch(Exception ee){
                  
              }
              return result;
          }
        }
}
