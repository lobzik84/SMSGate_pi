/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.tests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.lobzik.tools.Tools;

/**
 *
 * @author lobzik
 */
public class RegexTest {
    public static final String regex = "[0-9\\.]+ *р\\.";//будет в настройках
    public static final String replacer = "р.";//будет в настройках
    
    public static void main(String[] args) {
        /*String text = "Блабла 5588 Ваш баланс 3322.54 р. ололо, ололо!!5678.9тт Лучшие в городе пепяки от 66.8 р. 20 коп.!!";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String sVal = matcher.group(0);
            System.out.println(sVal);
            sVal = sVal.replaceAll(replacer, "").trim();
            System.out.println(sVal);
            double doubleVal = Tools.parseDouble(sVal, 0);
            System.out.println(doubleVal);
        }*/
        String mask="+7 (___) ___-__-__";
        System.out.println(Tools.maskPhone("+79263357107", mask));
        System.out.println(Tools.maskPhone("+7926335-7107", mask));
        System.out.println(Tools.maskPhone("+7 (926) 335-71-07", mask));            
        System.out.println(Tools.maskPhone("+7(9263357107", mask));
        System.out.println(Tools.maskPhone("+7(926)335-71-07", mask));   
                System.out.println(Tools.maskPhone("fgd335-71-07", mask));
                        System.out.println(Tools.maskPhone("", mask));
         System.out.println(Tools.maskPhone("112", mask));
    }
}
