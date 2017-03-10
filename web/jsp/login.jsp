<%@page import="org.lobzik.tools.Tools"%>
<%@page import="java.util.HashMap"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%
    HashMap data = (HashMap) request.getAttribute("JspData");
    String baseUrl = request.getContextPath() + request.getServletPath() + "/";
    boolean isFailLogin = false;
    if (data != null){
        isFailLogin = Tools.parseInt(data.get("FAIL_LOGIN"), -1) > 0;
    }
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login to SMS-gate</title>
    </head>
    <body>
        <h1>SMS-gate</h1>
        <h2>Admin login</h2>
        </br>
        <%if (isFailLogin){%>
        <h2 style = "color: red">Вы ввели неправильные логин или пароль</h2>
        <%}%>
        </br>
        <b>Login: </b>
        <form action="<%= baseUrl + "login"%>" method="post">
            <input type="text" name="login"/>
            </br>
            <input type="password" name="pass"/>
            <input type="submit" value="OK" name="submit" />
        </form><br> <br>
    </body>
</html>
