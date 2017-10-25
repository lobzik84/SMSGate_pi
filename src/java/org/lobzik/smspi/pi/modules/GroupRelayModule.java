/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi.modules;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.smspi.pi.AppData;
import org.lobzik.smspi.pi.BoxCommonData;
import org.lobzik.smspi.pi.ConnJDBCAppender;
import org.lobzik.smspi.pi.KnownRecipientsAPI;
import org.lobzik.smspi.pi.MessageStatus;
import org.lobzik.smspi.pi.event.Event;
import org.lobzik.smspi.pi.event.EventManager;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author lobzik
 */
public class GroupRelayModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static GroupRelayModule instance = null;

    private static Logger log = null;

    private GroupRelayModule() { //singleton
    }

    public static GroupRelayModule getInstance() {
        if (instance == null) {
            instance = new GroupRelayModule(); //lazy init
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

        EventManager.subscribeForEventType(this, Event.Type.USER_ACTION);

    }

    @Override
    public void handleEvent(Event e) {
        if (e.getType() == Event.Type.USER_ACTION && e.name.equals("sms_recieved")) {
            String sender = (String) e.data.get("sender");
            String sSQL = "select * from admins where phone_number=?";
            ArrayList arg = new ArrayList(1);
            arg.add(sender);
            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                List<HashMap> res = DBSelect.getRows(sSQL, arg, conn);
                if (res.size() > 0) {
                    HashMap adminMap = res.get(0);
                    int adminId = Tools.parseInt(adminMap.get("admin_id"), 0);
                    log.info("Got SMS from admin id=" + adminId);
                    String msg = (String) e.data.get("text");
                    int index1 = msg.indexOf(":");
                    int index2 = msg.indexOf(":", index1 + 1);
                    if (index1 >= 0 && index2 - index1 > 0) {
                        String group = msg.substring(index1 + 1, index2);
                        String text = msg.substring(index2 + 1);
                        sSQL = "select * from groups where admin_id=" + adminId + " and group_name=?";

                        arg.clear();
                        arg.add(group);
                        List<HashMap> grps = DBSelect.getRows(sSQL, arg, conn);
                        if (grps.size() > 0) {
                            LinkedList<HashMap> rcpnts = new LinkedList();
                            for (HashMap grp : grps) {
                                int grpId = Tools.parseInt(grp.get("id"), 0);
                                sSQL = "select * from group_recipients where group_id=" + grpId + " and admin_id=" + adminId;
                                rcpnts.addAll(DBSelect.getRows(sSQL, conn));
                            }
                            log.info("Relaying SMS from admin id=" + adminId + " to group " + group + " (" + rcpnts.size() + " recipients)");
                            for (HashMap rcpnt : rcpnts) {
                                String number = (String) rcpnt.get("number");
                                KnownRecipientsAPI.checkGreetings(number); //? do we need this
                                HashMap data = new HashMap();

                                data.put("message", text);
                                data.put("recipient", number);
                                data.put("date", new Date());
                                data.put("status", MessageStatus.STATUS_NEW);

                                DBTools.insertRow("sms_outbox", data, conn);
                            }
                            Event ce = new Event("check_outbox", null, Event.Type.USER_ACTION);
                            AppData.eventManager.newEvent(ce);
                        } else {
                            log.error("Relay group " + group + " for admin id=" + adminId + " not found");
                        }
                    }
                }
            } catch (Exception ex) {
                log.error(ex);
            }

        } else if (e.getType() == Event.Type.USER_ACTION && e.name.equals("send_group_sms")) {

            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                String groupName = (String) e.data.get("group");
                String adminName = (String) e.data.get("admin");
                String text = (String) e.data.get("message");
                Date validBefore = (Date) e.data.get("valid_before");

                log.info("Sending group sms from admin " + adminName + " to group " + groupName);

                String sSQL = "select gr.number from group_recipients gr\n"
                        + "join groups g on g.id=gr.group_id\n"
                        + "join admins a on a.admin_id=g.admin_id\n"
                        + "where a.login=? and g.group_name=?";
                List args = new LinkedList();
                args.add(adminName);
                args.add(groupName);
                List<HashMap> rcpnts = DBSelect.getRows(sSQL, args, conn);
                if (rcpnts.isEmpty()) {
                    log.warn("No recipients found for admin " + adminName + " and group " + groupName);
                }
                for (HashMap rcpnt : rcpnts) {
                    String number = (String) rcpnt.get("number");
                    KnownRecipientsAPI.checkGreetings(number); //? do we need this
                    HashMap data = new HashMap();

                    data.put("message", text);
                    data.put("recipient", number);
                    data.put("date", new Date());
                    data.put("status", MessageStatus.STATUS_NEW);
                    data.put("valid_before", validBefore);
                    DBTools.insertRow("sms_outbox", data, conn);
                }
                Event ce = new Event("check_outbox", null, Event.Type.USER_ACTION);
                AppData.eventManager.newEvent(ce);
            } catch (Exception ex) {
                log.error(ex);
            }

        }
    }

    public static void finish() {

    }
}
