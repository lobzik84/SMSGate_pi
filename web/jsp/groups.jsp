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
                    <td class="w-50">
                        Описание
                    </td>
                    <td class="w-20 ta-c">
                        Управление
                    </td>
                </tr>
            </thead>
            <tbody>
                <%for (HashMap hm : groups) {
                %>
                <tr>
                    <td class="ta-c"><%= Tools.parseInt(hm.get("id"), -1)%></td>
                    <td>
                        <%= Tools.getStringValue(hm.get("name"), "")%>
                    </td>
                    <td>
                        <%= Tools.getStringValue(hm.get("description"), "")%>
                    </td>
                    <td class="ta-c">
                        <input class="btn_hide-table icon icon-edit js-tooltip" title="Редактировать" type="submit" value=""/>
                        <a class="btn_delete" onclick="return confirm('Удалить группу?');" href="<%= baseUrl + "dogroup?removeGrp=1&id=" + hm.get("id")%>"></a>
                    </td>
                </tr>
                <%}%>
                <tr>
            <form class="form_hide none" action="" method="post">
                <td class="ta-c"></td>
                <td>
                    <div class="btn_add_mech none">
                        <input class="auth_rtrn_checkValue" type="text" name="login" />
                    </div>
                </td>
                <td>
                    <div class="btn_add_mech w-100 none">
                        <textarea name="" id="" cols="30" rows="10"></textarea>
                    </div>
                </td>
                <td class="ta-c">
                    <p class="btn_add_mech va-t mr-5 none">
                        <input hidden type="text" name="ADD_ME" value="1"/>
                        <input class="btn_add_mech va-t mr-5 none icon icon-save va-m js-tooltip" type="submit" value="" title="Сохранить" name="submit" />
                    </p>
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