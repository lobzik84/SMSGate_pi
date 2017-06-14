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
    public static final String logName = "PiServlet";

    static {
        if (log == null) {

            log = Logger.getLogger(logName);
            Appender appender = ConnJDBCAppender.getAppenderInstance(AppData.dataSource, logName);
            log.addAppender(appender);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        String localPathInfo = Tools.getStringValue(request.getPathInfo(), "");
        localPathInfo = localPathInfo.replace("/", "");
        try {
            HashMap jspData = new HashMap();
            Parameter p = AppData.parametersStorage.getParameterByAlias("MODEM_RSSI");
            Measurement m = AppData.measurementsCache.getLastMeasurement(p);
            int rssi = -101;//сигнал сети, дБ. <100 нет фишек, 100<rssi<90 одна фишка, 90<rssi<80 две фишки, 80<rssi<70 три фишки, >70 четыре фишки        
            if (m != null) {
                rssi = (int) (double) m.getDoubleValue();
            }
            jspData.put("RSSI", rssi);
            request.setAttribute("JspData", jspData);
            String baseUrl = request.getContextPath() + request.getServletPath();
            int loginAdmin = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
            switch (localPathInfo) {
                case "login":
                    if (loginAdmin < 0) {
                        loginAdmin(request, response);
                    } else {
                        log.info("Session is alive for admin: " + loginAdmin);
                        response.sendRedirect(baseUrl + "/main");
                    }
                    break;

                case "logout":
                    request.getSession().removeAttribute("AdminID");
                    request.getSession().removeAttribute("AdminLogin");
                    response.sendRedirect(baseUrl);
                    break;

                default:
                    if (loginAdmin > 0) {
                        jspData.put("head_url", localPathInfo);
                        switch (localPathInfo) {

                            case "main":
                                showMainPage(request, response);
                                break;

                            case "sendmsg":
                                sendMsg(request, response);
                                break;

                            case "addapp":
                                addApp(request, response);
                                break;

                            case "addadm":
                                addAdm(request, response);
                                break;

                            case "msgs":
                                doMsgs(request, response);
                                break;

                            case "chpass":
                                updAdmin(request, response);
                                break;

                            case "groups":
                                doGroups(request, response);
                                break;

                            case "addgroup":
                                addGroup(request, response);
                                break;

                            case "editgroup":
                                editGroup(request, response);
                                break;

                            case "delgroup":
                                deleteGroup(request, response);
                                break;

                            case "rcpnts":
                                doRcpnts(request, response);
                                break;

                            case "addrcpnt":
                                addRcpnt(request, response);
                                break;

                            case "editrcpnt":
                                editRcpnt(request, response);
                                break;

                            case "delrcpnt":
                                deleteRcpnt(request, response);
                                break;

                            default:
                                response.sendRedirect(baseUrl + "/main");
                        }
                    } else {
                        log.error("No alive session");
                        RequestDispatcher disp1 = request.getSession().getServletContext().getRequestDispatcher("/jsp/login.jsp");
                        disp1.include(request, response);
                    }
                    break;
            }

        } catch (Exception e) {
            log.error(e.getMessage());
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

    private void doGroups(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int loginAdmin = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
        HashMap jspData = (HashMap) request.getAttribute("JspData");
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            String sSQL = "select * from groups where admin_id=" + loginAdmin;
            List<HashMap> groups = DBSelect.getRows(sSQL, conn);
            jspData.put("GROUPS", groups);
        }
        RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/jsp/groups.jsp");
        request.setAttribute("JspData", jspData);
        disp.include(request, response);
    }

    private void addGroup(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int loginAdmin = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
        String baseUrl = request.getContextPath() + request.getServletPath();
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            HashMap reqData = Tools.replaceTags(getRequestParameters(request));
            reqData.put("admin_id", loginAdmin);
            DBTools.insertRow("groups", reqData, conn);
        }
        response.sendRedirect(baseUrl + "/groups");
    }

    private void editGroup(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int loginAdmin = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
        String baseUrl = request.getContextPath() + request.getServletPath();
        HashMap reqData = Tools.replaceTags(getRequestParameters(request));
        int groupId = Tools.parseInt(reqData.get("id"), -1);
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            String sSQL = "select id from groups where id=" + groupId + " and admin_id=" + loginAdmin;
            if (DBSelect.getRows(sSQL, conn).size() > 0) {
                DBTools.updateRow("groups", reqData, conn);
            }
        }
        response.sendRedirect(baseUrl + "/groups");
    }

    private void deleteGroup(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int loginAdmin = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
        String baseUrl = request.getContextPath() + request.getServletPath();
        HashMap reqData = Tools.replaceTags(getRequestParameters(request));
        int removeId = Tools.parseInt(reqData.get("id"), -1);
        if (removeId < 0) {
            return;
        }
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            DBSelect.executeStatement("delete from groups where id=" + removeId + " and admin_id=" + loginAdmin, null, conn);
        }
        response.sendRedirect(baseUrl + "/groups");
    }

    private void doRcpnts(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int loginAdmin = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
        HashMap jspData = (HashMap) request.getAttribute("JspData");
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            String sSQL = "select * from groups where admin_id=" + loginAdmin;
            List<HashMap> groups = DBSelect.getRows(sSQL, conn);
            jspData.put("GROUPS", groups);

            sSQL = "select g_rp.id id, g_rp.group_id group_id, g_rp.number number, g_rp.name, gr.group_name "
                    + " from group_recipients g_rp "
                    + " left join groups gr on gr.id = g_rp.group_id "
                    + " where g_rp.admin_id=" + loginAdmin;
            List<HashMap> rcpnts = DBSelect.getRows(sSQL, conn);
            jspData.put("RCPNTS", rcpnts);
        }
        RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/jsp/rcpnts.jsp");
        request.setAttribute("JspData", jspData);
        disp.include(request, response);
    }

    private void addRcpnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int loginAdmin = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
        String baseUrl = request.getContextPath() + request.getServletPath();
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            HashMap reqData = Tools.replaceTags(getRequestParameters(request));
            reqData.put("admin_id", loginAdmin);
            String number = Tools.unmaskPhone(Tools.getStringValue(reqData.get("number"), ""));
            reqData.put("number", number);
            int groupId = Tools.parseInt(reqData.remove("group_id"), 0);
            if (groupId > 0 ) {
                reqData.put("group_id", groupId);
            }
            DBTools.insertRow("group_recipients", reqData, conn);
        }
        response.sendRedirect(baseUrl + "/rcpnts");
    }

    private void editRcpnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int loginAdmin = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
        String baseUrl = request.getContextPath() + request.getServletPath();
        HashMap reqData = Tools.replaceTags(getRequestParameters(request));
        int rcpntId = Tools.parseInt(reqData.get("id"), -1);
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            String sSQL = "select id from group_recipients where id=" + rcpntId + " and admin_id=" + loginAdmin;
            if (DBSelect.getRows(sSQL, conn).size() > 0) {
                String number = Tools.unmaskPhone(Tools.getStringValue(reqData.get("number"), ""));
                reqData.put("number", number);
                DBTools.updateRow("group_recipients", reqData, conn);
            }
        }
        response.sendRedirect(baseUrl + "/rcpnts");
    }

    private void deleteRcpnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int loginAdmin = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
        String baseUrl = request.getContextPath() + request.getServletPath();
        HashMap reqData = Tools.replaceTags(getRequestParameters(request));
        int removeId = Tools.parseInt(reqData.get("id"), -1);
        if (removeId < 0) {
            return;
        }
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            DBSelect.executeStatement("delete from group_recipients where id=" + removeId + " and admin_id=" + loginAdmin, null, conn);
        }
        response.sendRedirect(baseUrl + "/rcpnts");
    }

    private void doMsgs(HttpServletRequest request, HttpServletResponse response) throws Exception {

        HashMap jspData = (HashMap) request.getAttribute("JspData");
        HashMap reqData = Tools.replaceTags(getRequestParameters(request));
        String msgsListSQL = "select a.*, u.name, adm.login from \n"
                + "(select si.id, si.message, si.sender as tel_no, 'inbox' as type, si.date, si.status, null as user_id, null as admin_id from sms_inbox si\n"
                + "union \n"
                + "select so.id, so.message, so.recipient as tel_no, 'outbox' as type, so.date, so.status, so.user_id, so.admin_id from sms_outbox so) a\n"
                + " left join users u on u.id = a.user_id "
                + " left join admins adm on adm.admin_id = a.admin_id ";
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

            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {

                msgsListSQL += whereString + "\n" + "order by a.date desc\n";
                List<HashMap> msgsList = DBSelect.getRows(msgsListSQL, args, conn);
                jspData.put("MSGS_LIST", msgsList);

            } catch (Exception e) {
                e.printStackTrace();
            }
            RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/jsp/msgs.jsp");
            request.setAttribute("JspData", jspData);
            disp.include(request, response);

        } else {
            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                msgsListSQL += "order by a.date desc\n"
                        + "limit 200";
                List<HashMap> msgsList = DBSelect.getRows(msgsListSQL, conn);
                jspData.put("MSGS_LIST", msgsList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/jsp/msgs.jsp");
            request.setAttribute("JspData", jspData);
            disp.include(request, response);
        }

    }

    private void addAdm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int loginAdmin = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
        String baseUrl = request.getContextPath() + request.getServletPath();
        HashMap jspData = (HashMap) request.getAttribute("JspData");
        if (loginAdmin == BoxCommonData.ROOT_ADMIN_ID) {
            HashMap reqData = Tools.replaceTags(getRequestParameters(request));
            String password = Tools.getStringValue(reqData.get("password"), "");
            int ldapAuth = Tools.parseInt(reqData.get("auth_via_ldap"), 0) == 1 ? 1 : 0;
            if (reqData.containsKey("ADD_ME") && Tools.parseInt(reqData.get("ADD_ME"), -1) > 0 && Tools.getStringValue(reqData.get("login"), "").trim().length() > 3 && (password.trim().length() > 3 || ldapAuth == 1)) {
                try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                    String phone = Tools.unmaskPhone(Tools.getStringValue(reqData.get("phone_number"), ""));
                    String salt = getSalt();
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] hash = digest.digest((password + ":" + salt).getBytes("UTF-8"));
                    String saltedHash = DatatypeConverter.printHexBinary(hash);
                    reqData.put("phone_number", phone);
                    reqData.put("salt", salt);
                    reqData.put("hash", saltedHash);
                    reqData.put("status", 1);
                    reqData.put("auth_via_ldap", ldapAuth);
                    DBTools.insertRow("admins", reqData, conn);
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
            }
        }
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            String appListSQL = "select * from admins\n"
                    + "order by admin_id";
            List<HashMap> admList = DBSelect.getRows(appListSQL, conn);
            jspData.put("ADM_LIST", admList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/jsp/admins.jsp");
        request.setAttribute("JspData", jspData);
        disp.include(request, response);
    }

    private void addApp(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String baseUrl = request.getContextPath() + request.getServletPath();
        HashMap jspData = (HashMap) request.getAttribute("JspData");
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
            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                String appListSQL = "select * from users\n"
                        + "order by id";
                List<HashMap> appList = DBSelect.getRows(appListSQL, conn);
                jspData.put("APP_LIST", appList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/jsp/add_apl.jsp");
            request.setAttribute("JspData", jspData);
            disp.include(request, response);
        }
    }

    private void sendMsg(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int loginAdmin = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
        String baseUrl = request.getContextPath() + request.getServletPath();
        String sms = Tools.getStringValue(request.getParameter("sms"), "");
        String recipient = Tools.getStringValue(request.getParameter("recipient"), "");
        sms = Tools.replaceTags(sms);
        recipient = Tools.replaceTags(recipient);
        recipient = Tools.unmaskPhone(recipient);
        if (request.getMethod().equalsIgnoreCase("POST") && sms.trim().length() > 0 && recipient.trim().length() > 0) {

            HashMap data = new HashMap();
            data.put("admin_id", loginAdmin);
            //data.put("valid_before", validBefore);
            data.put("message", sms);
            data.put("recipient", recipient);
            data.put("date", new Date());
            data.put("status", MessageStatus.STATUS_NEW);

            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                int msgId = DBTools.insertRow("sms_outbox", data, conn);
                log.info("New SMS id=" + msgId + " from admin id=" + loginAdmin);
                Event e = new Event("check_outbox", null, Event.Type.USER_ACTION);
                AppData.eventManager.newEvent(e);
            }

            response.sendRedirect(baseUrl + "/main");
        } else {
            response.sendRedirect(baseUrl + "/main");
        }

    }

    private int loginAdmin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int loginAdmin = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
        String baseUrl = request.getContextPath() + request.getServletPath();
        HashMap jspData = (HashMap) request.getAttribute("JspData");
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
                                log.error("Incorrect LDAP password for admin " + ", ip=" + request.getRemoteAddr());
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
                                    log.error("Admin:" + adminLogin + "is not active");
                                    RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/jsp/login.jsp");
                                    jspData.put("FAIL_LOGIN", 1);
                                    request.setAttribute("JspData", jspData);
                                    disp.forward(request, response);
                                }
                            } else {
                                log.error("Incorrect password for admin " + adminLogin + ", ip=" + request.getRemoteAddr());
                                RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/jsp/login.jsp");
                                jspData.put("FAIL_LOGIN", 1);
                                request.setAttribute("JspData", jspData);
                                disp.include(request, response);
                            }
                        }

                    } else {
                        log.error("Admin with login " + adminLogin + " not found" + ", ip=" + request.getRemoteAddr());
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
        return loginAdmin;

    }

    private void updAdmin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int loginAdmin = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
        String baseUrl = request.getContextPath() + request.getServletPath();
        HashMap reqData = Tools.replaceTags(getRequestParameters(request));
        int targetAdminId = Tools.parseInt(reqData.get("TARGET_ADMIN_ID"), -1);
        String password = Tools.getStringValue(reqData.get("password"), "");
        String phone = Tools.unmaskPhone(Tools.getStringValue(reqData.get("phone_number"), ""));
        int ldapAuth = Tools.parseInt(reqData.get("auth_via_ldap"), 0) == 1 ? 1 : 0;
        if (loginAdmin == BoxCommonData.ROOT_ADMIN_ID || loginAdmin == targetAdminId) { // меняем пароль или телефон, если сидим по root'ом или если id залогиненого админа совпадает  с id целевого админа для изменения пароля
            if (password.trim().length() > 5 || ldapAuth == 1) { //проверка длины
                try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {

                    String salt = getSalt();
                    HashMap dbMap = new HashMap();
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] hash = digest.digest((password + ":" + salt).getBytes("UTF-8"));
                    String saltedHash = DatatypeConverter.printHexBinary(hash);
                    if (targetAdminId > 0) {
                        dbMap.put("admin_id", targetAdminId);
                        dbMap.put("phone_number", phone);
                        dbMap.put("salt", salt);
                        dbMap.put("hash", saltedHash);

                        dbMap.put("auth_via_ldap", ldapAuth);
                        DBTools.updateRow("admins", dbMap, conn);
                    }
                    request.getSession().setAttribute("PASS_CHANGED", 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                HashMap dbMap = new HashMap();
                if (targetAdminId > 0) {
                    dbMap.put("admin_id", targetAdminId);
                    dbMap.put("phone_number", phone);
                    dbMap.put("auth_via_ldap", ldapAuth);
                    DBTools.updateRow("admins", dbMap, conn);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            request.getSession().setAttribute("ACCESS_ERROR", 1);

        }
        response.sendRedirect(baseUrl + "/addadm");
    }

    private void showMainPage(HttpServletRequest request, HttpServletResponse response) throws Exception {

        HashMap jspData = (HashMap) request.getAttribute("JspData");
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            Long msgSent = DBSelect.getCount("select count(*) as cnt from sms_outbox where status = " + MessageStatus.STATUS_SENT, "cnt", null, conn);

            Long msgSentDaily = DBSelect.getCount("select sum(case when multipart_status is not null then length(multipart_status) else 1 end) as cnt"
                    + " from sms_outbox where status = " + MessageStatus.STATUS_SENT
                    + " and date_sent > concat (current_date, ' 00:00:00') and date_sent < concat (current_date ,' 23:59:59')",
                    "cnt", null, conn);

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
    }

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
