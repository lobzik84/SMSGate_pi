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

    ArrayList<HashMap> rcpnts = new ArrayList();
    ArrayList<HashMap> groups = new ArrayList();

    if (JspData != null) {
        groups = (ArrayList<HashMap>) Tools.isNull(JspData.get("GROUPS"), new ArrayList());
        rcpnts = (ArrayList<HashMap>) Tools.isNull(JspData.get("RCPNTS"), new ArrayList());
    }
%>
<jsp:include page="header.jsp" />

<div class="content__layout">
    <div class="content__table">
        <p class="title">Получатели рассылок коротких сообщений</p>
        <table class="table mt-20">
            <thead>
                <tr>
                    <td class="bold w-5 ta-c">
                        №
                    </td>
                    <td class="w-25">
                        Номер
                    </td>
                    <td class="w-50">
                        ФИО
                    </td>
                    <td class="w-15">
                        Группа
                    </td>
                    <td class="w-5 ta-c" colspan="3">
                        Управление
                    </td>
                </tr>
            </thead>
            <tbody>
                <%for (HashMap hm : rcpnts) {
                    String number = Tools.maskPhone(Tools.getStringValue(hm.get("number"), ""), BoxCommonData.PHONE_MASK);
                %>
                <tr>
            <form id="" action="">
                <td class="ta-c"><%= Tools.parseInt(hm.get("id"), -1)%></td>
                <td id="rc_num_<%= Tools.parseInt(hm.get("id"), -1)%>" class="form_hide-assist">
                    <p class="form_hide"> <%=number%></p>
                    <div class="form_hide-table none">
                        <input type="text" class="phone__mask wp-170 mr-15 inline-b" placeholder="+7 (___) ___-__-__" value=" <%= number%>">
                    </div>
                </td>
                <td class="form_hide-assist">
                    <p class="form_hide"><%= Tools.getStringValue(hm.get("name"), "")%></p>
                    <div class="form_hide-table none">
                        <input type="text" class="wp-170 mr-15 inline-b" value="<%= Tools.getStringValue(hm.get("name"), "")%>">
                    </div>
                </td>
                <td class="form_hide-assist">
                    <p class="form_hide"><%= Tools.getStringValue(hm.get("group_name"), "")%></p>
                    <div class="form_hide-table none">
                        <select class="select wp-100" name="group_id">
                            <option value="">---</option>
                            <%for (HashMap gr : groups) {
                                    String selected = "";
                                    if (Tools.parseInt(gr.get("id"), 0) == Tools.parseInt(hm.get("group_id"), 0)) {
                                        selected = "selected";
                                    }
                            %>                
                            <option value="<%= Tools.parseInt(gr.get("id"), 0)%>" <%=selected%>><%= Tools.getStringValue(gr.get("group_name"), "")%></option>
                            <%}%>
                        </select>

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
            <%}%>
            <!--hardcode end-->

            <tr>
            <form class="form_hide none" action="" method="post">
                <td class="ta-c"></td>
                <td>
                    <div class="btn_add_mech none">
                        <input class="wp-170 phone__mask auth_rtrn_checkValue" placeholder="+7 (___) ___-__-__" type="text" name="" />
                    </div>
                </td>
                <td>
                    <div class="btn_add_mech none">
                        <input class="wp-170 auth_rtrn_checkValue" type="text" name="" />
                    </div>
                </td>
                <td>
                    <div class="btn_add_mech none">
                        <select class="select wp-100" name="group_id">
                            <option value="1">----></option>
                            <option value="2">123</option>
                            <option value="2">321</option>
                        </select>
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