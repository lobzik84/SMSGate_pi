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

    ArrayList<HashMap> msgsList = null;
    if (JspData != null) {
        msgsList = (ArrayList<HashMap>) Tools.isNull(JspData.get("MSGS_LIST"), new ArrayList<HashMap>());
    } else {
        return;
    }

    HashMap filterList = (HashMap) Tools.isNull(JspData.get("FILTER_LIST"), new HashMap());
    String searchText = Tools.getStringValue(filterList.get("search_text"), "");
    String telNo = Tools.getStringValue(filterList.get("tel_no"), "");
    String dateFrom = Tools.getStringValue(filterList.get("date_from"), "");
    String dateTo = Tools.getStringValue(filterList.get("date_to"), "");


%>
<jsp:include page="header.jsp" />

<div class="content__layout">
    <div class="content__table">

        <p class="title mb-20">Сообщения</p>

        <form id="filters_form" action="<%=baseUrl + "msgs"%>" method="POST">
            <div class="inline-b va-t">
                <div class="inline-b mb-15">
                    <label class="label_inline" for="date_from">С</label>
                    <input id="date_from" class="input_date" name="date_from" value="<%=dateFrom%>"/>
                </div>
                <div class="inline-b mb-15 ml-15">
                    <label class="label_inline" for="date_to">До</label>
                    <input id="date_to" class="input_date" name="date_to" value="<%=dateTo%>"/>
                </div>
            </div>
            <div class="inline-b va-t ml-15">
                <div class="inline-b mb-15">
                    <label class="label_inline" for="SearchText">Текст в<br>сообщении</label>
                    <textarea id="SearchText" class="textarea__text" type="text" name="search_text" value="<%=searchText%>"></textarea>
                </div>
                <div class="inline-b mb-15 ml-15">
                    <label class="label_inline" for="TelNo">Номер<br>телефона</label>
                    <input id="SearchText" class="inline-b va-t" type="text" name="tel_no" value="<%=telNo%>"/>
                </div>
            </div>
            <input hidden type="text" name="FLTR_DATA" value="1"/>
            <br>
            <input id="analytics-submit--true" class="btn blbc white mr-15" type="submit" name="filter_submit" value="Фильтровать"/>
            <a class="btn_a" onclick="return confirm('Сбросить фильтры?');" href="<%= baseUrl + "msgs"%>">Сбросить фильтры</a>
        </form>

        <table class="table mt-20">
            <thead>
                <tr>
                    <td class="w-10">
                        id
                    </td>
                    <td class="w-35">
                        Message
                    </td>
                    <td class="w-15">
                        Phone
                    </td>
                    <td class="w-15">
                        Type
                    </td>
                    <td class="w-15">
                        Date
                    </td>
                    <td class="w-10">
                        Status
                    </td>
                </tr>
            </thead>
            <tbody>
                <%for (HashMap hm : msgsList) {
                        int id = Tools.parseInt(hm.get("id"), -1);
                        String curMessage = Tools.getStringValue(hm.get("message"), "");
                        String curTelNo = Tools.getStringValue(hm.get("tel_no"), "");
                        String curType = Tools.getStringValue(hm.get("type"), "");
                        String curDate = Tools.getFormatedDate((java.util.Date) hm.get("date"), "dd.MM.yyyy HH:mm:SS");
                        String curStatus = Tools.getStringValue(hm.get("status"), "");
                %>
                <tr>
                    <td><%= id%></td>
                    <td>
                        <p class="<%= curType%>">
                            <%= curMessage%>
                        </p>
                    </td>
                    <td><%= curTelNo%></td>
                    <td><%= curType%></td>
                    <td><%= curDate%></td>
                    <td><%= curStatus%></td>
                </tr>
                <%}%>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
