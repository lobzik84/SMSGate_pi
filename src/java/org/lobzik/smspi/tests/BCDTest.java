/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.tests;

import java.util.HashMap;
import org.lobzik.tools.sms.CIncomingMessage;

/**
 *
 * @author lobzik
 */
public class BCDTest {

    public static void main(String[] args) {
        String engMsg = "07919730071111F1040B919730635343F40000716041211260210CBA23B1ECD681E8E5391D04";
        String rusMsg = "07919730071111F1040B919730635343F400087160412152402130003A043D04420432003A0020043F0440043E043204350440043A043000200440043004410441044B043B043A04380020";

        CIncomingMessage message = new CIncomingMessage(engMsg, 1);
        //HashMap dbMessage = new HashMap();
        System.out.println(message);//.getDate());
       // System.out.println(message.getOriginator());
       // System.out.println(message.getNativeText());
        System.out.println();
        System.out.println();
        System.out.println();
        CIncomingMessage message2 = new CIncomingMessage(rusMsg, 1);
        //HashMap dbMessage = new HashMap();
        System.out.println(message2);//.getDate());
        //System.out.println(message2.getOriginator());
       // System.out.println(message2.getNativeText());

    }
}
