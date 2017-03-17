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

<div class="content__table">

    <h2>Администраторы шлюза</h2>

    <%if (isRoot) {%>

    <input class="btn blbc white mt-20 mb-5" type="submit" value="Добавить нового пользователя"/>

    <form action="<%= baseUrl + "addadm"%>" method="post">
        <div class="inline-b">
            <label class="label mt-10">Login:</label>
            <input type="text" name="login" />
        </div>
        <div class="inline-b ml-10 mr-10 va-t">
            <label class="label mt-10">Password:</label>
            <input type="text" name="password"/>
        </div>
        <input hidden type="text" name="ADD_ME" value="1"/>

        <input class="btn blbc white" type="submit" value="Добавить" name="submit" />
    </form>

    <table class="table mt-20">
        <thead>
            <tr>
                <td class="w-10">
                    ID
                </td>
                <td class="w-60">
                    login
                </td>
                <td class="w-30">
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
</div>
<%} else {%>
<h2>У вас нет прав на добавление администратора. Только root Администратор может добавлять новых администраторов</h2>
<%}%>
</body>
</html>