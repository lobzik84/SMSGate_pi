/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.lobzik.smspi.pi.event.Event;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author lobzik
 */
public class XMLAPI {

    public static Document getOutMsgStatus(int msgId) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();

        Element rootElement = doc.createElement("response");
        doc.appendChild(rootElement);

        String sSQL = "select * from sms_outbox where id=" + msgId;
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            List<HashMap> resList = DBSelect.getRows(sSQL, conn);
            if (resList.size() != 1) {
                throw new Exception("Message " + msgId + " not found");
            }
            HashMap dbMap = resList.get(0);

            int status = Tools.parseInt(dbMap.get("status"), Integer.MAX_VALUE);

            Element res = doc.createElement("result");
            res.appendChild(doc.createTextNode("success"));
            rootElement.appendChild(res);

            Element message_id = doc.createElement("message_id");
            message_id.appendChild(doc.createTextNode(msgId + ""));
            rootElement.appendChild(message_id);

            Element statusEl = doc.createElement("status");
            statusEl.appendChild(doc.createTextNode(dbMap.get("status") + ""));
            rootElement.appendChild(statusEl);

            Element status_description = doc.createElement("status_description");
            status_description.appendChild(doc.createTextNode((new MessageStatus(dbMap.get("status"))).eng()));
            rootElement.appendChild(status_description);

            if (status == MessageStatus.STATUS_SENT) {
                Date dateSent = (Date) dbMap.get("date_sent");
                if (dateSent != null) {
                    Element date_sent = doc.createElement("status_description");
                    date_sent.appendChild(doc.createTextNode(dateSent.getTime() + ""));
                    rootElement.appendChild(date_sent);
                }
            }
        }
        return doc;
    }

    public static Document sendSms(Document req, String username) throws Exception {
        String sSQL = "select * from users where name=?";
        List args = new LinkedList();
        args.add(username);
        NodeList nl = req.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
            throw new Exception("Cannot find Signature element");
        }

        String text = req.getElementsByTagName("text").item(0).getTextContent();
        String recipient = null;
        String group = null;
        String admin = null;
        if (req.getElementsByTagName("recipient").getLength() == 1) {
            recipient = req.getElementsByTagName("recipient").item(0).getTextContent();
            if (!recipient.matches("\\+[0-9]{7,15}")) {
                throw new Exception("Invalid recipient (must be like +71234567890)");
            }
        } else {
            group = req.getElementsByTagName("group").item(0).getTextContent();
            admin = req.getElementsByTagName("admin").item(0).getTextContent();
        }

        Date validBefore = null;
        NodeList validBeforeNL = req.getElementsByTagName("valid_before");
        if (validBeforeNL.getLength() == 1) {
            long vbMs = Tools.parseLong(validBeforeNL.item(0).getTextContent(), 0);
            if (vbMs > 0) {
                validBefore = new Date(vbMs);
            }
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

            DOMValidateContext valContext = new DOMValidateContext(userPublicKey, nl.item(0));
            XMLSignatureFactory xfactory = XMLSignatureFactory.getInstance("DOM");
            XMLSignature signature = xfactory.unmarshalXMLSignature(valContext);
            boolean coreInvalidity = signature.validate(valContext);
            if (coreInvalidity) {
                throw new Exception("Invalid digest!");
            }
            if (recipient != null) {
                KnownRecipientsAPI.checkGreetings(recipient);
                HashMap data = new HashMap();
                data.put("user_id", Tools.parseInt(resList.get(0).get("id"), 0));
                data.put("valid_before", validBefore);
                data.put("message", text);
                data.put("recipient", recipient);
                data.put("date", new Date());
                data.put("status", MessageStatus.STATUS_NEW);
                msgId = DBTools.insertRow("sms_outbox", data, conn);
                Event e = new Event("check_outbox", null, Event.Type.USER_ACTION);
                AppData.eventManager.newEvent(e);
            } else {
                HashMap h = new HashMap();
                h.put("admin", admin);
                h.put("group", group);
                h.put("valid_before", validBefore);
                h.put("message", text);
                Event e = new Event("send_group_sms", h, Event.Type.USER_ACTION);
                AppData.eventManager.newEvent(e);
            }

        }

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();

        Element rootElement = doc.createElement("response");
        doc.appendChild(rootElement);
        Element res = doc.createElement("result");
        res.appendChild(doc.createTextNode("success"));
        rootElement.appendChild(res);

        Element message_id = doc.createElement("message_id");
        message_id.appendChild(doc.createTextNode(msgId + ""));
        rootElement.appendChild(message_id);

        return doc;
    }

}
