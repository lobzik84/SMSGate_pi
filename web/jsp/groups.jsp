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


                <!--hardcode-->

                <tr>
            <form id="" action="">
                <td class="ta-c">23</td>
                <td id="" class="form_hide-assist">
                    <p class="form_hide">Group1</p>
                    <div class="form_hide-table none">
                        <input type="text" class="wp-170 mr-15 inline-b" value="Group1">
                    </div>
                </td>
                <td class="form_hide-assist">
                    <p class="form_hide">Тестовая группа</p>
                    <div class="form_hide-table none">
                        <textarea name="" id="" cols="30" rows="10">Тестовая группа</textarea>
                    </div>
                </td>
                <td class="ta-c btn_hide-parent">
                    <div class="btn_hide-table va-m icon icon-edit js-tooltip inline-b" title="Редактировать"></div>
                </td>
                <td class="ta-c btn_hide-save">
                    <div class="none">
                        <input class="icon icon-save js-tooltip va-m" title="Сохранить" type="submit" value="" name="submit" />
                    </div>
                </td>
                <td class="ta-c">
                    <a class="btn_delete" onclick="return confirm('Удалить?');" href="#"></a>
                </td>
            </form>
            </tr>

            <!--hardcode end-->

            <tr>
            <form class="form_hide none" action="" method="post">
                <td class="ta-c"></td>
                <td>
                    <div class="btn_add_mech none">
                        <input class="wp-170 auth_rtrn_checkValue" type="text" name="login" />
                    </div>
                </td>
                <td>
                    <div class="btn_add_mech w-100 none">
                        <textarea name="" id="" cols="30" rows="10"></textarea>
                    </div>
                </td>
                <td></td>
                <td class="ta-c">
                    <p class="btn_add_mech va-t none">
                        <input hidden type="text" name="ADD_ME" value="1"/>
                        <input class="btn_add_mech va-t none icon icon-save va-m js-tooltip" type="submit" value="" title="Сохранить" name="submit" />
                    </p>
                </td>
                <td class="ta-c">
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