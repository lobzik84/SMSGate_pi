<%@page import="java.util.Date"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.HashMap"%>
<%@page import="org.lobzik.tools.Tools"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<jsp:useBean id="JspData" class="java.util.HashMap" scope="request"/>
<%
    int adminId = Tools.parseInt(request.getSession().getAttribute("AdminID"), -1);
    if (adminId < 0) {
        return;
    }

    String baseUrl = request.getContextPath() + request.getServletPath() + "/";

    int msgSent = 0;
    int msgSentDaily = 0;
    int msgErrs = 0;
    int msgInbox = 0;
    List<HashMap> logData = null;
    List<HashMap> messageList = null;
    if (JspData != null) {
        msgSent = Tools.parseInt(JspData.get("msgSent"), -1);
        msgErrs = Tools.parseInt(JspData.get("msgErrs"), -1);
        msgInbox = Tools.parseInt(JspData.get("msgInbox"), -1);
        msgSentDaily = Tools.parseInt(JspData.get("msgSentDaily"), -1);
        logData = (ArrayList<HashMap>) Tools.isNull(JspData.get("logData"), new ArrayList<HashMap>());
        messageList = (ArrayList<HashMap>) Tools.isNull(JspData.get("messageList"), new ArrayList<HashMap>());
    }
%>

<jsp:include page="header.jsp" />

<div class="content__layout">
    <div class="content__top mb-30">
        <div id="container" class="chart"></div><!--
        --><div class="chart__total">

            <p class="chart__total_label">Отправлено за сегодня:</p>
            <p class="chart__total_num"><%= msgSentDaily%></p>

            <p class="chart__total_label">Отправлено всего:</p>
            <p class="chart__total_num"><%= msgSent%></p>

            <p class="chart__total_label">Принято всего:</p>
            <p class="chart__total_num"><%= msgInbox%></p>

            <p class="chart__total_label">Ошибок:</p> 
            <p class="chart__total_num"><%= msgErrs%></p>

        </div>
    </div>

    <div class="content__block inline-b fl-l w-60">
        <h2>Лог шлюза:</h2>
        <table class="">
            <thead>
                <tr>
                    <td class="">
                        Module_name
                    </td>
                    <td class="">
                        Dated
                    </td>
                    <td class="">
                        Level
                    </td>
                    <td class="">
                        Message
                    </td>
                </tr>
            </thead>
            <tbody>
                <%for (HashMap hm : logData) {
                        String moduleName = Tools.getStringValue(hm.get("module_name"), "");
                        String dated = Tools.getFormatedDate((java.util.Date) hm.get("dated"), "dd.MM.yyyy HH:mm:SS");
                        String level = Tools.getStringValue(hm.get("level"), "");
                        String message = Tools.getStringValue(hm.get("message"), "");
                %>
                <tr>
                    <td><%= moduleName%></td>
                    <td><%= dated%></td>
                    <td><%= level%></td>
                    <td><%= message%></td>
                </tr>
                <%}%>
            </tbody>
        </table>
    </div>

    <div class="content__block inline-b fl-l w-60">
        <h2>Сообщения: </h2>
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
                <%for (HashMap hm : messageList) {
                        int id = Tools.parseInt(hm.get("id"), -1);
                        String message = Tools.getStringValue(hm.get("message"), "");
                        String telNo = Tools.getStringValue(hm.get("tel_no"), "");
                        String type = Tools.getStringValue(hm.get("type"), "");
                        String date = Tools.getFormatedDate((java.util.Date) hm.get("date"), "dd.MM.yyyy HH:mm:SS");
                        String status = Tools.getStringValue(hm.get("status"), "");                       
                %>
                <tr>
                    <td><%= id%></td>
                    <td><%= message%></td>
                    <td><%= telNo%></td>
                    <td><%= type%></td>
                    <td><%= date%></td>
                    <td><%= status%></td>
                </tr>
                <%}%>
            </tbody>
        </table>
    </div>

    <div class="content__block inline-b fl-r w-25">
        <h2>Отправить сообщение:</h2>
        <form action="<%= baseUrl + "sendmsg"%>" method="post">
            <label>Phone:</label>
            <input type="text" name="recipient" />

            <label>Text:</label>
            <span>Тестовое сообщение</span>
            <textarea name="sms"></textarea>

            <input class="btn blbc white" type="submit" value="Отправить" name="submit" />
        </form>
    </div>

</div>
<script type="text/javascript">
    $.getJSON("<%= request.getContextPath() + "/HighchartsJsonServlet"%>", function (data) {
        Highcharts.stockChart('container', {
            chart: {
                alignTicks: false
            },
            rangeSelector: {
                selected: 1
            },
            title: {
                text: 'Количество SMS'
            },
            series: [{
                    type: 'column',
                    name: 'Отправлено',
                    data: data.data1,
                    dataGrouping: {
                        approximation: "sum",
                        enabled: true,
                        forced: true,
                        units: [[
                                'hour', // unit name
                                [1] // allowed multiples
                            ], [
                                'day', // unit name
                                [1] // allowed multiples
                            ], [
                                'month',
                                [1]
                            ]]
                    }
                },
                {type: 'column',
                    name: 'Принято',
                    data: data.data2,
                    dataGrouping: {
                        approximation: "sum",
                        enabled: true,
                        forced: true,
                        units: [[
                                'hour', // unit name
                                [1] // allowed multiples
                            ], [
                                'day', // unit name
                                [1] // allowed multiples
                            ], [
                                'month',
                                [1]
                            ]]
                    }
                }]
        });
    });


</script>
</body>
</html>

