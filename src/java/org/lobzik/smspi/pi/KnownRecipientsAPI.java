/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.smspi.pi.event.Event;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author lobzik
 */
public class KnownRecipientsAPI {

    static Logger log = null;

    static {
        if (log == null) {
            String MODULE_NAME = KnownRecipientsAPI.class.getClass().getSimpleName();
            log = Logger.getLogger(MODULE_NAME);
            Appender appender = ConnJDBCAppender.getAppenderInstance(AppData.dataSource, MODULE_NAME);
            log.addAppender(appender);
        }
    }

    /*
    *
    *  @return new id, or zero if recipient already in database or invalid
     */
    public static int addRecipent(String number) {
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            String sSQL = "select id from known_recipients where number=?";
            ArrayList args = new ArrayList();
            args.add(number);
            List result = DBSelect.getRows(sSQL, args, conn);
            if (result.isEmpty()) {
                HashMap paramsMap = new HashMap();
                paramsMap.put("number", number);
                int newId = DBTools.insertRow("known_recipients", paramsMap, conn);
                return newId;
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return 0;
    }

    public static void checkGreetings(String number) {
        int newRecipientId = addRecipent(number);
        if (newRecipientId > 0) { //if new recipient added
            log.info("New recipient! id=" + newRecipientId + ", number " + number);
            log.info("Sending greetings");
            for (String k : BoxSettingsAPI.getSettingsMap().keySet()) {
                if (k.startsWith("GreetingSMSText")) {
                    String greeting = BoxSettingsAPI.get(k);
                    HashMap data = new HashMap();

                    data.put("message", greeting);
                    data.put("recipient", number);
                    data.put("date", new Date());
                    data.put("status", MessageStatus.STATUS_NEW);
                    try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                        DBTools.insertRow("sms_outbox", data, conn);

                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
            }
            Event e = new Event("check_outbox", null, Event.Type.USER_ACTION);
            AppData.eventManager.newEvent(e);
        }

    }

}
