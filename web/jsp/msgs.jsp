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

    ArrayList<HashMap> msgsList = new ArrayList<HashMap>();
    if (JspData != null) {
        msgsList = (ArrayList<HashMap>) Tools.isNull(JspData.get("MSGS_LIST"), new ArrayList<HashMap>());
    }
    HashMap filterList = new HashMap();
    String searchText = " ";
    String telNo = " ";
    String dateFrom = " ";
    String dateTo = " ";

%>
<jsp:include page="header.jsp" />

<div class="content__layout">
    
    <form id="filters_form" action="<%=baseUrl + "msgs"%>" method="POST">
        <% if (/*filterList.size() > 0*/true) {%>
        <%if (dateFrom != null && dateTo != null) {
            if (dateFrom.length() > 0 && dateTo.length() > 0) {%>
        <div class="">
            <label class="">Дата:</label>
            <label class="" for="date_from">С</label>
            <input id="date_from" name="date_from" class="" value="<%=dateFrom%>"/>
            <br/>
            <label class="" for="date_to">До</label>
            <input id="date_to" name="date_to" class="" value="<%=dateTo%>"/>
        </div>
        <%}
        }%>

        <%if (searchText != null && searchText.trim().length() > 0) {%>
        <div class="">
            <label class="" for="SearchText">Текст в сообщении:</label>
            <input class="" type = "text" id="SearchText" name="search_text" value="<%=searchText%>"/>
        </div>
        <%}%>

        <%if (telNo != null && telNo.trim().length() > 0) {%>
        <div class="">
            <label class="" for="SearchText">Номер телефона:</label>
            <input class="" type = "text" id="SearchText" name="search_text" value="<%=searchText%>"/>
        </div>
        <%}%>

        <div class="">
            <input id="analytics-submit--true" class="" type="submit" name="filter_submit" value="Построить"/>
        </div>
        <% }%>

    </form>
        
    <div class="content__table">

        <h2>Сообщения</h2>
        
        <table class="">
            <thead>
                <tr>
                    <td class="">
                        id
                    </td>
                    <td class="">
                        Message
                    </td>
                    <td class="">
                        Tel_no
                    </td>
                    <td class="">
                        Type
                    </td>
                    <td class="">
                        Date
                    </td>
                    <td class="">
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
                    <td><%= curMessage%></td>
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
