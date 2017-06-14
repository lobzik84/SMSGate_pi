<%@page import="org.lobzik.smspi.pi.BoxCommonData"%>
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
    boolean isRoot = (adminId == BoxCommonData.ROOT_ADMIN_ID);
    boolean isPassChanged = false;
    boolean isAccessError = false;
    if (JspData != null) {
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
        <p class="title mt-15">Данные изменены!</p>
        <%} else if (isAccessError) {%>
        <h2>Вы можете изменить только свои данные. Изменять данные других администраторов может только root администратор</h2>
        <%}%>
        <%if (isRoot) {%>

        <table class="table mt-20">
            <thead>
                <tr>
                    <td class="bold w-5 ta-c">
                        №
                    </td>
                    <td class="w-25">
                        Логин
                    </td>
                    <td class="w-65">
                        Телефон
                    </td>
                    <td class="w-5 ta-c" colspan="3">
                        Управление
                    </td>
                </tr>
            </thead>
            <tbody>
                <%for (HashMap hm : admList) {
                        int id = Tools.parseInt(hm.get("admin_id"), -1);
                        String login = Tools.getStringValue(hm.get("login"), "");
                        String phone = Tools.maskPhone(Tools.getStringValue(hm.get("phone_number"), ""), BoxCommonData.PHONE_MASK);
                        int status = Tools.parseInt(hm.get("status"), -1);
                        String auth_via_ldap = (Tools.parseInt(hm.get("auth_via_ldap"), -1) == 1) ? "checked" : "";
                %>
                <tr>
                    <td class="ta-c"><%= id%></td>
                    <td>
                        <%= login%>
                    </td>
            <form id="edit_form" action="<%= baseUrl + "chpass"%>" method="post">
                <td class="form_hide-assist">
                    <p class="form_hide"><%= phone%></p>
                    <div class="form_hide-table none">
                        <input type="text" class="phone__mask wp-170 mr-15 inline-b" placeholder="+7 (___) ___-__-__" name="phone_number" value="<%= phone%>">
                        <div class="va-t wp-240 inline-b">
                            <input class='va-m auth_rtrn' type="checkbox" value="1" name="auth_via_ldap" <%=auth_via_ldap%>/>
                            <label class='label__sub'>Доменная авторизация РТРС</label>
                            <input hidden type="text" name="TARGET_ADMIN_ID" value="<%=id%>"/>
                            <div class="auth_rtrn_pass mt-10">
                                <label class="icon icon-genPass js-tooltip va-m" title="Сгенерировать пароль" onclick="generatePass('#admin_pass_<%=id%>')"></label>
                                <input id="admin_pass_<%=id%>" class="wp-170 js-tooltip mb-5" title="Пароль не должен содержать символы &quot;, ', <, >," type="text" name="password" placeholder="Пароль"/>
                            </div>
                        </div>
                    </div>
                </td>
                <td class="btn_hide-save ta-c wp-30">
                    <div class="none btn__save">
                        <input class="icon icon-save js-tooltip va-m" title="Сохранить" type="submit" value="" name="submit" />
                    </div>
                </td>
                <td class="btn_hide-parent ta-c wp-30">
                    <div class="btn_hide-table va-m icon icon-edit js-tooltip inline-b" title="Редактировать"></div>
                </td>
            </form>
            <td class="ta-c wp-30">
                <%if (id != 1) {%>
                <a class="btn_delete inline-b" onclick="return confirm('Удалить администратора?');" href="<%= baseUrl + "addadm?removeAdm=1&id=" + id%>"></a>
                <%}%>
            </td>
            </tr>
            <%}%>
            <tr>
            <form class="form_hide none" action="<%= baseUrl + "addadm"%>" method="post">
                <td class="ta-c"></td>
                <td>
                    <div class="btn_add_mech none">
                        <input class="auth_rtrn_checkValue" type="text" name="login" />
                    </div>
                </td>
                <td>
                    <div class="btn_add_mech none">
                        <div class="inline-b va-t mr-15">
                            <input class="wp-170 phone__mask auth_rtrn_checkValue" type="text" placeholder="+7 (___) ___-__-__" name="phone_number" />
                        </div>
                        <div class="inline-b">
                            <input class='va-m auth_rtrn' type="checkbox" checked value="1" name="auth_via_ldap"/>
                            <label class='label__sub'>Доменная авторизация РТРС</label>
                            <div class="auth_rtrn_pass mt-10">
                                <label class="icon icon-genPass js-tooltip va-m" title="Сгенерировать пароль" onclick="generatePass('#admin_pass')"></label>
                                <input id="admin_pass" class="wp-170 js-tooltip mb-5" title="Пароль не должен содержать символы &quot;, ', <, >," type="text" name="password" placeholder="Пароль"/>
                            </div>
                        </div>
                    </div>
                </td>
                <td class="ta-c wp-30">
                    <p class="btn_add_mech btn__save va-t none">
                        <input hidden type="text" name="ADD_ME" value="1"/>
                        <input class="btn_add_mech va-t none icon icon-save va-m js-tooltip" type="submit" value="" title="Сохранить" name="submit" />
                    </p>
                </td>
                <td class="wp-30"></td>
                <td class="ta-c wp-30">
                    <div class="btn_add icon va-m icon-add"></div>
                </td>
            </form>
            </tr>
            </tbody>
        </table>

        <%} else {
            String phone = "";
            String login = "";
            String auth_via_ldap = "";
            int id = 0;
            for (HashMap hm : admList) {
                id = Tools.parseInt(hm.get("admin_id"), -1);
                login = Tools.getStringValue(hm.get("login"), "");
                phone = Tools.maskPhone(Tools.getStringValue(hm.get("phone_number"), ""), BoxCommonData.PHONE_MASK);
                //int status = Tools.parseInt(hm.get("status"), -1);
                auth_via_ldap = (Tools.parseInt(hm.get("auth_via_ldap"), -1) == 1) ? "checked" : "";
                if (Tools.parseInt(hm.get("admin_id"), -1) == adminId) {
                    break;
                }
            }
        %>

        <p class="label_wrong">Только root администратор может добавлять новых администраторов!</p>
        <p class="label_wrong mb-30">Вы можете изменить свои данные.</p>

        <table class="table mt-20">
            <thead>
                <tr>
                    <td class="bold w-5 ta-c">
                        №
                    </td>
                    <td class="w-25">
                        Логин
                    </td>
                    <td class="w-65">
                        Телефон
                    </td>
                    <td class="w-5 ta-c" colspan="2">
                        Управление
                    </td>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td class="ta-c"><%=adminId%></td>
                    <td>
                        <%=login%>
                    </td>

            <form id="edit_form" action="<%= baseUrl + "chpass"%>" method="post">
                <td class="form_hide-assist">
                    <p><%= phone%></p>
                    <div class="form_hide-table none">
                        <!--<input type="text" class="phone__mask wp-170 mr-15 inline-b" placeholder="+7 (___) ___-__-__" name="phone_number" value="<%=phone%>">-->
                        <div class="va-t wp-240 inline-b">
                            <input class='va-m auth_rtrn' <%=auth_via_ldap%> type="checkbox" name="auth_via_ldap" />
                            <label class='label__sub'>Доменная авторизация РТРС</label>
                            <input hidden type="text" name="TARGET_ADMIN_ID" value="<%=adminId%>"/>
                            <div class="auth_rtrn_pass mt-10">
                                <label class="icon icon-genPass js-tooltip va-m" title="Сгенерировать пароль" onclick="generatePass('#admin_pass_<%=adminId%>')"></label>
                                <input class="wp-170 js-tooltip mb-5" title="Пароль не должен содержать символы &quot;, ', <, >," type="text" name="password" id="admin_pass_<%=adminId%>"/>
                            </div>
                        </div>
                    </div>
                </td>
                <td class="btn_hide-save ta-c wp-30">
                    <div class="none btn__save">
                        <input class="icon icon-save js-tooltip ml-10 va-m" title="Сохранить" type="submit" value="" name="submit" />
                    </div>
                </td>
                <td class="btn_hide-parent ta-c wp-30">
                    <div class="btn_hide-table va-m icon icon-edit js-tooltip inline-b" title="Редактировать"></div>
                </td>
            </form>
            </tr>
            </tbody>
        </table>

        <%}%>
    </div>
</div>
</body>
</html>