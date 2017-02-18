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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

    private static long lastParametersWriteTime = 0;

    public static JSONObject getSettingsJSON(RSAPublicKey publicKey, String login) throws Exception {
        JSONObject reply = new JSONObject();
        JSONObject settingsJSON = new JSONObject();
        Map<String, String> settings = BoxSettingsAPI.getSettingsMap();
        for (String name : settings.keySet()) {
            settingsJSON.put(name, settings.get(name));
        }
        settingsJSON.put("UserLogin", login);
        reply.put("settings", settingsJSON);
        return reply;
    }

    public static void sendSms(JSONObject json, String username) throws Exception {
        String sSQL = "select * from users where name=?";
        List args = new LinkedList();
        args.add(username);
        String digest = json.getString("digest");
        JSONObject message = json.getJSONObject("message");
        String text = message.getString("text");
        String recipient = message.getString("recipient");
        if (!recipient.matches("\\+[0-9]{7,15}")) {
            throw new Exception("Invalid recipient (must be like +71234567890)"); //TODO regex check
        }
        if (text.length() == 0 || text.length() > 160) {
            throw new Exception("Text is missing or too long");
        }
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
        }
        HashMap data = new HashMap();
        data.put("message", text);
        data.put("recipient", recipient);
        Event e = new Event("send_sms", data, Event.Type.USER_ACTION);
        AppData.eventManager.newEvent(e);

    }

}
