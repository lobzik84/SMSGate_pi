<%@page import="java.io.File"%>
<%@page import="org.lobzik.tools.Tools"%>
<%@page import="java.sql.Connection"%>
<%@page import="org.lobzik.tools.db.mysql.DBTools"%>
<%@page import="org.lobzik.smspi.pi.event.Event"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.smspi.pi.*"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>SMS-gate</h1>

        <br>
        <%
            int loginAdmin = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
            if (loginAdmin  != 1) {
                return;
                    
            } 
            request.setCharacterEncoding("UTF-8");

            if (request.getMethod().equalsIgnoreCase("POST")) {
                String command = request.getParameter("command");
                String modemCommand = request.getParameter("modem_command");
                
                String event = request.getParameter("event");

                String sms = request.getParameter("sms");
                String recipient = request.getParameter("recipient");

                String system_event = request.getParameter("system_event");
                
                if (command != null && command.length() > 0) {
                    HashMap data = new HashMap();
                    data.put("uart_command", command);
                    Event e = new Event("internal_uart_command", data, Event.Type.USER_ACTION);
                    AppData.eventManager.newEvent(e);
                }
                
                if (modemCommand != null && modemCommand.length() > 0) {
                    HashMap data = new HashMap();
                    data.put("modem_command", modemCommand);
                    Event e = new Event("modem_command", data, Event.Type.USER_ACTION);
                    AppData.eventManager.newEvent(e);
                }
                
                if (sms != null && sms.length() > 0) {
                    HashMap data = new HashMap();
                    data.put("message", sms);
                    data.put("recipient", recipient);
                    Event e = new Event("send_sms", data, Event.Type.USER_ACTION);
                    AppData.eventManager.newEvent(e);
                }
                if (system_event != null && system_event.length() > 0) {
                    Event e = new Event(system_event, null, Event.Type.SYSTEM_EVENT);
                    AppData.eventManager.newEvent(e);
                } else if (event != null && event.length() > 0) {
                    Event e = new Event(event, null, Event.Type.TIMER_EVENT);
                    AppData.eventManager.newEvent(e);
                }
            }
        %> 

        <br>
         <b>Internal UART command: </b>
        <form action="" method="post">
            <input type="text" name="modem_command" /><input type="submit" value="OK" name="submit" />
        </form> <br> <br>
        
        <b>Modem command: </b>
        <form action="" method="post">
            <input type="text" name="command" /><input type="submit" value="OK" name="submit" />
        </form> <br> <br>
        
        <br>

        <b>Generate timer event: </b>
        <form action="" method="post">
            <input type="text" name="event" value="db_clearing" /><input type="submit" value="OK" name="submit" />
        </form><br> <br>

        <b>Show image: </b>
        <form action="" method="post">
            <input type="text" name="image" value="screen.jpg" /><input type="submit" value="OK" name="submit" />
        </form>
        <br> <br>
        <b>Do system command: </b>
        <form action="" method="post">
            <input type="text" name="system_event" value="shutdown" /><input type="submit" value="OK" name="submit" />
        </form>
        <br>

        <form action="" method="post">
            SMS to:<input type="text" name="recipient" /><br>
            Text:<input type="text" name="sms" /><input type="submit" value="Send" name="submit" />
        </form> <br> <br>
        <br>
        <br>
        <br>
        <div id="container" style="height: 400px; width: 800px;"></div>
    </body>

</html>
