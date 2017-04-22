/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.tests;

import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Collections;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author lobzik
 */
public class SMSClientTestXML {

    static String SMSGateUrl = "http://192.168.11.22:8080/smspi/xml";
    static String username = "mobility";
    static String privateKeyString = "61832d2f4e3032863f3b1957d5cc0a4e5bdf3f26cb3995bac83fd50f7da4eab282cf6af34aa52ab95da0961f5ee28980c36cf141605120c907cbc63e92a21cf1f72ca32e7ceb9fb4fe5a3245d4e136ee57a88e26fbefcac331a8bb8230cd8794759489782bdd385fa05efcde0f14b0b12784c93ae463531cbdf9831edf778171";
    static String publicKeyString = "844af8350323d29b7bf760245eec4298491e9c144c813493e2d315669de4c5f67ac6eb2117c539d3334db9bd64322d5950fd6160fe4d328d5c3db23f9bc7d84d82847fce176223b9d514a33067096e80f832f00661798d44949ce5fc01d48bbe1e216e236f05af2f7b692d1bbf9fba5e1dfc4c7a3c438a42b7a0a598b2390f33";
    static String  publicExponentString = "10001";
    static String text = "Это тест отправки sms через XML:" + System.currentTimeMillis();
    static String recipient = "+79263357107";
    static final String CHARSET = "UTF-8";

    public static void main(String[] args) {
        new Thread() {
            @Override
            public void run() {
                try {

                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

                    Document doc = docBuilder.newDocument();

                    Element rootElement = doc.createElement("request");
                    doc.appendChild(rootElement);
                    
                    Element usernameEl = doc.createElement("username");
                    usernameEl.appendChild(doc.createTextNode(username));
                    rootElement.appendChild(usernameEl);
                    
                    Element action = doc.createElement("action");
                    action.appendChild(doc.createTextNode("send_sms"));
                    rootElement.appendChild(action);
                    
                    Element message = doc.createElement("message");

                    long validBefore = System.currentTimeMillis() + 10 * 60 * 1000l;//expires in 10 min 

                    Element valid_before = doc.createElement("valid_before");
                    valid_before.appendChild(doc.createTextNode(validBefore + ""));
                    message.appendChild(valid_before);

                    Element textEl = doc.createElement("text");
                    textEl.appendChild(doc.createTextNode(text));
                    message.appendChild(textEl);

                    Element recipientEl = doc.createElement("recipient");
                    recipientEl.appendChild(doc.createTextNode(recipient));
                    message.appendChild(recipientEl);

                    rootElement.appendChild(message);
                    //prettyPrint(doc);
                    BigInteger modulus = new BigInteger(publicKeyString, 16);
                    BigInteger privateExp = new BigInteger(privateKeyString, 16);
                    BigInteger publicExponent = new BigInteger(publicExponentString, 16);
                    
                    RSAPrivateKeySpec privSpec = new RSAPrivateKeySpec(modulus, privateExp);
                    RSAPublicKeySpec pubSpec = new RSAPublicKeySpec(modulus, publicExponent);
                    
                    KeyFactory kf = KeyFactory.getInstance("RSA");
                    
                    RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(privSpec);
                    RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(pubSpec);
                    
                    DOMSignContext dsc = new DOMSignContext(privateKey, doc.getElementsByTagName("message").item(0));
                    XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
                    Reference ref = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA256, null),
                            Collections.singletonList(fac.newTransform(Transform.ENVELOPED,
                                    (TransformParameterSpec) null)), null, null);

                    SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,
                            (C14NMethodParameterSpec) null),
                            fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                            Collections.singletonList(ref));
                    KeyInfoFactory kif = fac.getKeyInfoFactory();
                    KeyValue kv = kif.newKeyValue(publicKey);
                    KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));
                    XMLSignature signature = fac.newXMLSignature(si, ki);
                    signature.sign(dsc);
                    
                    prettyPrint(doc);
                    
                    URL url = new URL(SMSGateUrl);

                    URLConnection conn = url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    try (OutputStream out = conn.getOutputStream()) {

                        Transformer tf = TransformerFactory.newInstance().newTransformer();
                        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                        tf.setOutputProperty(OutputKeys.INDENT, "yes");
                        tf.transform(new DOMSource(doc), new StreamResult(out));
                        out.flush();
                    }

                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document docResponse = builder.parse((conn.getInputStream()));
                    prettyPrint(docResponse);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    public static final void prettyPrint(Document xml) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        //Writer out = new StringWriter();
        tf.transform(new DOMSource(xml), new StreamResult(System.out));
        //System.out.println(out.toString());
    }
}
