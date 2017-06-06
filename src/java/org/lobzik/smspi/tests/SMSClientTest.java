/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.tests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAPrivateKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.json.JSONObject;

/**
 *
 * @author lobzik
 */
public class SMSClientTest {

    static String SMSGateUrl = "http://smspi.molnet.ru/smspi/json";
    static String username = "supertt";
    static String privateKeyString = "a4cac7b1904641014f1159acd3bcac57fc5468d5e946e8a551c88741fe6d7c2fd195379420de98132302e7c3bb482bdeb2cfe37e1dab2aa29e2403056931f220eebb85e07b5a23037d66e6cb28adb842b6cbd166e28b2ea14bd00df642c23a3684209a25284111cb1b3e6fde2ce0cdfaf23ae72fffed1ef16e201937a4b6873d";
    static String publicKeyString = "bd96fa4a28de14a4b6c5ffdb63ab7ed3e99a8ed69d89325445ef4bc8d247c343df2505ae94777455ab555de54f777ca06def3f6ec2715011831683feabb65e142d68877fca6c5bee6ed83adbf426f8c533d2a6e2921622dfd7da025a8509e60479dbfed55b9bb8dd9108dc981debccf7e7be7ae459257bffd93fb307ce121765";
    static String text = "Тест супертт";//Это тест отправки sms:";// + System.currentTimeMillis();
    static String recipient = "+79263357107";
    static final String CHARSET = "UTF-8";

    public static void main(String[] args) {
        new Thread() {
            @Override
            public void run() {
                try {

                    JSONObject message = new JSONObject();
                    message.put("text", text);
                    message.put("recipient", recipient);

                    //creating rsa key
                    BigInteger modulus = new BigInteger(publicKeyString, 16);
                    BigInteger privateExp = new BigInteger(privateKeyString, 16);
                    RSAPrivateKeySpec spec = new RSAPrivateKeySpec(modulus, privateExp);
                    KeyFactory kf = KeyFactory.getInstance("RSA");
                    RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(spec);
                    // creating digest
                    Signature digest = Signature.getInstance("SHA256withRSA");
                    digest.initSign(privateKey);
                    digest.update(message.toString().getBytes(CHARSET));
                    byte[] digestRaw = digest.sign();
                    String digestHex = DatatypeConverter.printHexBinary(digestRaw);

                    long validBefore = System.currentTimeMillis() + 30 * 60 * 1000l;//expires in 1 min 

                    JSONObject requestJson = new JSONObject();
                    requestJson.put("valid_before", validBefore);
                    requestJson.put("action", "send_sms");
                    requestJson.put("message", message);
                    requestJson.put("username", username);
                    requestJson.put("digest", digestHex);

                    URL url = new URL(SMSGateUrl);

                    URLConnection conn = url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), CHARSET);
                    out.write(requestJson.toString());
                    out.close();

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String decodedString;
                    StringBuffer sb = new StringBuffer();
                    while ((decodedString = in.readLine()) != null) {
                        sb.append(decodedString);
                    }
                    in.close();
                    JSONObject response = new JSONObject(sb.toString());
                    String result = response.getString("result");
                    int messageId = 0;
                    if (response.has("msg_id")) {
                        messageId = response.getInt("msg_id");
                    }
                    if (result.equals("success")) {
                        System.out.println("message sent to gate successfully, id = " + messageId);

                    } else {
                        System.err.print("Error from sms gate: ");
                        System.err.println(response.getString("message"));
                    }

                    Thread.sleep(3000);

                    requestJson = new JSONObject();
                    requestJson.put("action", "get_out_status");
                    requestJson.put("msg_id", messageId);

                    url = new URL(SMSGateUrl);

                    conn = url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    out = new OutputStreamWriter(conn.getOutputStream(), CHARSET);
                    out.write(requestJson.toString());
                    out.close();

                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    sb = new StringBuffer();
                    while ((decodedString = in.readLine()) != null) {
                        sb.append(decodedString);
                    }
                    in.close();
                    response = new JSONObject(sb.toString());
                    //result = response.getString("result");
                    System.out.println(response.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }
}
