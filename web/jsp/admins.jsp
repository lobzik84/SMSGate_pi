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
    boolean isPassChanged = false;
    boolean isAccessError = false;
    if (JspData != null) {
        isRoot = Tools.parseInt(JspData.get("NOT_ROOT_ADMIN"), -1) < 0;
        admList = (ArrayList<HashMap>) Tools.isNull(JspData.get("ADM_LIST"), new ArrayList<HashMap>());
        if (Tools.parseInt(request.getSession().getAttribute("PASS_CHANGED"), -1) > 0) {
            isPassChanged = true;
            request.getSession().removeAttribute("PASS_CHANGED");
        }
        if (Tools.parseInt(request.getSession().getAttribute("ACCESS_ERROR"), -1) > 0) {
            isAccessError = true;
            request.getSession().removeAttribute("ACCESS_ERROR");
        }
    }
%>
<jsp:include page="header.jsp" />

<div class="content__layout">
    <div class="content__table">

        <p class="title">Администраторы шлюза</p>
        <%if (isPassChanged) {%>
        <h2>Пароль успешно изменён</h2>
        <%} else if (isAccessError) {%>
        <h2>Вы можете изменить только свой пароль. Изменять пароль других администроторов может только root администратор</h2>
        <%}%>
        <%if (isRoot) {%>

        <input class="btn btn_hide mt-20 mb-5" type="submit" value="Добавить нового администратора"/>

        <form class="form_hide none" action="<%= baseUrl + "addadm"%>" method="post">
            <div class="inline-b">
                <label class="label mt-10">Login</label>
                <input type="text" name="login" />
            </div>
            <div class="inline-b ml-10 mr-10 va-t wp-210">
                <label class="label_inline_gen mt-10">Password</label>
                <label class="label_inline_gen label_generate" onclick="generatePass('#admin_pass')">Сгенерировать</label>
                <input class="wp-170 js-tooltip" title="Пароль не должен содержать символы &quot;, ', <, >," type="text" name="password" id="admin_pass"/>
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
                    <td class="w-30">
                        login
                    </td>
                    <td class="w-20">
                        status
                    </td>
                    <td class="w-30">
                        edit
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
                        <input class="btn btn_hide-table" type="submit" value="Редактировать пароль"/>
                        <form class="form_hide-table none" id="edit_form" action="<%= baseUrl + "chpass"%>" method="post">
                            <div class="va-t wp-240">
                                <label class="label_inline_gen mt-10">Новый пароль</label>
                                <label class="label_inline_gen label_generate" onclick="generatePass('#admin_pass_<%=id%>')">Сгенерировать</label>
                                <br>
                                <input class="wp-210 js-tooltip" title="Пароль не должен содержать символы &quot;, ', <, >," type="text" name="password" id="admin_pass_<%=id%>"/>
                            </div>
                            <input hidden type="text" name="TARGET_ADMIN_ID" value="<%=id%>"/>
                            <input class="btn blbc white mt-15" type="submit" value="Изменить" name="submit" />
                        </form>
                    </td>
                    <td>
                        <%if (id != 1) {%>
                        <a class="btn_delete" onclick="return confirm('Удалить администратора?');" href="<%= baseUrl + "addadm?removeAdm=1&id=" + id%>"></a>
                        <%}%>
                    </td>
                </tr>
                <%}%>
            </tbody>
        </table>
    </div>

    <%} else {%>

    <p class="label_wrong">Только root администратор может добавлять новых администраторов!</p>
    <p class="label ta-c pt-10">Вы можете изменить свой пароль.</p>

    <input class="btn btn_hide mt-20 mb-5" type="submit" value="Редактировать пароль"/>
    <form class="form_hide none" id="edit_form" action="<%= baseUrl + "chpass"%>" method="post">
        <div class="va-t wp-240">
            <label class="label_inline_gen mt-10">Новый пароль</label>
            <label class="label_inline_gen label_generate" onclick="generatePass('#admin_pass')">Сгенерировать</label>
            <br>
            <input class="wp-210 js-tooltip" title="Пароль не должен содержать символы &quot;, ', <, >," type="text" name="password" id="admin_pass"/>

        </div>
        <input hidden type="text" name="TARGET_ADMIN_ID" value="<%=adminId%>"/>
        <input class="btn blbc white mt-15" type="submit" value="Изменить" name="submit" />
    </form>

    <%}%>
</div>
</body>
</html>