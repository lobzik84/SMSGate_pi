/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Properties;
import org.lobzik.tools.RSATools;
import org.lobzik.tools.Tools;

/**
 * DO NOT USE THIS CLASS ON SERVER PACKAGE!
 * @author lobzik
 */
public class BoxCommonData {

    public static final String dataSourceName = "jdbc/hs";
    
    public static final String BOX_PROPERTIES_FILE = "/etc/box.properties";
    public static final Integer BOX_ID;
    public static final String BOX_VERSION;
    public static final String BOX_ID_FILE;
    public static final String PUBLIC_KEY_FILE;
    public static final String PRIVATE_KEY_FILE;
    public static final String HOSTAPD_CONFIG_FILE;
    public static final String TUNNEL_SERVER_URL;
    public static final String REGISTER_SERVER_URL;
    public static final String SSID;
    public static final String WPA_PSK;
    public static final RSAPublicKey PUBLIC_KEY;
    public static final RSAPrivateKey PRIVATE_KEY;
    public static final String SERIAL_PORT;
    public static final String MODEM_INFO_PORT;
    public static final BigInteger RSA_E;
            

    
    private BoxCommonData() {
    }

    static { //Init from files
        HashMap<String, Object> settingsMap = new HashMap();
        try {

            Properties props = new Properties();
            props.load(new FileInputStream(BoxCommonData.BOX_PROPERTIES_FILE));
            settingsMap.put("SERIAL_PORT", props.getProperty("serial_port"));
            settingsMap.put("MODEM_INFO_PORT", props.getProperty("modem_info_port"));
            settingsMap.put("PRIVATE_KEY_FILE", props.getProperty("private_key_file"));
            settingsMap.put("TUNNEL_SERVER_URL", props.getProperty("tunnel_server_url"));
            settingsMap.put("REGISTER_SERVER_URL", props.getProperty("register_server_url"));
            settingsMap.put("BOX_VERSION", props.getProperty("box_version"));
            String privateKeyFileName = props.getProperty("private_key_file");
            String publicKeyFileName = props.getProperty("public_key_file");
            settingsMap.put("PUBLIC_KEY_FILE", publicKeyFileName);
            //settingsMap.put("PUBLIC_KEY", new String(Files.readAllBytes(Paths.get(publicKeyFileName)), "UTF-8"));
            
            settingsMap.put("RSA_E", new BigInteger(props.getProperty("rsa_e")));

            String hostapdConfFileName = props.getProperty("hostapd_conf_file");
            settingsMap.put("HOSTAPD_CONFIG_FILE", hostapdConfFileName);
            props.load(new FileInputStream(hostapdConfFileName));
            settingsMap.put("SSID", props.getProperty("ssid"));
            settingsMap.put("WPA_PSK", props.getProperty("wpa_passphrase"));

            String boxIdFileName = props.getProperty("id_file");
            settingsMap.put("BOX_ID_FILE", boxIdFileName);
            File boxIdFile = new File(boxIdFileName);

            if (boxIdFile.exists()) {
                props = new Properties();
                props.load(new FileInputStream(boxIdFileName));
                settingsMap.put("BOX_ID", props.getProperty("box_id"));
            }
            
            RSAPrivateKey privateKey =  RSATools.getPrivateKey(new String(Files.readAllBytes(Paths.get(privateKeyFileName)), "UTF-8"));
            RSAPublicKey publicKey = RSATools.getPublicKey(privateKey);
            
            settingsMap.put("PRIVATE_KEY", privateKey);
            settingsMap.put("PUBLIC_KEY", publicKey);
                        
        } catch (Throwable t) {
            System.err.println("Fatal error while initializing!");
            t.printStackTrace();
            System.exit(-1);

        }

        BOX_ID = Tools.parseInt(settingsMap.get("BOX_ID"), 0);
        BOX_ID_FILE = (String)settingsMap.get("BOX_ID_FILE");
        BOX_VERSION = (String)settingsMap.get("BOX_VERSION");
        PUBLIC_KEY_FILE = (String)settingsMap.get("PUBLIC_KEY_FILE");
        PRIVATE_KEY_FILE = (String)settingsMap.get("PRIVATE_KEY_FILE");
        HOSTAPD_CONFIG_FILE = (String)settingsMap.get("HOSTAPD_CONFIG_FILE");
        TUNNEL_SERVER_URL = (String)settingsMap.get("TUNNEL_SERVER_URL");
        REGISTER_SERVER_URL = (String)settingsMap.get("REGISTER_SERVER_URL");
        SSID = (String)settingsMap.get("SSID");
        WPA_PSK = (String)settingsMap.get("WPA_PSK");
        PUBLIC_KEY = (RSAPublicKey)settingsMap.get("PUBLIC_KEY");
        PRIVATE_KEY = (RSAPrivateKey)settingsMap.get("PRIVATE_KEY");    
        SERIAL_PORT = (String)settingsMap.get("SERIAL_PORT");
        MODEM_INFO_PORT = (String)settingsMap.get("MODEM_INFO_PORT");
        RSA_E = (BigInteger)settingsMap.get("RSA_E");
        //TEST_MODE = false;
    }
}
