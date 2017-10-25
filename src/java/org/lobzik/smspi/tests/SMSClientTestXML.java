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

    //static String SMSGateUrl = "http://smsity.rtrn.ru:8080/smspi/xml";
    static String SMSGateUrl = "http://localhost:8083/smspi/xml";
    static String username = "msk-hq-dms";
    static String publicKeyString = "61b9ea96509090e80b17419f156e6981bc953060027cf8fc4fa20d9cf9a58cb14d0855ad8b347bd865ab0b40319b040790ac736e05cd3034cbeda58c843ba13c563000ab99c49e132b55b78e54786670c7b59d56680f27ecd7038a81dac3372bac4b6e96fc42f53028d82689d779fcb370aa00401e84460d165a14e6b6f87e71";
    static String privateKeyString = "30264c00d79d02e8715e7a0489a8a6ef371d42826b776badd5068132908612b5e0c074865709fae2f9e4b65c609481fdc038577f23f89f01c46048aadd140c3272c68c073b2c71c62ea117ea078c46ece5b5a83f3d10f7527161891eb690565d433599edfcfbca273dc91c1d148a107c5602ac441b0df2faefc613dbf05bdae1";
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
