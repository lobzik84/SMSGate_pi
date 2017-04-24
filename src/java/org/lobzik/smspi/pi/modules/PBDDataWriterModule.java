/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi.modules;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.smspi.pi.AppData;
import org.lobzik.smspi.pi.BoxCommonData;
import org.lobzik.smspi.pi.BoxSettingsAPI;
import org.lobzik.smspi.pi.ConnJDBCAppender;
import org.lobzik.smspi.pi.event.Event;
import org.lobzik.smspi.pi.event.EventManager;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author lobzik
 */
public class PBDDataWriterModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static PBDDataWriterModule instance = null;
    private static final AtomicBoolean busy = new AtomicBoolean(false);
    private static Logger log = null;

    private static final String requestInsertQueryFields_String
            = "msg_id, message, tel_no, type, msg_date, date_sent, status, user_id, username";

    private static final String requestInsertQuery
            = "INSERT INTO smsgate_data(" + requestInsertQueryFields_String + ") \n"
            + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final List<String> requestInsertQueryFields = Arrays.asList(requestInsertQueryFields_String.replace(" ", "").split(","));

    private PBDDataWriterModule() { //singleton
    }

    public static PBDDataWriterModule getInstance() {
        if (instance == null) {
            instance = new PBDDataWriterModule(); //lazy init
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
    public void start() {
        try {

            //ready to accept events, subscribing
            EventManager.subscribeForEventType(this, Event.Type.TIMER_EVENT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void handleEvent(Event e) {
        if (e.type == Event.Type.TIMER_EVENT && e.name.equals("write_PBD_data")) {
            if (busy.get()) {
                log.error("PBD sync already running!");//TODO return
                return;
            }
            new Thread() {
                @Override
                public void run() {
                    busy.set(true);
                    try {
                        log.info("Selecting data for PBD ");
                        String messageListSql = "select a.*, u.name from \n"
                                + "(select si.id msg_id, si.message, si.sender as tel_no, 'inbox' as type, si.date msg_date, null as date_sent, si.status, null as user_id from sms_inbox si\n"
                                + "where si.date > ?\n"
                                + "union \n"
                                + "select so.id msg_id, so.message, so.recipient as tel_no, 'outbox' as type, so.date msg_date, so.date_sent, so.status, so.user_id from sms_outbox so\n"
                                + "where so.date > ?) a\n"
                                + "left join users u on u.id = a.user_id\n"
                                + "limit 60000"; //1200 per hour * 24 hours * (inbox+outbox)
                        ArrayList argsList = new ArrayList();
                        Calendar c = new GregorianCalendar();
                        c.add(Calendar.DAY_OF_YEAR, -1);
                        argsList.add(c.getTime());
                        argsList.add(c.getTime());
                        List<HashMap> resList;
                        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                            resList = DBSelect.getRows(messageListSql, argsList, conn);
                            log.info("Writing to PBD " + resList.size() + " entries");
                        }
                        if (!resList.isEmpty()) {
                            String connectionUrl = BoxSettingsAPI.get("PBDConnURL");
                            Class.forName(BoxSettingsAPI.get("PBDConnDriver"));
                            try (Connection mssqlconn = DriverManager.getConnection(connectionUrl)) {
                                for (HashMap map : resList) {
                                    
                                    try (PreparedStatement preparedStatement = mssqlconn.prepareStatement(requestInsertQuery)) {
                                        int i = 0;
                                        for (String f : requestInsertQueryFields) {
                                            i++;
                                            if (map.get(f) == null) {
                                                preparedStatement.setObject(i, null);
                                            } else if (map.get(f) instanceof String) {
                                                preparedStatement.setString(i, (String) map.get(f));
                                            } else if (map.get(f) instanceof Integer) {
                                                preparedStatement.setInt(i, (Integer) map.get(f));
                                            } else if (map.get(f) instanceof Long) {
                                                preparedStatement.setInt(i, ((Long) map.get(f)).intValue());
                                            } else if (map.get(f) instanceof Double) {
                                                preparedStatement.setDouble(i, (Double) map.get(f));
                                            } else if (map.get(f) instanceof Date) {
                                                preparedStatement.setTimestamp(i, new Timestamp(((Date) map.get(f)).getTime()));
                                            }
                                        }

                                        preparedStatement.executeUpdate();
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        log.error("Error while writing to PBD");
                        log.error(ex.getMessage());
                    }
                    busy.set(false);
                }
            }.start();
        }

    }

    public static void finish() {

    }
}
