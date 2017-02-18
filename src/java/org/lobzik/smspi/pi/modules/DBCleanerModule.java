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
    private static Connection conn = null;
    private static Logger log = null;
    private static boolean run = true;
    private static boolean clearingInProgress = false;
    private static final int DAYS_TO_STORE_LOG_MSG = 9;

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
        try {
            conn = DBTools.openConnection(BoxCommonData.dataSourceName);
            DBSelect.executeStatement("delete from logs where datediff(curdate(), dated) > " + DAYS_TO_STORE_LOG_MSG, null, conn);
            log.info("Log table cleared");

 
        } catch (Exception e) {
            log.error("Error while DB Clearing: " + e.getMessage());

        } finally {
            DBTools.closeConnection(conn);
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
