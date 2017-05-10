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

        <p class="title">Учетные записи, допущенные к администрированию службы коротких сообщений</p>
        <%if (isPassChanged) {%>
        <p class="title mt-15">Пароль успешно изменён!</p>
        <%} else if (isAccessError) {%>
        <h2>Вы можете изменить только свой пароль. Изменять пароль других администроторов может только root администратор</h2>
        <%}%>
        <%if (isRoot) {%>

        <table class="table mt-20">
            <thead>
                <tr>
                    <td class="bold w-10">
                        №
                    </td>
                    <td class="w-20">
                        Наименование
                    </td>
                    <td class="w-50"></td>
                    <td class="w-20">
                        Управление
                    </td>
                </tr>
            </thead>
            <tbody>
                <%for (HashMap hm : admList) {
                        int id = Tools.parseInt(hm.get("admin_id"), -1);
                        String login = Tools.getStringValue(hm.get("login"), "");
                        int status = Tools.parseInt(hm.get("status"), -1);
                        String auth_via_ldap = (Tools.parseInt(hm.get("auth_via_ldap"), -1) == 1) ? "checked" : "";
                %>
                <tr>
                    <td class="ta-r"><%= id%></td>
                    <td>
                        <%= login%>
                    </td>
                    <td>
                        <input class="btn_hide-table icon icon-edit js-tooltip" title="Изменить пароль" type="submit" value=""/>
                        <form class="form_hide-table none" id="edit_form" action="<%= baseUrl + "chpass"%>" method="post">
                            <div class="va-t wp-240">
                                <input class='va-m auth_rtrn' type="checkbox" value="1" name="auth_via_ldap" <%=auth_via_ldap%>/>
                                <label class='label__sub'>Доменная авторизация РТРС</label>
                                <div class="auth_rtrn_pass mt-10">
                                    <label class="icon icon-genPass js-tooltip va-m" title="Сгенерировать пароль" onclick="generatePass('#admin_pass_<%=id%>')"></label>
                                    <input class="wp-170 js-tooltip mb-5" title="Пароль не должен содержать символы &quot;, ', <, >," type="text" name="password" id="admin_pass_<%=id%>"/>
                                </div>
                            </div>
                            <input hidden type="text" name="TARGET_ADMIN_ID" value="<%=id%>"/>
                            <input class="icon icon-save js-tooltip mt-15" title="Сохранить" type="submit" value="" name="submit" />
                        </form>
                    </td>
                    <td class="ta-c">
                        <%if (id != 1) {%>
                        <a class="btn_delete" onclick="return confirm('Удалить администратора?');" href="<%= baseUrl + "addadm?removeAdm=1&id=" + id%>"></a>
                        <%}%>
                    </td>
                </tr>
                <%}%>
                <tr>
            <form class="form_hide none" action="<%= baseUrl + "addadm"%>" method="post">
                <td class="ta-r"></td>
                <td>
                    <div class="btn_add_mech none">
                        <input class="auth_rtrn_checkValue" type="text" name="login" />
                    </div>
                </td>
                <td>
                    <div class="btn_add_mech none">
                        <input class='va-m auth_rtrn' type="checkbox" checked value="1" name="auth_via_ldap"/>
                        <label class='label__sub'>Доменная авторизация РТРС</label>

                        <div class="auth_rtrn_pass mt-10">
                            <label class="icon icon-genPass js-tooltip va-m" title="Сгенерировать пароль" onclick="generatePass('#admin_pass')"></label>
                            <input class="wp-170 js-tooltip mb-5" title="Пароль не должен содержать символы &quot;, ', <, >," type="text" name="password" id="admin_pass"/>
                        </div>
                    </div>
                </td>
                <td class="ta-c">
                    <div class="btn_add icon icon-add"></div>
                    <div class="btn_add_mech none">
                        <input hidden type="text" name="ADD_ME" value="1"/>
                        <input class="icon icon-save js-tooltip mt-15" type="submit" value="" title="Сохранить" name="submit" />
                    </div>
                </td>
            </form>
            </tr>
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
            <input class="wp-210 js-tooltip mb-5" title="Пароль не должен содержать символы &quot;, ', <, >," type="text" name="password" id="admin_pass"/>
            <br>
            <label class='label__sub'>Доменная авторизация</label>
            <input class='va-m' type="checkbox"/>
        </div>
        <input hidden type="text" name="TARGET_ADMIN_ID" value="<%=adminId%>"/>
        <input class="btn blbc white mt-15" type="submit" value="Изменить" name="submit" />
    </form>

    <%}%>
</div>
</body>
</html>