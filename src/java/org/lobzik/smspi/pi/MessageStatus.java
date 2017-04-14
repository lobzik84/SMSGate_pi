/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi;

import org.lobzik.tools.Tools;

/**
 *
 * @author lobzik
 */
public class MessageStatus {

    public static final int STATUS_NEW = 0;
    public static final int STATUS_SENT = 1;
    public static final int STATUS_READ = 2;
    public static final int STATUS_ERROR_SENDING = -1;
    public static final int STATUS_ERROR_TOO_OLD = -2;
    public static final int STATUS_ERROR_ATTEMPTS_EXCEEDED = -3;
    public static final int STATUS_SENDING = 3;

    int statusInt;

    public MessageStatus(int status) {
        statusInt = status;
    }

    public MessageStatus(Object status) {
        statusInt = Tools.parseInt(status, Integer.MAX_VALUE);
    }

    public String rus() {
        switch (statusInt) {

            case STATUS_NEW:
                return "Новое";
            case STATUS_SENT:
                return "Отправлено";
            case STATUS_READ:
                return "Прочитано";
            case STATUS_ERROR_SENDING:
                return "Ошибка отправления";
            case STATUS_ERROR_TOO_OLD:
                return "Слишком старая";
            case STATUS_ERROR_ATTEMPTS_EXCEEDED:
                return "Превышено количество попыток отправки";
            case STATUS_SENDING:
                return "Отправляется";
            default:
                return "";
        }
    }

    public String eng() {
        switch (statusInt) {

            case STATUS_NEW:
                return "New";
            case STATUS_SENT:
                return "Sent";
            case STATUS_READ:
                return "Read";
            case STATUS_ERROR_SENDING:
                return "Error sending";
            case STATUS_ERROR_TOO_OLD:
                return "Message is Too Old";
            case STATUS_ERROR_ATTEMPTS_EXCEEDED:
                return "Send Attempts Exceeded";
            case STATUS_SENDING:
                return "Sending";
            default:
                return "";
        }
    }

    public String CSSClass() {
        switch (statusInt) {

            case STATUS_NEW:
                return "new";
            case STATUS_SENT:
                return "sent";
            case STATUS_READ:
                return "read";
            case STATUS_ERROR_SENDING:
                return "error";
            case STATUS_ERROR_TOO_OLD:
                return "sad_pedobear";
            case STATUS_ERROR_ATTEMPTS_EXCEEDED:
                return "exceeded";
            case STATUS_SENDING:
                return "sending";
            default:
                return "";
        }
    }
}
