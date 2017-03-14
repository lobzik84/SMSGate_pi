<%@page import="org.lobzik.tools.Tools"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<jsp:useBean id="JspData" class="java.util.HashMap" scope="request"/>
<%
    int adminId = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
    if (adminId < 0) {
        return;
    }
    
    String baseUrl = request.getContextPath() + request.getServletPath() + "/";

    int msgSent = 0;
    int msgSentDaily = 0;
    int msgErrs = 0;
    int msgInbox = 0;
    if (JspData != null) {
        msgSent = Tools.parseInt(JspData.get("msgSent"), -1);
        msgErrs = Tools.parseInt(JspData.get("msgErrs"), -1);
        msgInbox = Tools.parseInt(JspData.get("msgInbox"), -1);
        msgSentDaily = Tools.parseInt(JspData.get("msgSentDaily"), -1);
    }
%>

<jsp:include page="header.jsp" />
<br>
<h4>Отправленных сообщений всего: <%= msgSent%></h4>
<h4>Отправленных сообщений за сегодня: <%= msgSentDaily%></h4>
<h4>Ошибок: <%= msgErrs%></h4>
<h4>Принятых сообщений: <%= msgInbox%></h4>
<h5>Отправка сообщения:</h5>
<form action="<%= baseUrl + "sendmsg"%>" method="post">
    SMS to:<input type="text" name="recipient" /><br>
    Text:<input type="text" name="sms" /><input type="submit" value="Send" name="submit" />
</form> <br> <br>

</body>
</html>
