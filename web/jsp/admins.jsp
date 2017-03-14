<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashMap"%>
<%@page import="org.lobzik.tools.Tools"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<jsp:useBean id="JspData" class="java.util.HashMap" scope="request"/>

<% int adminId = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
    if (adminId < 0) {
        return;
    }
    String baseUrl = request.getContextPath() + request.getServletPath() + "/";

    ArrayList<HashMap> admList = new ArrayList<HashMap>();
    boolean isRoot = true;
    if (JspData != null) {
        isRoot = Tools.parseInt(JspData.get("NOT_ROOT_ADMIN"), -1) < 0;
        admList = (ArrayList<HashMap>) Tools.isNull(JspData.get("ADM_LIST"), new ArrayList<HashMap>());
    }
%>
<jsp:include page="header.jsp" />
<br>
<h3>Администраторы шлюза</h3>
<%if (isRoot) {%>
<table border ="1" >
    <thead>
        <tr>
            <td>
                ID
            </td>
            <td>
                login
            </td>
            <td>
                status 
            </td>
        </tr>
    </thead>
    <tbody>
        <%for (HashMap hm : admList) {
                int id = Tools.parseInt(hm.get("admin_id"), -1);
                String login = Tools.getStringValue(hm.get("login"), "");
                int status = Tools.parseInt(hm.get("status"), -1);
        %>
        <tr>
            <td><%= id%></td>
            <td><%= login%></td>
            <td><%= status%></td>
        </tr>
        <%}%>
    </tbody>
</table>
<br>
<br>
<h3>Добавить нового администратора</h3>

<form action="<%= baseUrl + "addadm"%>" method="post">
    Login:<input type="text" name="login" /><br>
    Password<input type="text" name="password" /><br>
    <input hidden type="text" name="ADD_ME" value="1"/>
    <input type="submit" value="Добавить" name="submit" />
</form>
<br>
<br>
<%} else {%>
<h3>У вас нет прав на добавление администратора. Только root Администратор может добавлять новых администраторов</h3>
<%}%>
</body>
</html>
