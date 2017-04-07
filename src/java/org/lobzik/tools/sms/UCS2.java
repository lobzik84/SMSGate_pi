/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.tools.sms;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 *
 * @author lobzik
 */
public class UCS2 {

    public HashMap alphabet = new HashMap();

    public UCS2() {
        alphabet.put("0410", "А");
        alphabet.put("0411", "Б");
        alphabet.put("0412", "В");
        alphabet.put("0413", "Г");
        alphabet.put("0414", "Д");
        alphabet.put("0415", "Е");
        alphabet.put("0416", "Ж");
        alphabet.put("0417", "З");
        alphabet.put("0418", "И");
        alphabet.put("0419", "Й");
        alphabet.put("041A", "К");
        alphabet.put("041B", "Л");
        alphabet.put("041C", "М");
        alphabet.put("041D", "Н");
        alphabet.put("041E", "О");
        alphabet.put("041F", "П");
        alphabet.put("0420", "Р");
        alphabet.put("0421", "С");
        alphabet.put("0422", "Т");
        alphabet.put("0423", "У");
        alphabet.put("0424", "Ф");
        alphabet.put("0425", "Х");
        alphabet.put("0426", "Ц");
        alphabet.put("0427", "Ч");
        alphabet.put("0428", "Ш");
        alphabet.put("0429", "Щ");
        alphabet.put("042A", "Ъ");
        alphabet.put("042B", "Ы");
        alphabet.put("042C", "Ь");
        alphabet.put("042D", "Э");
        alphabet.put("042E", "Ю");
        alphabet.put("042F", "Я");
        alphabet.put("0430", "а");
        alphabet.put("0431", "б");
        alphabet.put("0432", "в");
        alphabet.put("0433", "г");
        alphabet.put("0434", "д");
        alphabet.put("0435", "е");
        alphabet.put("0436", "ж");
        alphabet.put("0437", "з");
        alphabet.put("0438", "и");
        alphabet.put("0439", "й");
        alphabet.put("043A", "к");
        alphabet.put("043B", "л");
        alphabet.put("043C", "м");
        alphabet.put("043D", "н");
        alphabet.put("043E", "о");
        alphabet.put("043F", "п");
        alphabet.put("0440", "р");
        alphabet.put("0441", "с");
        alphabet.put("0442", "т");
        alphabet.put("0443", "у");
        alphabet.put("0444", "ф");
        alphabet.put("0445", "х");
        alphabet.put("0446", "ц");
        alphabet.put("0447", "ч");
        alphabet.put("0448", "ш");
        alphabet.put("0449", "щ");
        alphabet.put("044A", "ъ");
        alphabet.put("044B", "ы");
        alphabet.put("044C", "ь");
        alphabet.put("044D", "э");
        alphabet.put("044E", "ю");
        alphabet.put("044F", "я");
        alphabet.put("0401", "Ё");
        alphabet.put("0451", "ё");
        alphabet.put("002E", ".");
        alphabet.put("002C", ",");
        alphabet.put("0021", "!");
        alphabet.put("0022", "\"");
        alphabet.put("2116", "№");
        alphabet.put("003B", ";");
        alphabet.put("0025", "%");
        alphabet.put("003A", ": ");
        alphabet.put("003F", "?");
        alphabet.put("002A", "*");
        alphabet.put("0028", "(");
        alphabet.put("0029", ");");
        alphabet.put("002F", "/");
        alphabet.put("0030", "0");
        alphabet.put("0031", "1");
        alphabet.put("0032", "2");
        alphabet.put("0033", "3");
        alphabet.put("0034", "4");
        alphabet.put("0035", "5");
        alphabet.put("0036", "6");
        alphabet.put("0037", "7");
        alphabet.put("0038", "8");
        alphabet.put("0039", "9");
        alphabet.put("002B", "+");
        alphabet.put("002D", "-");
        alphabet.put("003D", "=");
        alphabet.put("2C00", ",");
        alphabet.put("0020", " ");
        alphabet.put("4100", "A");
        alphabet.put("4200", "B");
        alphabet.put("4300", "C");
        alphabet.put("4400", "D");
        alphabet.put("4500", "E");
        alphabet.put("4600", "F");
        alphabet.put("4700", "G");
        alphabet.put("4800", "H");
        alphabet.put("4900", "I");
        alphabet.put("4A00", "J");
        alphabet.put("4B00", "K");
        alphabet.put("4C00", "L");
        alphabet.put("4D00", "M");
        alphabet.put("4E00", "N");
        alphabet.put("4F00", "O");
        alphabet.put("5000", "P");
        alphabet.put("5100", "Q");
        alphabet.put("5200", "R");
        alphabet.put("5300", "S");
        alphabet.put("5400", "T");
        alphabet.put("5500", "U");
        alphabet.put("5600", "V");
        alphabet.put("5700", "W");
        alphabet.put("5800", "X");
        alphabet.put("5900", "Y");
        alphabet.put("5A00", "Z");
        alphabet.put("6100", "a");
        alphabet.put("6200", "b");
        alphabet.put("6300", "c");
        alphabet.put("6400", "d");
        alphabet.put("6500", "e");
        alphabet.put("6600", "f");
        alphabet.put("6700", "g");
        alphabet.put("6800", "h");
        alphabet.put("6900", "i");
        alphabet.put("6A00", "j");
        alphabet.put("6B00", "k");
        alphabet.put("6C00", "l");
        alphabet.put("6D00", "m");
        alphabet.put("6E00", "n");
        alphabet.put("6F00", "o");
        alphabet.put("7000", "p");
        alphabet.put("7100", "q");
        alphabet.put("7200", "r");
        alphabet.put("7300", "s");
        alphabet.put("7400", "t");
        alphabet.put("7500", "u");
        alphabet.put("7600", "v");
        alphabet.put("7700", "w");
        alphabet.put("7800", "x");
        alphabet.put("7900", "y");
        alphabet.put("7A00", "z");
    }

    public String decode(String input) {
        String result = "";
        int i = 0;
        try {
            for (i = 0; i < input.length(); i += 4) {
                String sstr = input.substring(i, i + 4).toUpperCase();
                String c = "";
                try {
                    c = (String) alphabet.get(sstr);
                } catch (Exception e) {
                }
                if (c != null) {
                    result += c;
                }
            }

        } catch (Exception ee) {

        }
        return result;
    }

    public static String encodeAsUCS2(String test) throws UnsupportedEncodingException {

        byte[] bytes = test.getBytes("UTF-16BE");

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();

    }

    public static String ucs2ToUTF8(byte[] ucs2Bytes) throws UnsupportedEncodingException {
        String unicode = new String(ucs2Bytes, "UTF-16");
        String utf8 = new String(unicode.getBytes("UTF-8"), "Cp1251");
        return utf8;
    }
}
