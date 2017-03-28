/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi.modules;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.entity.Parameter;
import org.lobzik.smspi.pi.AppData;
import org.lobzik.smspi.pi.BoxCommonData;
import org.lobzik.smspi.pi.ConnJDBCAppender;
import org.lobzik.smspi.pi.event.Event;
import org.lobzik.smspi.pi.event.EventManager;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author lobzik
 */
public class DBDataWriterModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static DBDataWriterModule instance = null;
    private static Connection conn = null;
    private static long lastWriteTime = 0l;
    private static Logger log = null;

    private DBDataWriterModule() { //singleton
    }

    public static DBDataWriterModule getInstance() {
        if (instance == null) {
            instance = new DBDataWriterModule(); //lazy init
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
            conn = DBTools.openConnection(BoxCommonData.dataSourceName);
            //ready to accept events, subscribing

            EventManager.subscribeForEventType(this, Event.Type.TIMER_EVENT);
            EventManager.subscribeForEventType(this, Event.Type.PARAMETER_CHANGED);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void handleEvent(Event e) {
        if (e.type == Event.Type.TIMER_EVENT && e.name.equals("write_db_data")) {
            for (Integer paramId : AppData.parametersStorage.getParameterIds()) {
                try {
                    Parameter p = AppData.parametersStorage.getParameter(paramId);
                    switch (p.getType()) {
                        case DOUBLE:
                            Measurement avg = AppData.measurementsCache.getAvgMeasurementFrom(p, lastWriteTime);
                            Measurement min = AppData.measurementsCache.getMinMeasurementFrom(p, lastWriteTime);
                            Measurement max = AppData.measurementsCache.getMaxMeasurementFrom(p, lastWriteTime);
                            if (avg != null && min != null && max != null) {
                                HashMap dataMap = new HashMap();
                                dataMap.put("parameter_id", paramId);

                                dataMap.put("value_d", avg.getDoubleValue());
                                dataMap.put("value_min", min.getDoubleValue());
                                dataMap.put("date_min", new Date(min.getTime()));
                                dataMap.put("value_max", max.getDoubleValue());
                                dataMap.put("date_max", new Date(max.getTime()));
                                dataMap.put("value_avg", avg.getDoubleValue());
                                dataMap.put("date", new Date(avg.getTime()));

                                log.debug("Writing " + dataMap.toString());
                                DBTools.insertRow("sensors_data", dataMap, conn);
                            }
                            break;

                        case BOOLEAN:
                            Integer transferCounts = AppData.measurementsCache.getTransferTrueCountFrom(p, lastWriteTime);
                            if (transferCounts != null && transferCounts > 0) {
                                HashMap dataMap = new HashMap();
                                dataMap.put("parameter_id", paramId);

                                dataMap.put("transfer_count", transferCounts);
                                dataMap.put("date", new Date());

                                log.debug("Writing " + dataMap.toString());
                                DBTools.insertRow("sensors_data", dataMap, conn);
                            }
                            break;

                        case INTEGER:
                            break;
                    }

                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
            lastWriteTime = System.currentTimeMillis();
        } else if (e.type == Event.Type.PARAMETER_CHANGED) {
            Measurement m = (Measurement) e.data.get("measurement");
            Parameter p = m.getParameter();
            if (p.getType() == Parameter.Type.BOOLEAN && m.getBooleanValue() != null) {
                HashMap dataMap = new HashMap();
                dataMap.put("parameter_id", p.getId());

                dataMap.put("value_b", m.getBooleanValue()?1:0);
                dataMap.put("date", new Date());

                log.debug("Writing " + dataMap.toString());
                try {
                    DBTools.insertRow("sensors_data", dataMap, conn);
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
        }

    }

    public static void finish() {
        DBTools.closeConnection(conn);
    }
}
