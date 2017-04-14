/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi;

import org.lobzik.home_sapiens.entity.UsersSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.interfaces.RSAPublicKey;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.lobzik.tools.Tools;

/**
 *
 * @author lobzik
 */
@WebServlet(name = "JsonServlet", urlPatterns = {"/json", "/json/*"})
public class JSONServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        //response.setHeader("Access-Control-Allow-Credentials", "true");  

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = request.getInputStream();
        long readed = 0;
        long content_length = request.getContentLength();
        byte[] bytes = new byte[65536];
        while (readed < content_length) {
            int r = is.read(bytes);
            if (r < 0) {
                break;
            }
            baos.write(bytes, 0, r);
            readed += r;
        }
        baos.close();
        String requestString = baos.toString("UTF-8");

        try {
            if (requestString.startsWith("{")) {
                JSONObject json = new JSONObject(requestString);
                request.setAttribute("json", json);
                //int userId = 0;
                String action = json.getString("action");
                switch (action) {
                    case "send_sms":
                        String username = json.getString("username");
                        if (username.length() > 0) {
                            doSmsSend(request, response, username);
                        }
                        break;

                    case "get_out_status":

                        replyWithOutStatus(request, response);

                        break;

                }
            } else {
                response.getWriter().print("accepted json only");
            }
        } catch (Throwable e) {
            //e.printStackTrace();
            JSONObject reply = new JSONObject();
            reply.put("result", "error");
            reply.put("message", e.getMessage());
            response.getWriter().print(reply);
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

    private void doSmsSend(HttpServletRequest request, HttpServletResponse response, String username) throws Exception {
        JSONObject json = (JSONObject) request.getAttribute("json");
        int msgId = JSONAPI.sendSms(json, username);

        JSONObject reply = new JSONObject();
        reply.put("result", "success");
        reply.put("msg_id", msgId);

        response.getWriter().write(reply.toString());

    }

    private void replyWithOutStatus(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject json = (JSONObject) request.getAttribute("json");
        int msgId = json.getInt("msg_id");
        
        JSONObject reply = JSONAPI.getOutMsgStatusJSON(msgId);

        response.getWriter().write(reply.toString());
    }

    private void replyWithLog(HttpServletRequest request, HttpServletResponse response) throws Exception {

        JSONObject reply = new JSONObject();//JSONAPI.getEncryptedLogJSON(json, (RSAPublicKey) session.get("UsersPublicKey"));
        reply.put("result", "success");

        response.getWriter().write(reply.toString());
    }

    private BigInteger sha256(String s) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] bytes = s.getBytes();
        sha.update(bytes, 0, bytes.length);
        return new BigInteger(1, sha.digest());
    }
}
