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

    ArrayList<HashMap> groups = new ArrayList();

    if (JspData != null) {
        groups = (ArrayList<HashMap>) Tools.isNull(JspData.get("GROUPS"), new ArrayList());
    }
%>
<jsp:include page="header.jsp" />

<div class="content__layout">
    <div class="content__table">
        <p class="title">Группы получателей рассылок коротких сообщений</p>
        <table class="table mt-20">
            <thead>
                <tr>
                    <td class="bold w-5 ta-c">
                        №
                    </td>
                    <td class="w-25">
                        Название
                    </td>
                    <td class="w-75">
                        Описание
                    </td>
                    <td class="w-5 ta-c" colspan="3">
                        Управление
                    </td>
                </tr>
            </thead>
            <tbody>
                <%for (HashMap hm : groups) {
                        int grId = Tools.parseInt(hm.get("id"), -1);
                %>
                <tr>
            <form action="<%= baseUrl + "editgroup"%>" method="post">
                <input type="hidden" name="id" value="<%=grId%>" />
                <td class="ta-c"><%= grId%></td>
                <td id="" class="form_hide-assist">
                    <p class="form_hide"><%= Tools.getStringValue(hm.get("group_name"), "")%></p>
                    <div class="form_hide-table none">
                        <input type="text" class="wp-170 mr-15 inline-b" name="group_name" value="<%= Tools.getStringValue(hm.get("group_name"), "")%>">
                    </div>
                </td>
                <td class="form_hide-assist">
                    <p class="form_hide"><%= Tools.getStringValue(hm.get("description"), "")%></p>
                    <div class="form_hide-table none">
                        <textarea name="description" id="" cols="30" rows="10"><%= Tools.getStringValue(hm.get("description"), "")%></textarea>
                    </div>
                </td>
                <td class="ta-c btn_hide-save wp-30">
                    <div class="none">
                        <input class="icon icon-save js-tooltip va-m" title="Сохранить" type="submit" value="" name="submit" />
                    </div>
                </td>
                <td class="ta-c btn_hide-parent wp-30">
                    <div class="btn_hide-table va-m icon icon-edit js-tooltip inline-b" title="Редактировать"></div>
                </td>
                <td class="ta-c wp-30">
                    <a class="btn_delete" onclick="return confirm('Удалить?');" href="<%= baseUrl + "delgroup?id=" + grId%>"></a>
                </td>
            </form>
            </tr>

            <%}%>

            <tr>
            <form class="form_hide none" action="<%= baseUrl + "addgroup"%>" method="post">
                <td class="ta-c"></td>
                <td>
                    <div class="btn_add_mech none">
                        <input class="wp-170 auth_rtrn_checkValue" type="text" name="group_name" />
                    </div>
                </td>
                <td>
                    <div class="btn_add_mech w-100 none">
                        <textarea name="description" id="" cols="30" rows="10"></textarea>
                    </div>
                </td>
                <td class="ta-c wp-30">
                    <p class="btn_add_mech va-t none">
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
    </div>


</div>
</body>
</html>