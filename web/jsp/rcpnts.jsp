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
                    <td class="w-50">
                        Группа
                    </td>
                </tr>
            </thead>
            <tbody>
                <%for (HashMap hm : rcpnts) {
                %>
                <tr>
                    <td class="ta-c"><%= Tools.parseInt(hm.get("id"), -1)%></td>
                    <td>
                        <%= Tools.getStringValue(hm.get("number"), "")%>
                    </td>
                    <td>
                        <%= Tools.getStringValue(hm.get("name"), "")%>
                    </td>
                     <td>
                         <select name="group_id">
                             <option value="">---></option>
                             <%for (HashMap gr : groups) { 
                                String selected = "";
                                if (Tools.parseInt(gr.get("id"), 0) == Tools.parseInt(hm.get("group_id"), 0)) {
                                    selected = "selected";
                                }
                             %>                
                             <option value="<%= Tools.parseInt(gr.get("id"), 0)%>" <%=selected%>><%= Tools.getStringValue(gr.get("name"), "")%></option>
                             <%}%>
                         </select>
                    </td>
                </tr>
                <%}%>
            </tbody>
        </table>
    </div>


</div>
</body>
</html>