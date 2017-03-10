<%@page import="org.lobzik.tools.Tools"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<jsp:useBean id="JspData" class="java.util.HashMap" scope="request"/>
<!DOCTYPE html>
<!DOCTYPE html>
<%
    String baseUrl = request.getContextPath() + request.getServletPath() + "/";
    int adminId = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
    if (adminId < 0) {
        return;
    }
    int msgSent = 0;
    int msgErrs = 0;
    int msgInbox = 0;
    boolean smsSended = false;
    if (JspData != null) {
        msgSent = Tools.parseInt(JspData.get("msgSent"), -1);
        msgErrs = Tools.parseInt(JspData.get("msgErrs"), -1);
        msgInbox = Tools.parseInt(JspData.get("msgInbox"), -1);
        smsSended = Tools.parseInt(JspData.get("SMS_SENDED"), -1) > 0;
    }
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>SMS-Gate Admin profile</title>
    </head>
    <body>
        <h1>SMS-Gate Admin profile</h1>
        <h2>Поздравляю, Вы залогинились!</h2>
        <h2>AdminId: <%= adminId%></h2>
        <br>
        <br>
        <h3>Отправленных сообщений: <%= msgSent%></h3>
        <h3>Ошибок: <%= msgErrs%></h3>
        <h3>Принятых сообщений: <%= msgInbox%></h3>

        <br>
        <%if (smsSended) {%>
        <h4>Сообщение отправлено!</h4>
        <br>
        <%}%>
        <form action="<%= baseUrl + "sendmsg"%>" method="post">
            SMS to:<input type="text" name="recipient" /><br>
            Text:<input type="text" name="sms" /><input type="submit" value="Send" name="submit" />
        </form> <br> <br>
        <br>

        <a class ="sn-userbox-link ul-reg" rel="nofollow" href="<%= baseUrl + "logout"%>">Выйти</a>
    </body>
</html>
