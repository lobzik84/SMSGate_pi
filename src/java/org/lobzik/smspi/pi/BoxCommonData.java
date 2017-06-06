/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi;

import java.math.BigInteger;


/**
 * DO NOT USE THIS CLASS ON SERVER PACKAGE!
 * @author lobzik
 */
public class BoxCommonData {

    public static final String dataSourceName = "jdbc/hs";
    
    public static final String MODEM_INFO_PORT = "/dev/ttyUSB0";
    public static final String SERIAL_PORT = "/dev/ttyS0";
    public static final BigInteger RSA_E = new BigInteger("65537");
    public static final boolean ON_PI = System.getProperty("os.arch").equalsIgnoreCase("arm");

    
    private BoxCommonData() {
    }


}
