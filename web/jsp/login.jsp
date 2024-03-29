<%@page import="org.lobzik.tools.Tools"%>
<%@page import="java.util.HashMap"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%
    HashMap data = (HashMap) request.getAttribute("JspData");
    String baseUrl = request.getContextPath() + request.getServletPath() + "/";
    boolean isFailLogin = false;
    if (data != null) {
        isFailLogin = Tools.parseInt(data.get("FAIL_LOGIN"), -1) > 0;
    }
%>
<html lang="ru">
    <head>
        <title>Login to SMS-gate</title>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <link rel="shortcut icon" href="<%=request.getContextPath()%>/img/favicon.png" type="image/png">
        <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css?v=1.0">
    </head>
    <body class="lbc">
        <div class="login__block">
            <div class="login__title">
                Служба<br>коротких<br>сообщений<br>ФГУП РТРС
            </div>
            <div class="login__form">
<!--                <p class="title mb-25"></p>-->
                <form action="<%= baseUrl + "login"%>" method="post">
                    <label class="label">Логин</label>
                    <input class="login__input mb-20" type="text" name="login"/>
                    <label class="label">Пароль</label>
                    <input class="login__input" type="password" name="pass"/>
                    
                    <%if (isFailLogin) {%>
                        <div class="label_wrong">Неверный логин или пароль!</div>
                    <%}%>
                    
                    <input class="login__input_button" type="submit" value="Войти" name="submit" />
                    <div class="login__hint">Ввод логина и пароля осуществляется с учетом больших и маленьких букв</div>
                </form>
            </div>
        </div>
    </body>
</html>
