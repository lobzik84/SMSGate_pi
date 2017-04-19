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

<div class="content__layout">
    <div class="content__table">

        <p class="title">Администраторы шлюза</p>

        <%if (isRoot) {%>

        <input class="btn btn_hide mt-20 mb-5" type="submit" value="Добавить нового администратора"/>

        <form class="form_hide none" action="<%= baseUrl + "addadm"%>" method="post">
            <div class="inline-b">
                <label class="label mt-10">Login</label>
                <input type="text" name="login" />
            </div>
            <div class="inline-b ml-10 mr-10 va-t wp-210">
                <label class="label_inline_gen mt-10">Password</label>
                <label class="label_inline_gen label_generate" onclick="generatePass()">Сгенерировать</label>
                <input class="wp-170" type="text" name="password" id="admin_pass"/>
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
                    <td class="w-20">
                        status
                    </td>
                    <td class="w-10">
                        delete
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
                    <td>
                        <a class="btn_delete" onclick="return confirm('Удалить администратора?');" href="<%= baseUrl + "addadm?removeAdm=1&id=" + id%>"></a>
                    </td>
                </tr>
                <%}%>
            </tbody>
        </table>
    </div>
    <%} else {%>
    <h2>У вас нет прав на добавление администратора. Только root Администратор может добавлять новых администраторов</h2>
    <%}%>
</div>
</body>
</html>