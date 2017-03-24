<%@page import="org.lobzik.tools.Tools"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<jsp:useBean id="JspData" class="java.util.HashMap" scope="request"/>
<!DOCTYPE html>
<%
    String baseUrl = request.getContextPath() + request.getServletPath() + "/";
    int adminId = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
    if (adminId < 0) {
        return;
    }
    String headUrl = Tools.getStringValue(JspData.get("head_url"), "");
    int rssi = Tools.parseInt(JspData.get("RSSI"),-1);
    int signalLevel = -1;
    if (rssi < -100 && rssi >= -90){
        signalLevel = 1;
    } else if (rssi < -90 && rssi >= -80){
        signalLevel = 2;
    } else if (rssi < -80 && rssi >= -70){
        signalLevel = 3;
    } else if (rssi < -70){
        signalLevel = 4;
    }
%>
<html lang="ru">
    <head>
        <title>SMS-Gate Admin profile</title>        
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css?v=1.0">
        <script type="text/javascript" src="<%=request.getContextPath()%>/js/min/main.min.js"></script>
    </head>
    <body class="abc">
        <header class="header">
            <a class="header__logo" href="<%= baseUrl + "main"%>">smsity</a>
            <nav class="nav">
                <ul class="nav__list">
                    <li class="nav__item <%= headUrl.equals("main") ? "nav__item_active" : ""%> ">
                        <a class="nav__link" href="<%= baseUrl + "main"%>">Главная</a>
                    </li>
                    <li class="nav__item <%= headUrl.equals("addapp") ? "nav__item_active" : ""%> ">
                        <a class="nav__link" href="<%= baseUrl + "addapp"%>">Пользователи</a>
                    </li>
                    <li class="nav__item <%= headUrl.equals("addadm") ? "nav__item_active" : ""%> ">
                        <a class="nav__link" href="<%= baseUrl + "addadm"%>">Администраторы</a>
                    </li>
                    <li class="nav__item <%= headUrl.equals("msgs") ? "nav__item_active" : ""%> ">
                        <a class="nav__link" href="<%= baseUrl + "msgs"%>">Сообщения</a>
                    </li>
                </ul>
            </nav>
            <div class="header__right">
                <div class="header__user">
                    <p class="header__user_id"><%= adminId%><%= adminId == 1 ? "(root)" : ""%></p>
                    <a class="header__user_logout" href="<%= baseUrl + "logout"%>">Выйти</a>
                </div>
                <div class="header__dbm">
                    <p class="<%= signalLevel%>"><%= rssi%></p>
                </div>
            </div>
        </header>

