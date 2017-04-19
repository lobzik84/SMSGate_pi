/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi;

import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.lobzik.tools.Tools;

/**
 *
 * @author lobzik
 */
@WebServlet(name = "xml", urlPatterns = {"/xml"})
public class XMLServlet extends HttpServlet {

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
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/xml;charset=UTF-8");

        try {
            Document req = parseRequest(request);

            String action = req.getElementsByTagName("action").item(0).getTextContent();

            switch (action) {
                case "send_sms":
                    String username = req.getElementsByTagName("username").item(0).getTextContent();
                    if (username.length() > 0) {
                        Document reply = XMLAPI.sendSms(req, username);
                        writeDocToResponse(reply, response);
                    }
                    break;

                case "get_out_status":
                    int msgId = Tools.parseInt(req.getElementsByTagName("message_id").item(0).getTextContent(), -1);
                    if (msgId < 0) {
                        throw new Exception("unknown message id");
                    }
                    Document reply = XMLAPI.getOutMsgStatus(msgId);
                    writeDocToResponse(reply, response);

                    break;

                default:
                    throw new Exception("Unknown action");
            }

        } catch (Exception e) {
            e.printStackTrace();
            replyWithError(e.getClass().getName() + ":" + e.getMessage(), response);
        }
    }

    private void replyWithError(String errorMessage, HttpServletResponse response) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("result");
            doc.appendChild(rootElement);

            Element err = doc.createElement("error");
            err.appendChild(doc.createTextNode(errorMessage));
            rootElement.appendChild(err);
            writeDocToResponse(doc, response);
        } catch (Exception e) {
            System.err.println("Error while processing error!");
            e.printStackTrace();
        }
    }

    private Document parseRequest(HttpServletRequest request) throws Exception {

        InputStream is = request.getInputStream();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.parse(is);
        
        return doc;
    }

    private void writeDocToResponse(Document doc, HttpServletResponse response) throws Exception {
        try (OutputStream out = response.getOutputStream()) {
            /*TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        DOMSource source = new DOMSource(doc);
                        StreamResult result = new StreamResult(out);
                        transformer.transform(source, result);*/

            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            //Writer out = new StringWriter();
            tf.transform(new DOMSource(doc), new StreamResult(out));
            out.flush();
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
