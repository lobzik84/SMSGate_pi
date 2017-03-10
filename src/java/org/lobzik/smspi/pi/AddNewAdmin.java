/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.util.HashMap;
import javax.xml.bind.DatatypeConverter;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *Класс  генерирует данные для добавления нового админа в БД.
 * @author konstantin makarov
 * 
 */
public class AddNewAdmin {

    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String login = "makarov"; // сюда пишем логин нового админа
        String password = "m@k@r0v";//сюда пишем пароль нового админа

        if (login.trim().length() > 0 && password.trim().length() > 0) {
            String salt = getSalt();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((password + ":" + salt).getBytes("UTF-8"));
            String saltedHash = DatatypeConverter.printHexBinary(hash);
            System.out.println("Логин: " + login);
            System.out.println("Соль: " + salt);
            System.out.println("Хэш: " + saltedHash);
        }
    }

    private static String getSalt() {
        String sourse = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_";
        char[] sourseArray = sourse.toCharArray();
        StringBuilder saltBuilder = new StringBuilder();
        for (int i = 0; i < 15; ++i) {
            int randomIndex = new SecureRandom().nextInt(63);
            saltBuilder.append(sourseArray[randomIndex]);
        }
        return saltBuilder.toString();
    }

}
