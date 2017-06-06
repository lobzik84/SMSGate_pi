<%@page import="org.lobzik.tools.Tools"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<jsp:useBean id="JspData" class="java.util.HashMap" scope="request"/>
<!DOCTYPE html>
<%
    String baseUrl = request.getContextPath() + request.getServletPath() + "/";
    int adminId = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
    String adminLogin = Tools.getStringValue(request.getSession().getAttribute("AdminLogin"), "");
    if (adminId < 0) {
        return;
    }
    String headUrl = Tools.getStringValue(JspData.get("head_url"), "");
    int rssi = Tools.parseInt(JspData.get("RSSI"), -1);
%>
<html lang="ru">
    <head>
        <title>SMS-Gate Admin profile</title>        
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <meta name="format-detection" content="telephone=no"> <!-- IE -->
        <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css?v=1.0">
        <link rel="shortcut icon" href="<%=request.getContextPath()%>/img/favicon.png" type="image/png">
        <script type="text/javascript" src="<%=request.getContextPath()%>/js/min/main.min.js"></script>
    </head>
    <body class="abc">
        <header class="header">
            <div class="header__top">
                <a class="header__logo" href="<%= baseUrl + "main"%>"><p class="header__logo_top ">АИС РТРС</p><p class="header__logo_bottom">служба коротких сообщений</p></a>

                <div class="header__right">
                    <div class="header__user">
                        <div class="header__user_root <%= adminId == 1 ? "header__user_root_on" : ""%>"></div>
                        <p class="header__user_id"><%= adminLogin%></p>
                        <a class="header__user_logout" href="<%= baseUrl + "logout"%>"></a>
                    </div>
                    <div class="header__dbm">
                        <?xml version="1.0" standalone="no"?>
                        <svg class="dBm_svg" width="19" height="16" viewBox="0 0 19 16" xmlns="http://www.w3.org/2000/svg" version="1.1">
                        <style>
                            svg { -webkit-background-clip: text; }
                        </style>
                        <rect class="min" width="4" height="4" x="0" y="12" fill="#ffffff" />
                        <rect class="low" width="4" height="8" x="5" y="8" fill="#ffffff" />
                        <rect class="middle" width="4" height="12" x="10" y="4" fill="#ffffff" />
                        <rect class="good" width="4" height="16" x="15" y="0" fill="#ffffff" />
                        </svg>
                        <p class="counter_dBm"><%=rssi%></p> dBm
                    </div>
                </div>
            </div>
            <nav class="nav">
                <ul class="nav__list">
                    <li class="nav__item <%= headUrl.equals("main") ? "nav__item_active" : ""%> ">
                        <a class="nav__link" href="<%= baseUrl + "main"%>">Главная</a>
                    </li>
                    <li class="nav__item <%= headUrl.equals("addapp") ? "nav__item_active" : ""%> ">
                        <a class="nav__link" href="<%= baseUrl + "addapp"%>">Клиенты</a>
                    </li>
                    <li class="nav__item <%= headUrl.equals("addadm") ? "nav__item_active" : ""%> ">
                        <a class="nav__link" href="<%= baseUrl + "addadm"%>">Администраторы</a>
                    </li>
                    <li class="nav__item <%= headUrl.equals("msgs") ? "nav__item_active" : ""%> ">
                        <a class="nav__link" href="<%= baseUrl + "msgs"%>">Сообщения</a>
                    </li>
                    <li class="nav__item <%= headUrl.equals("rcpnts") ? "nav__item_active" : ""%> ">
                        <a class="nav__link" href="<%= baseUrl + "rcpnts"%>">Получатели рассылок</a>
                    </li>
                    <li class="nav__item <%= headUrl.equals("groups") ? "nav__item_active" : ""%> ">
                        <a class="nav__link" href="<%= baseUrl + "groups"%>">Группы рассылок</a>
                    </li>
                </ul>
            </nav>
        </header>