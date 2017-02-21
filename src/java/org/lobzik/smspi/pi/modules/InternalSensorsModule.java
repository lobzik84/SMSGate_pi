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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;

import org.lobzik.smspi.pi.AppData;

import org.lobzik.smspi.pi.BoxCommonData;
import org.lobzik.smspi.pi.BoxSettingsAPI;
import org.lobzik.smspi.pi.ConnJDBCAppender;
import org.lobzik.smspi.pi.event.Event;
import org.lobzik.smspi.pi.event.EventManager;
import org.lobzik.tools.Tools;

/**
 *
 * @author lobzik
 */
public class InternalSensorsModule extends Thread implements Module {

    private static InternalSensorsModule instance = null;

    public final String MODULE_NAME = this.getClass().getSimpleName();

    private static Logger log = null;
    private static boolean run = true;
    private static CommPort commPort = null;
    private static SerialWriter serialWriter = null;

    private static final int BAUD_RATE = 57600;
    private static final int DATABITS = SerialPort.DATABITS_8;
    private static final int STOPBITS = SerialPort.STOPBITS_1;
    private static final int PARITY = SerialPort.PARITY_NONE;
    private static final int PORT_TIMEOUT = 2000;
    private static final int ONEWAY_433_PARAMETERS_TIMEOUT = 1000 * 15;
    //private static List<Integer> ONEWAY_PARAMETERS = new ArrayList();

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void handleEvent(Event e) {

        switch (e.getType()) {
            case TIMER_EVENT:
                if (e.name.equals("internal_sensors_poll")) {
                    if (serialWriter != null) {
                        serialWriter.poll();
                    }
                }
                break;

            case USER_ACTION:
                if (e.name.equals("internal_uart_command")) {
                    log.debug("Sending command " + (String) e.data.get("uart_command"));
                    if (serialWriter != null) {
                        serialWriter.doCommand((String) e.data.get("uart_command"));
                    }
                }
                break;

        }
    }

    private InternalSensorsModule() { //singleton
    }

    public static InternalSensorsModule getInstance() {
        if (instance == null) {
            instance = new InternalSensorsModule(); //lazy init
            log = Logger.getLogger(instance.MODULE_NAME);
            Appender appender = ConnJDBCAppender.getAppenderInstance(AppData.dataSource, instance.MODULE_NAME);
            log.addAppender(appender);
        }
        return instance;
    }

    @Override
    public synchronized void run() {

        setName(this.getClass().getSimpleName() + "-Thread");
        log.info("Starting " + getName() + " on " + BoxCommonData.SERIAL_PORT);
        EventManager.subscribeForEventType(this, Event.Type.TIMER_EVENT);
        EventManager.subscribeForEventType(this, Event.Type.USER_ACTION);
        try {
            connect(BoxCommonData.SERIAL_PORT);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void finish() {
        log.info("Stopping SerialWriter");
        if (serialWriter != null) {
            serialWriter.finish();

        }
        run = false;
    }

    /*private void parseUartReply(String data) {
        try {
            if (data.contains("DS18B20")) {
                String val = data.substring(data.lastIndexOf(":") + 1, data.length());
                val = val.trim();
                String address = data.substring(0, data.indexOf("DS18B20"));
                address = address.trim();

            } else if (data.contains(":")) {
                String val = data.substring(data.lastIndexOf(":") + 1, data.length());
                val = val.trim();
                String paramName = data.substring(0, data.indexOf(":"));
                paramName = paramName.trim();
                Parameter doorP = AppData.parametersStorage.getParameterByAlias("DOOR_SENSOR");
                Parameter wetP = AppData.parametersStorage.getParameterByAlias("WET_SENSOR");
                if (paramName.equals("433_RX")) {
                    Measurement m = null;
                    HashMap eventData = new HashMap();
                    String door433Addresses = BoxSettingsAPI.get("DoorSensorAddress433");
                    if (door433Addresses != null && door433Addresses.length() > 0) {
                        for (String address : door433Addresses.split(",")) {
                            if (val.equals(address)) {
                                m = new Measurement(doorP, true);
                                eventData.put("parameter", doorP);

                            }
                        }
                    }

                    String wet433Addresses = BoxSettingsAPI.get("WetSensorAddress433");
                    if (wet433Addresses != null && wet433Addresses.length() > 0) {
                        for (String address : wet433Addresses.split(",")) {
                            if (val.equals(address)) {
                                m = new Measurement(wetP, true);
                                eventData.put("parameter", wetP);
                            }
                        }
                    }

                    if (m != null) {
                        eventData.put("measurement", m);
                        Event e = new Event("433 recieved", eventData, Event.Type.PARAMETER_UPDATED);
                        AppData.eventManager.newEvent(e);
                    }
                } else {
                    int paramId = AppData.parametersStorage.resolveAlias(paramName);

                    if (paramId > 0) {
                        HashMap eventData = new HashMap();
                        Parameter p = AppData.parametersStorage.getParameter(paramId);
                        Measurement m = null;
                        switch (p.getType()) {
                            case BOOLEAN:
                                m = new Measurement(p, Tools.parseBoolean(val, null));
                                break;

                            case DOUBLE:
                                if (val.length() > 0 && !val.equalsIgnoreCase("nan") && !val.equalsIgnoreCase("error")) {
                                    double raw = Tools.parseDouble(val, null);
                                    if (raw == 1023) {
                                        log.debug("Suspicious value " + p.getAlias() + "=" + raw + " ADC overloaded?");
                                    }
                                    m = new Measurement(p, raw * p.getCalibration() + p.getCorrection());
                                }
                                break;

                            case INTEGER:
                                m = new Measurement(p, Tools.parseInt(val, null));
                                break;

                            default:
                                m = new Measurement(p, val);
                                break;

                        }
                        if (m != null) {
                            eventData.put("parameter", p);
                            eventData.put("measurement", m);
                            Event e = new Event("internal sensors updated", eventData, Event.Type.PARAMETER_UPDATED);

                            AppData.eventManager.newEvent(e);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }*/

    private void connect(String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

        commPort = portIdentifier.open(this.getClass().getName(), PORT_TIMEOUT);

        SerialPort serialPort = (SerialPort) commPort;
        serialPort.setSerialPortParams(BAUD_RATE, DATABITS, STOPBITS, PARITY);

        serialWriter = new SerialWriter(serialPort.getOutputStream(), serialPort);
        serialWriter.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

        String decodedString;
        while ((decodedString = in.readLine()) != null && run) {
            log.debug("UART: " + decodedString);
            //parseUartReply(decodedString);
        }
        in.close();
        serialWriter.finish();
        commPort = null;
    }

    public static class SerialWriter extends Thread {

        OutputStream out;
        SerialPort port;
        private static boolean run = true;
        private static String command = null;

        public SerialWriter(OutputStream out, SerialPort port) {
            setName(this.getClass().getSimpleName() + "-Thread");
            this.out = out;
            this.port = port;
        }

        public void finish() {
            run = false;
            synchronized (this) {
                notify();
            }
        }

        public void poll() {
            synchronized (this) {
                notify();
            }
        }

        public void doCommand(String command) {
            if (command == null || command.length() == 0) {
                return;
            }
            SerialWriter.command = command;
            synchronized (this) {
                notify();
            }
        }

        @Override
        public void run() {
            OutputStreamWriter outWriter = new OutputStreamWriter(this.out);
            while (run) {
                try {
                    if (command != null && command.length() > 0) {
                        outWriter.write(command + "\r\n");
                        command = null;
                    } else {
                        outWriter.write("GS\r\n");
                    }
                    outWriter.flush();
                    try {
                        synchronized (this) {
                            wait();
                        }
                    } catch (InterruptedException ie) {
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            try {
                outWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                port.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
