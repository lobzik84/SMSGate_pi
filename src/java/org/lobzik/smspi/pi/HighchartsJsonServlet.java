/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author konstantin makarov
 */
@WebServlet(name = "HighchartsJsonServlet", urlPatterns = {"/HighchartsJsonServlet", "/HighchartsJsonServlet/*"})
public class HighchartsJsonServlet extends HttpServlet {

    private static final List<String> PATHINFO_ACTIONS = Arrays.asList(
            "/smsbydate",
            "/smsbyusers"
    );

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        response.setDateHeader("Expires", 0); // Proxies.

        if (request.getContentType() != null && request.getContentType().toLowerCase().contains("utf-8")) {
            request.setCharacterEncoding("UTF-8");
        } else {
            request.setCharacterEncoding("windows-1251");
        }

        int loginAdmin = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
        if (loginAdmin < 0) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }

        String path = Tools.getStringValue(request.getPathInfo(), "");
        JSONObject result;
        switch (getActionId(path)) {
            case 0:
                result = getSmsByDate(request);
                break;
            case 1:
                result = getSmsByUsers(request);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
        }

        try (PrintWriter out = response.getWriter()) {
            result.write(out);
        }
    }

    private JSONObject getSmsByDate(HttpServletRequest request) {
        JSONObject json = new JSONObject();
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            String inboxSql = "select c.epoch*1000 as epoch, count(*) as cnt from\n"
                    + "(select \n"
                    + "    b.*, \n"
                    + "    unix_timestamp (b.trunc2) as epoch\n"
                    + "from\n"
                    + "    (select \n"
                    + "        a.*, \n"
                    + "        str_to_date (a.trunc, '%d.%m.%Y %k:00:00') as trunc2 \n"
                    + "    from\n"
                    + "        (select \n"
                    + "            so.*,\n"
                    + "            date_format (so.date_sent, '%d.%m.%Y %k:00:00 ') as trunc\n"
                    + "        from sms_outbox so\n"
                    + "        where date_sent is not null and status = 1\n"
                    + "        )a\n"
                    + "    )b)c group by c.epoch\n"
                    + "    order by c.epoch";
            List<HashMap> res1 = DBSelect.getRows(inboxSql, null, conn);
            long[][] dataArray1 = new long[res1.size()][2];
            for (int i = 0; i < res1.size(); ++i) {
                long[] element = {Tools.parseLong(res1.get(i).get("epoch"), -1), Tools.parseLong(res1.get(i).get("cnt"), -1)};
                dataArray1[i] = element;
            }
            json.put("data1", dataArray1);

            String outboxSql = "select c.epoch*1000 as epoch, count(*) as cnt from\n"
                    + "(select \n"
                    + "    b.*, \n"
                    + "    unix_timestamp (b.trunc2) as epoch\n"
                    + "from\n"
                    + "    (select \n"
                    + "        a.*, \n"
                    + "        str_to_date (a.trunc, '%d.%m.%Y %k:00:00') as trunc2 \n"
                    + "    from\n"
                    + "        (select \n"
                    + "            si.*,\n"
                    + "            date_format (si.date, '%d.%m.%Y %k:00:00 ') as trunc\n"
                    + "        from sms_inbox si\n"
                    + "        where date is not null\n"
                    + "        )a\n"
                    + "    )b)c group by c.epoch\n"
                    + "    order by c.epoch";
            List<HashMap> res2 = DBSelect.getRows(outboxSql, null, conn);
            long[][] dataArray2 = new long[res2.size()][2];
            for (int i = 0; i < res2.size(); ++i) {
                long[] element = {Tools.parseLong(res2.get(i).get("epoch"), -1), Tools.parseLong(res2.get(i).get("cnt"), -1)};
                dataArray2[i] = element;
            }
            json.put("data2", dataArray2);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    private int getActionId(String pathInfo) {
        pathInfo = pathInfo.toLowerCase();
        for (int j = 0; j < PATHINFO_ACTIONS.size(); j++) {
            String action = PATHINFO_ACTIONS.get(j);
            action = (action.startsWith("/") ? action : ("/" + action)).toLowerCase();
            if (pathInfo.startsWith(action)) {
                return j;
            }
        }
        return -1;
    }

    private JSONObject getSmsByUsers(HttpServletRequest request) {
        double start = Tools.parseDouble(request.getParameter("start"), 0.0) / 1000;//параметры из запроса начала и конца периода по которому требуется статистика, в unix времени
        double end = Tools.parseDouble(request.getParameter("end"), 0.0) / 1000; // делим на 1000 тк hightcharts выдает время в мс

        List argList = new ArrayList();
        argList.add(start);
        argList.add(end);

        JSONObject result = new JSONObject();
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            String sqlQuerry = "select d.sender_name, count(*) as y from (select \n"
                    + "    so.id, \n"
                    + "    coalesce (so.date_sent, \"01.03.2017 01:00:00\") as sent_date , \n"
                    + "    coalesce (a.admin_id,u.id, -2) as sender_id, \n"
                    + "    coalesce (a.login, u.name, \"Local\" ) as sender_name \n"
                    + "from sms_outbox so\n"
                    + "left join admins a on a.admin_id = so.admin_id\n"
                    + "left join users u on u.id = so.user_id\n"
                    + ") d\n"
                    + "where  d.sent_date between FROM_UNIXTIME(?) and FROM_UNIXTIME(?)\n"
                    + "group by d.sender_name";
            List<HashMap> list = DBSelect.getRows(sqlQuerry, argList, conn);

            JSONArray data = new JSONArray();

            list.stream().forEach((hm) -> {
                HashMap cur = new HashMap();
                cur.put("name", Tools.getStringValue(hm.get("sender_name"), ""));
                cur.put("y", Tools.parseInt(hm.get("y"), -1));
                data.put(new JSONObject(cur));
            });

            if (data.length() > 0) {
                result.put("RESULT", data);
                result.put("SC", HttpServletResponse.SC_OK);
            } else {
                result.put("SC", HttpServletResponse.SC_NO_CONTENT);
            }

        } catch (Exception e) {
            result.put("SC", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("ERROR", e.getMessage());
            e.printStackTrace(System.err);
        }
        return result;
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

}
