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
%>
<html>
    <head>
        <style type = "text/css">
            table {
                width: 500px;
            }
            
            .key-hover { 
                display: none; 
                position: relative;
                top: 0;
                left: 0;
                z-index: 99;
                border: 1px solid #333;
                background: #eee;
                width: 300px;
                word-wrap: break-word;
            }
            p:hover + .key-hover { display: block; }
        </style>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>SMS-Gate Admin profile</title>
    </head>
    <body>
        <h1>SMS-Gate Admin profile</h1>
        <h2>AdminId: <%= adminId%><%= adminId == 1 ? "(root)" : ""%></h2>
        <br>

        <a  href="<%= baseUrl + "main"%>">Главная</a>&nbsp;
        <a  href="<%= baseUrl + "addapp"%>">Пользователи шлюза</a>&nbsp;
        <a  href="<%= baseUrl + "addadm"%>">Администраторы</a>&nbsp;
        <a  href="<%= baseUrl + "logout"%>">Выйти</a>

