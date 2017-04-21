/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.inet.ldap.LdapConfig;
import org.inet.ldap.LdapConnection;
import org.inet.ldap.com.LdapTools;
import org.inet.ldap.entity.LdapReader;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.entity.Parameter;
import org.lobzik.smspi.pi.event.Event;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author lobzik
 */
@WebServlet(name = "AdmServlet", urlPatterns = {"/adm", "/adm/*"})
public class PiServlet extends HttpServlet {

    static Logger log = null;

    static {
        if (log == null) {
            String MODULE_NAME = PiServlet.class.getClass().getSimpleName();
            log = Logger.getLogger(MODULE_NAME);
            Appender appender = ConnJDBCAppender.getAppenderInstance(AppData.dataSource, MODULE_NAME);
            log.addAppender(appender);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        String localPathInfo = Tools.getStringValue(request.getPathInfo(), "");
        localPathInfo = localPathInfo.replace("/", "");

        HashMap jspData = new HashMap();
        Parameter p = AppData.parametersStorage.getParameterByAlias("MODEM_RSSI");
        Measurement m = AppData.measurementsCache.getLastMeasurement(p);
        int rssi = -101;//сигнал сети, дБ. <100 нет фишек, 100<rssi<90 одна фишка, 90<rssi<80 две фишки, 80<rssi<70 три фишки, >70 четыре фишки        
        if (m != null) {
            rssi = (int) (double) m.getDoubleValue();
        }
        jspData.put("RSSI", rssi);
        String baseUrl = request.getContextPath() + request.getServletPath();
        int loginAdmin = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);

        switch (localPathInfo) {
            case "login":
                if (loginAdmin < 0) {
                    if (request.getMethod().equalsIgnoreCase("POST")) {
                        String adminLogin = request.getParameter("login");
                        String adminPass = request.getParameter("pass");
                        if (adminLogin != null && adminPass != null && adminPass.trim().length() > 0 && adminPass.trim().length() > 0) {
                            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                                String loginSql = "select * from admins a where a.login = ? and status = 1";
                                LinkedList args = new LinkedList();
                                args.add(adminLogin);
                                List<HashMap> res = DBSelect.getRows(loginSql, args, conn);
                                if (res.size() == 1) {
                                    HashMap h = res.get(0);
                                    int id = Tools.parseInt(h.get("admin_id"), 0);
                                    boolean authViaLDAP = Tools.parseInt(h.get("auth_via_ldap"), 0) == 1;
                                    if (authViaLDAP) {
                                        log.info("Authorizing admin id=" + id + " via LDAP");
                                        String[] userLoginArr = LdapTools.splitUsername(adminLogin);
                                        final String un = userLoginArr[0];
                                        LdapReader reader = LdapConfig.getReader(userLoginArr[1]);
                                        LdapReader userReader = new LdapReader.Builder(un, adminPass, reader.getDomainName()).setServerIp(reader.getServerIp()).build();
                                        if (LdapConnection.checkAuthorization(userReader)) {

                                            loginAdmin = id;
                                            log.info("Admin login ok! id=" + loginAdmin + ", ip=" + request.getRemoteAddr());
                                            request.getSession().setAttribute("AdminID", loginAdmin);
                                            request.getSession().setAttribute("AdminLogin", adminLogin);
                                            response.sendRedirect(baseUrl + "/main");
                                        } else {
                                            log.error("Incorrect password for admin " + adminLogin);
                                            RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/jsp/login.jsp");
                                            jspData.put("FAIL_LOGIN", 1);
                                            request.setAttribute("JspData", jspData);
                                            disp.include(request, response);
                                        }

                                    } else {
                                        String salt = (String) h.get("salt");
                                        String dbHash = (String) h.get("hash");
                                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                                        byte[] hash = digest.digest((adminPass + ":" + salt).getBytes("UTF-8"));
                                        String saltedHash = DatatypeConverter.printHexBinary(hash);
                                        if (dbHash.equals(saltedHash)) {
                                            if (Tools.parseInt(h.get("status"), -1) == 1) {
                                                loginAdmin = id;
                                                log.info("Admin login ok! id=" + loginAdmin + ", ip=" + request.getRemoteAddr());
                                                request.getSession().setAttribute("AdminID", loginAdmin);
                                                request.getSession().setAttribute("AdminLogin", adminLogin);
                                                response.sendRedirect(baseUrl + "/main");
                                            } else {
                                                log.error("Admin:" + adminLogin + "is not activated");
                                                RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/jsp/login.jsp");
                                                jspData.put("FAIL_LOGIN", 1);
                                                request.setAttribute("JspData", jspData);
                                                disp.forward(request, response);
                                            }
                                        } else {
                                            log.error("Incorrect password for admin " + adminLogin);
                                            RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/jsp/login.jsp");
                                            jspData.put("FAIL_LOGIN", 1);
                                            request.setAttribute("JspData", jspData);
                                            disp.include(request, response);
                                        }
                                    }

                                } else {
                                    log.error("Admin with login " + adminLogin + " not found");
                                    RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/jsp/login.jsp");
                                    jspData.put("FAIL_LOGIN", 1);
                                    request.setAttribute("JspData", jspData);
                                    disp.include(request, response);
                                }

                            } catch (Exception e) {
                                log.error("Error while authenticating " + e.getMessage());
                                e.printStackTrace();
                            }

                        } else {
                            response.sendRedirect(baseUrl);
                        }
                    } else {
                        RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/jsp/login.jsp");
                        disp.include(request, response);
                    }
                } else {
                    log.error("Registration for admin: " + loginAdmin + " is alive");
                    response.sendRedirect(baseUrl + "/main");
                }
                break;

            case "main":
                if (loginAdmin > 0) {
                    log.info("Registration for admin: " + loginAdmin + " is alive");
                    try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                        Long msgSent = DBSelect.getCount("select count(*) as cnt from sms_outbox where status = " + MessageStatus.STATUS_SENT, "cnt", null, conn);
                        Long msgSentDaily = DBSelect.getCount("select count(*) as cnt from sms_outbox where status = " + MessageStatus.STATUS_SENT + " and date_sent > concat (current_date, ' 00:00:00') and date_sent < concat (current_date ,' 23:59:59')", "cnt", null, conn);
                        Long msgErrs = DBSelect.getCount("select count(*) as cnt from sms_outbox where status in (" + MessageStatus.STATUS_ERROR_SENDING + ", " + MessageStatus.STATUS_ERROR_TOO_OLD + ", " + MessageStatus.STATUS_ERROR_ATTEMPTS_EXCEEDED + ")", "cnt", null, conn);
                        Long msgInbox = DBSelect.getCount("select count(*) as cnt from sms_inbox", "cnt", null, conn);
                        jspData.put("msgSent", msgSent);
                        jspData.put("msgSentDaily", msgSentDaily);
                        jspData.put("msgErrs", msgErrs);
                        jspData.put("msgInbox", msgInbox);
                        String logSql = "select * from logs\n"
                                + "order by dated desc\n"
                                + "limit 100";
                        List<HashMap> logData = DBSelect.getRows(logSql, null, conn);
                        jspData.put("logData", logData);
                        jspData.put("head_url", "main");
                        String messageListSql = "select a.*, u.name from \n"
                                + "(select si.id, si.message, si.sender as tel_no, 'inbox' as type, si.date, si.status, null as user_id from sms_inbox si\n"
                                + "union \n"
                                + "select so.id, so.message, so.recipient as tel_no, 'outbox' as type, so.date, so.status, so.user_id from sms_outbox so) a\n"
                                + " left join users u on u.id = a.user_id "
                                + " order by a.date desc\n"
                                + "limit 100";
                        List<HashMap> messageList = DBSelect.getRows(messageListSql, null, conn);
                        jspData.put("messageList", messageList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/jsp/main.jsp");
                    request.setAttribute("JspData", jspData);
                    disp.include(request, response);
                } else {
                    log.error("No alive registration");
                    response.sendRedirect(baseUrl);
                }
                break;
            case "logout":
                request.getSession().removeAttribute("AdminID");
                request.getSession().removeAttribute("AdminLogin");
                response.sendRedirect(baseUrl);
                break;
            case "sendmsg":
                if (loginAdmin > 0) {
                    String sms = Tools.getStringValue(request.getParameter("sms"), "");
                    String recipient = Tools.getStringValue(request.getParameter("recipient"), "");
                    sms = Tools.replaceTags(sms);
                    recipient = Tools.replaceTags(recipient);
                    recipient = recipient.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\\-", "").replaceAll(" ", "");
                    if (request.getMethod().equalsIgnoreCase("POST") && sms.trim().length() > 0 && recipient.trim().length() > 0) {
                        HashMap data = new HashMap();
                        data.put("message", sms);
                        data.put("recipient", recipient);
                        Event e = new Event("send_sms", data, Event.Type.USER_ACTION);
                        AppData.eventManager.newEvent(e);
                        response.sendRedirect(baseUrl + "/main");
                    } else {
                        response.sendRedirect(baseUrl + "/main");
                    }
                } else {
                    response.sendRedirect(baseUrl);
                }

                break;
            case "addapp":
                if (loginAdmin > 0) {
                    HashMap reqData = getRequestParameters(request);
                    if (reqData.containsKey("REG_ME") && Tools.parseInt(reqData.get("REG_ME"), -1) > 0 && Tools.getStringValue(reqData.get("name"), "").trim().length() > 0 && Tools.getStringValue(reqData.get("public_key"), "").trim().length() > 0) {
                        String publicKey = Tools.getStringValue(reqData.get("public_key"), "");
                        if (publicKey.length() <= 256) {
                            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                                int newUserId = DBTools.insertRow("users", Tools.replaceTags(reqData), conn);
                                response.sendRedirect(baseUrl + "/addapp");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            response.sendRedirect(baseUrl + "/addapp");
                        }
                    } else if (reqData.containsKey("removeApp") && Tools.parseInt(reqData.get("removeApp"), -1) > 0 && reqData.containsKey("id") && Tools.parseInt(reqData.get("id"), -1) > 0) {
                        int removeId = Tools.parseInt(reqData.get("id"), -1);
                        HashMap removeData = new HashMap();
                        removeData.put("id", removeId);
                        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                            DBTools.deleteRow("users", Tools.replaceTags(removeData), conn);
                            response.sendRedirect(baseUrl + "/addapp");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        List<HashMap> appList = new ArrayList<>();
                        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                            String appListSQL = "select*from users\n"
                                    + "order by id";
                            appList = DBSelect.getRows(appListSQL, conn);
                            jspData.put("APP_LIST", appList);
                            jspData.put("head_url", "addapp");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/jsp/add_apl.jsp");
                        request.setAttribute("JspData", jspData);
                        disp.include(request, response);
                    }
                } else {
                    response.sendRedirect(baseUrl);
                }
                break;
            case "addadm":
                if (loginAdmin > 0) {
                    if (loginAdmin == 1) {
                        HashMap reqData = Tools.replaceTags(getRequestParameters(request));
                        if (reqData.containsKey("ADD_ME") && Tools.parseInt(reqData.get("ADD_ME"), -1) > 0 && Tools.getStringValue(reqData.get("login"), "").trim().length() > 0 && Tools.getStringValue(reqData.get("password"), "").trim().length() > 0) {
                            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                                String password = Tools.getStringValue(reqData.get("password"), "");
                                String salt = getSalt();
                                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                                byte[] hash = digest.digest((password + ":" + salt).getBytes("UTF-8"));
                                String saltedHash = DatatypeConverter.printHexBinary(hash);
                                reqData.put("salt", salt);
                                reqData.put("hash", saltedHash);
                                reqData.put("status", 1);
                                int newAdminId = DBTools.insertRow("admins", reqData, conn);
                                response.sendRedirect(baseUrl + "/addadm");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (reqData.containsKey("removeAdm") && Tools.parseInt(reqData.get("removeAdm"), -1) > 0 && reqData.containsKey("id") && Tools.parseInt(reqData.get("id"), -1) > 1) {
                            int removeId = Tools.parseInt(reqData.get("id"), -1);
                            HashMap removeData = new HashMap();
                            removeData.put("admin_id", removeId);
                            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                                DBTools.deleteRow("admins", removeData, conn);
                                response.sendRedirect(baseUrl + "/addadm");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            List<HashMap> admList = new ArrayList<>();
                            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                                String appListSQL = "select*from admins\n"
                                        + "order by admin_id";
                                admList = DBSelect.getRows(appListSQL, conn);
                                jspData.put("ADM_LIST", admList);
                                jspData.put("head_url", "addadm");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/jsp/admins.jsp");
                            request.setAttribute("JspData", jspData);
                            disp.include(request, response);
                        }
                    } else {
                        RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/jsp/admins.jsp");
                        jspData.put("NOT_ROOT_ADMIN", 1);
                        request.setAttribute("JspData", jspData);
                        disp.include(request, response);
                    }
                } else {
                    response.sendRedirect(baseUrl);
                }
                break;
            case "msgs":
                if (loginAdmin > 0) {
                    HashMap reqData = Tools.replaceTags(getRequestParameters(request));
                    String msgsListSQL = "select a.*, u.name from \n"
                            + "(select si.id, si.message, si.sender as tel_no, 'inbox' as type, si.date, si.status, null as user_id from sms_inbox si\n"
                            + "union \n"
                            + "select so.id, so.message, so.recipient as tel_no, 'outbox' as type, so.date, so.status, so.user_id from sms_outbox so) a\n"
                            + " left join users u on u.id = a.user_id ";
                    if (reqData.containsKey("FLTR_DATA") && Tools.parseInt(reqData.get("FLTR_DATA"), -1) == 1) {
                        String whereString = "where 1=1 ";
                        HashMap filterList = new HashMap();
                        LinkedList args = new LinkedList();

                        Date from = Tools.parseDate((String) reqData.get("date_from"));
                        Date to = Tools.parseDate((String) reqData.get("date_to"));
                        if (from != null) {
                            String fromS = Tools.getFormatedDate(from, "dd.MM.yyyy");
                            whereString += " and a.date >= str_to_date(?,'%d.%m.%Y %H:%i:%s')";
                            args.add(fromS + " 00:00:00");
                            filterList.put("date_from", fromS);
                        } else {
                            filterList.put("date_from", " ");
                        }
                        if (to != null) {
                            String toS = Tools.getFormatedDate(to, "dd.MM.yyyy");
                            whereString += " and a.date <= str_to_date(? ,'%d.%m.%Y %H:%i:%s')";
                            args.add(toS + " 23:59:59");
                            filterList.put("date_to", toS);
                        } else {
                            filterList.put("date_to", " ");
                        }

                        String searchText = Tools.getStringValue(reqData.get("search_text"), "");
                        String telNo = Tools.getStringValue(reqData.get("tel_no"), "");

                        if (searchText.trim().length() > 0) {
                            whereString += " and a.message like (?)";
                            args.add("%" + searchText + "%");
                            filterList.put("search_text", searchText);
                        }

                        if (telNo.trim().length() > 0) {
                            whereString += " and a.tel_no like (?)";
                            args.add("%" + telNo + "%");
                            filterList.put("tel_no", telNo);
                        }

                        jspData.put("FILTER_LIST", filterList);

                        List<HashMap> msgsList = new ArrayList<>();
                        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {

                            msgsListSQL += whereString + "\n" + "order by a.date desc\n";
                            msgsList = DBSelect.getRows(msgsListSQL, args, conn);
                            jspData.put("MSGS_LIST", msgsList);
                            jspData.put("head_url", "msgs");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/jsp/msgs.jsp");
                        request.setAttribute("JspData", jspData);
                        disp.include(request, response);

                    } else {
                        List<HashMap> msgsList = new ArrayList<>();
                        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                            msgsListSQL += "order by a.date desc\n"
                                    + "limit 200";
                            msgsList = DBSelect.getRows(msgsListSQL, conn);
                            jspData.put("MSGS_LIST", msgsList);
                            jspData.put("head_url", "msgs");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/jsp/msgs.jsp");
                        request.setAttribute("JspData", jspData);
                        disp.include(request, response);
                    }
                } else {
                    response.sendRedirect(baseUrl);
                }
                break;
            case "chpass":
                if (loginAdmin > 0) {
                    HashMap reqData = Tools.replaceTags(getRequestParameters(request));
                    if (loginAdmin == 1 || loginAdmin == Tools.parseInt(reqData.get("TARGET_ADMIN_ID"), -1)) { // меняем пароль, если сидим по root'ом или если id залогиненого админа совпадает  с id целевого админа для изменения пароля
                        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                            String password = Tools.getStringValue(reqData.get("password"), "");
                            int targetAdminId = Tools.parseInt(reqData.get("TARGET_ADMIN_ID"), -1);
                            String salt = getSalt();
                            MessageDigest digest = MessageDigest.getInstance("SHA-256");
                            byte[] hash = digest.digest((password + ":" + salt).getBytes("UTF-8"));
                            String saltedHash = DatatypeConverter.printHexBinary(hash);
                            if (targetAdminId > 0) {
                                reqData.put("salt", salt);
                                reqData.put("hash", saltedHash);
                                reqData.put("admin_id", targetAdminId);
                                DBTools.updateRow("admins", reqData, conn);
                            }
                            request.getSession().setAttribute("PASS_CHANGED", 1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        response.sendRedirect(baseUrl + "/addadm");
                    } else {
                        request.getSession().setAttribute("ACCESS_ERROR", 1);
                        response.sendRedirect(baseUrl + "/addadm");
                    }
                } else {
                    response.sendRedirect(baseUrl);
                }
                break;
            default:
                if (loginAdmin > 0) {
                    response.sendRedirect(baseUrl + "/main");
                } else {
                    RequestDispatcher disp1 = request.getSession().getServletContext().getRequestDispatcher("/jsp/login.jsp");
                    disp1.include(request, response);
                }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private HashMap getRequestParameters(HttpServletRequest request) {
        HashMap parameters = new HashMap();
        Enumeration keys = request.getParameterNames();

        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            String vals[] = request.getParameterValues(key);
            if (vals.length == 1) {
                parameters.put(key, vals[0]);
            } else if (vals.length > 1) {
                parameters.put(key, vals);
            }
        }

        return parameters;
    }

    /*
    *Метод генерирует "соль" для рассчета хэш суммы "засоленого" пароля
     */
    private String getSalt() {
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
