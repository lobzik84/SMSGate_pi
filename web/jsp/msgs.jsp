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

        <h2 class="mb-20">Сообщения</h2>

        <form id="filters_form" action="<%=baseUrl + "msgs"%>" method="POST">
            <div class="inline-b mb-15">
                <label class="label" for="date_from">С</label>
                <input id="date_from" name="date_from" value="<%=dateFrom%>"/>

                <label class="label" for="date_to">До</label>
                <input id="date_to" name="date_to" value="<%=dateTo%>"/>
            </div>
            <div class="inline-b mb-15">
                <label class="label" for="SearchText">Текст в сообщении:</label>
                <input type = "text" id="SearchText" name="search_text" value="<%=searchText%>"/>

                <label class="label" for="TelNo">Номер телефона:</label>
                <input type = "text" id="SearchText" name="tel_no" value="<%=telNo%>"/>
            </div>
            <input hidden type="text" name="FLTR_DATA" value="1"/>
            <br>
            <input id="analytics-submit--true" class="btn blbc white mr-15" type="submit" name="filter_submit" value="Фильтровать"/>
            <a class="btn p-9 lbc black tdn inline-b" onclick="return confirm('Сбросить фильтры?');" href="<%= baseUrl + "msgs"%>">Сбросить фильтры</a>
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
                    <td class="<%= curType%>"><%= curMessage%></td>
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

<script type="text/javascript">
    $(document).ready(function () {
        $('#date_from').datepicker({
            onClose: function (selectedDate) {
                $('#date_to').datepicker("option", "minDate", selectedDate);
            }
        });
        $('#date_to').datepicker({
            onClose: function (selectedDate) {
                $('#date_from').datepicker("option", "maxDate", selectedDate);
            }
        });

    <%if (!dateFrom.isEmpty()) {%>
        $('#date_to').datepicker("option", "minDate", '<%=dateFrom%>');
    <%}%>
    <%if (!dateTo.isEmpty()) {%>
        $('#date_from').datepicker("option", "maxDate", '<%=dateTo%>');
    <%}%>
    });
</script>
</body>
</html>
