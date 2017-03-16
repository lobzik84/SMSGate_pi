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
%>
<html lang="ru">
    <head>
        <title>SMS-Gate Admin profile</title>        
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <link rel="stylesheet" href="<%=request.getContextPath()%>/css/style.css?v=1.0">
    </head>
    <body class="abc">
        <header class="header">
            <a class="header__logo" href="<%= baseUrl + "main"%>">smsity</a>
            <nav class="nav">
                <ul class="nav__list">
                    <li class="nav__item nav__item_active">
                        <a class="nav__link" href="<%= baseUrl + "main"%>">Главная</a>
                    </li>
                    <li class="nav__item">
                        <a class="nav__link" href="<%= baseUrl + "addapp"%>">Пользователи шлюза</a>
                    </li>
                    <li class="nav__item">
                        <a class="nav__link" href="<%= baseUrl + "addadm"%>">Администраторы</a>
                    </li>
                </ul>
            </nav>
                    <div class="header__right">
                <div class="header__user">
                    <p class="header__user_id">AdminId: <%= adminId%><%= adminId == 1 ? "(root)" : ""%></p>
                    <a class="header__user_logout" href="<%= baseUrl + "logout"%>">Выйти</a>
                </div>
                <div class="header__dbm">
                    000 dBm
                </div>
            </div>
        </header>

