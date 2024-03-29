/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONObject;
import org.lobzik.smspi.pi.event.Event;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 * class for generating and parsing JSON to be used in TunnelClient and
 * JSONServlet
 *
 * @author lobzik
 */
public class JSONAPI {

    public static JSONObject getOutMsgStatusJSON(int msgId) throws Exception {
        JSONObject reply = new JSONObject();
        String sSQL = "select * from sms_outbox where id=" + msgId;
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            List<HashMap> resList = DBSelect.getRows(sSQL, conn);
            if (resList.size() != 1) {
                throw new Exception("Message " + msgId + " not found");
            }
            HashMap dbMap = resList.get(0);
            reply.put("message_id", msgId);
            int status = Tools.parseInt(dbMap.get("status"), Integer.MAX_VALUE);
            reply.put("status", status);
            reply.put("status_description", (new MessageStatus(dbMap.get("status"))).eng());
            if (status == MessageStatus.STATUS_SENT) {
                Date dateSent = (Date) dbMap.get("date_sent");
                if (dateSent != null) {
                    reply.put("date_sent", dateSent.getTime());
                }
            }
            
        }
        return reply;
    }

    public static int sendSms(JSONObject json, String username) throws Exception {
        String sSQL = "select * from users where name=?";
        List args = new LinkedList();
        args.add(username);
        String digest = json.getString("digest");
        JSONObject message = json.getJSONObject("message");
        String text = message.getString("text");
        Date validBefore = null;
        if (json.has("valid_before")) {
            validBefore = new Date(json.getLong("valid_before"));
        }

        String recipient = message.getString("recipient");
        if (!recipient.matches("\\+[0-9]{7,15}")) {
            throw new Exception("Invalid recipient (must be like +71234567890)"); //TODO regex check
        }
        if (text.length() == 0 || text.length() > BoxSettingsAPI.getInt("MaxMessageLength")) {
            throw new Exception("Text is missing or too long");
        }
        int msgId = 0;
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            List<HashMap> resList = DBSelect.getRows(sSQL, args, conn);
            if (resList.size() != 1) {
                throw new Exception("Username " + username + " not found");
            }
            String pubKeyStr = (String) resList.get(0).get("public_key");
            BigInteger modulus = new BigInteger(pubKeyStr, 16);
            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, BoxCommonData.RSA_E);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            RSAPublicKey userPublicKey = (RSAPublicKey) factory.generatePublic(spec);
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(userPublicKey);
            verifier.update(message.toString().getBytes());
            boolean valid = verifier.verify(Tools.toByteArray(digest));
            if (!valid) {
                throw new Exception("Invalid digest");
            }
            if (!recipient.matches("\\+[0-9]{7,15}")) {
                throw new Exception("Invalid recipient (must be like +71234567890)");

            }
            KnownRecipientsAPI.checkGreetings(recipient);
            HashMap data = new HashMap();
            data.put("user_id", Tools.parseInt(resList.get(0).get("id"), 0));
            data.put("valid_before", validBefore);
            data.put("message", text);
            data.put("recipient", recipient);
            data.put("date", new Date());
            data.put("status", MessageStatus.STATUS_NEW);
            msgId = DBTools.insertRow("sms_outbox", data, conn);

        }
        Event e = new Event("check_outbox", null, Event.Type.USER_ACTION);
        AppData.eventManager.newEvent(e);
        return msgId;
    }

}
