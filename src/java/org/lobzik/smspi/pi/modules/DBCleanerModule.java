/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi.modules;

import java.sql.Connection;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.smspi.pi.AppData;
import org.lobzik.smspi.pi.BoxCommonData;
import org.lobzik.smspi.pi.ConnJDBCAppender;
import org.lobzik.smspi.pi.event.Event;
import org.lobzik.smspi.pi.event.EventManager;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author lobzik
 */
public class DBCleanerModule extends Thread implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static DBCleanerModule instance = null;
    private static Logger log = null;
    private static boolean run = true;
    private static boolean clearingInProgress = false;
    private static final int DAYS_TO_STORE_LOG_MSG = 9;
    private static final int OUTBOX_MAX_ROWS = 10000;
    private static final int INBOX_MAX_ROWS = 10000;
    private static final int SENSORS_DATA_MAX_ROWS = 10000;

    private DBCleanerModule() { //singleton
    }

    public static DBCleanerModule getInstance() {
        if (instance == null) {
            instance = new DBCleanerModule(); //lazy init
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
        try {
            EventManager.subscribeForEventType(this, Event.Type.TIMER_EVENT);
            while (run) {
                synchronized (this) {
                    wait();
                }
                if (run && !clearingInProgress) {
                    clearingInProgress = true;
                    doClearing();
                    clearingInProgress = false;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void finish() {
        log.info("Stopping " + instance.MODULE_NAME);
        run = false;
        synchronized (instance) {
            instance.notify();
        }
    }

    private void doClearing() {
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            DBSelect.executeStatement("delete from logs where datediff(curdate(), dated) > " + DAYS_TO_STORE_LOG_MSG, null, conn);
            log.info("Log table cleared");
        } catch (Exception e) {
            log.error("Error while Log Clearing: " + e.getMessage());
        }

        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            long minId = DBSelect.getCount("select (max(id) - " + OUTBOX_MAX_ROWS + ") as min_id from sms_outbox", "min_id", null, conn);
            DBSelect.executeStatement("delete from sms_outbox where id < " + minId, null, conn);
            log.info("OutBox cleared");
        } catch (Exception e) {
            log.error("Error while OutBox Clearing: " + e.getMessage());
        }

        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            long minId = DBSelect.getCount("select (max(id) - " + INBOX_MAX_ROWS + ") as min_id from sms_inbox", "min_id", null, conn);
            DBSelect.executeStatement("delete from sms_inbox where id < " + minId, null, conn);
            log.info("InBox cleared");
        } catch (Exception e) {
            log.error("Error while InBox Clearing: " + e.getMessage());
        }

        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            long minId = DBSelect.getCount("select (max(rec_id) - " + SENSORS_DATA_MAX_ROWS + ") as min_id from sensors_data", "min_id", null, conn);
            DBSelect.executeStatement("delete from sensors_data where rec_id < " +minId, null, conn);
            log.info("Sensors Data cleared");
        } catch (Exception e) {
            log.error("Error while Sensors Data Clearing: " + e.getMessage());
        }

    }

    @Override
    public synchronized void handleEvent(Event e) {
        if (e.type == Event.Type.TIMER_EVENT && e.name.equals("db_clearing")) {
            if (!clearingInProgress) {
                synchronized (this) {
                    notify();
                }
            }
        }
    }

}
