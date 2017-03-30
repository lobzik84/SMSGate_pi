/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author konstantin
 */
@WebServlet(name = "HighchartsJsonServlet", urlPatterns = {"/HighchartsJsonServlet"})
public class HighchartsJsonServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        StringBuilder jsonData = new StringBuilder();
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

        try (PrintWriter out = response.getWriter()) {
            json.write(out);
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

}
