/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi.modules;

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

    static String SMSGateUrl = "http://192.168.11.22:8080/smspi/json";
    static String username = "mobility";
    static String privateKeyString = "61832d2f4e3032863f3b1957d5cc0a4e5bdf3f26cb3995bac83fd50f7da4eab282cf6af34aa52ab95da0961f5ee28980c36cf141605120c907cbc63e92a21cf1f72ca32e7ceb9fb4fe5a3245d4e136ee57a88e26fbefcac331a8bb8230cd8794759489782bdd385fa05efcde0f14b0b12784c93ae463531cbdf9831edf778171";
    static String publicKeyString = "844af8350323d29b7bf760245eec4298491e9c144c813493e2d315669de4c5f67ac6eb2117c539d3334db9bd64322d5950fd6160fe4d328d5c3db23f9bc7d84d82847fce176223b9d514a33067096e80f832f00661798d44949ce5fc01d48bbe1e216e236f05af2f7b692d1bbf9fba5e1dfc4c7a3c438a42b7a0a598b2390f33";
    static String text = "Это тест отправки sms:" + System.currentTimeMillis();
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

                    long validBefore = System.currentTimeMillis() + 1 * 60 * 1000l;//expires in 1 min 

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
                    System.out.print(response.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }
}
