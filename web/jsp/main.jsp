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
    if (JspData != null) {
        msgSent = Tools.parseInt(JspData.get("msgSent"), -1);
        msgErrs = Tools.parseInt(JspData.get("msgErrs"), -1);
        msgInbox = Tools.parseInt(JspData.get("msgInbox"), -1);
        msgSentDaily = Tools.parseInt(JspData.get("msgSentDaily"), -1);
        logData = (ArrayList<HashMap>) Tools.isNull(JspData.get("logData"), new ArrayList<HashMap>());
    }
%>

<jsp:include page="header.jsp" />

<div class="content__top">
    <div id="container" class="inline-b" style="height: 400px; width: 800px;"></div>

    <div class="inline-b wbc">
        <h4>Отправленных сообщений всего: <%= msgSent%></h4>
        <h4>Отправленных сообщений за сегодня: <%= msgSentDaily%></h4>
        <h4>Ошибок: <%= msgErrs%></h4>
        <h4>Принятых сообщений: <%= msgInbox%></h4>
    </div>
</div>

<div class="content wbc">
    <h5>Отправка сообщения:</h5>
    <form action="<%= baseUrl + "sendmsg"%>" method="post">
        Phone:<input type="text" name="recipient" /><br>
        Text:<textarea name="sms" rows = "5"></textarea><br> 
        <input type="submit" value="Send" name="submit" />
    </form>
</div>

<div class="content wbc">
    <h5>Лог шлюза:</h5>
    <table class="table mt-20">
        <thead>
            <tr>
                <td class="w-10">
                    Module_name
                </td>
                <td class="w-60">
                    Dated
                </td>
                <td class="w-30">
                    Level
                </td>
                <td class="w-30">
                    Message
                </td>
            </tr>
        </thead>
        <tbody>
            <%for (HashMap hm : logData) {
                    String moduleName = Tools.getStringValue(hm.get("module_name"), "");
                    String dated = Tools.getStringValue(hm.get("dated"), "");
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
                text: 'Диаграмма отправки/приема СМС'
            },
            series: [{
                    type: 'column',
                    name: 'Колличество отправленных СМС',
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
                    name: 'Колличество принятых СМС',
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

