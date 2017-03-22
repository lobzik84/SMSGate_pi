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
    <div class="content__block content__block_left">
        <h2>Сообщения</h2>
        <table class="table__messages">
            <thead>
                <tr>
                    <td class="w-5">
                        id
                    </td>
                    <td class="w-20">
                        Message
                    </td>
                    <td class="w-15">
                        Phone
                    </td>
                    <td class="w-10">
                        Type
                    </td>
                    <td class="w-10">
                        Date
                    </td>
                    <td class="w-5">
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
                    <td class="w-5"><%= id%></td>
                    <td class="w-30"><%= message%></td>
                    <td class="w-20"><%= telNo%></td>
                    <td class="w-10"><%= type%></td>
                    <td class="w-15"><%= date%></td>
                    <td class="w-5"><%= status%></td>
                </tr>
                <%}%>
            </tbody>
        </table>

        <div class="log">
            <%for (HashMap hm : logData) {
                    String moduleName = Tools.getStringValue(hm.get("module_name"), "");
                    String dated = Tools.getFormatedDate((java.util.Date) hm.get("dated"), "dd.MM.yyyy HH:mm:SS");
                    String level = Tools.getStringValue(hm.get("level"), "");
                    String message = Tools.getStringValue(hm.get("message"), "");
            %>
            <div class="log__row">
                <p class="log__date"><%= dated%></p>
                <p class="log__name_<%= level%>"><%= moduleName%> <%= level%>:</p>
                <p class="log__message"><%= message%></p>
            </div>
            <%}%>
        </div>

    </div>

    <div class="content__padding"></div>

    <div class="content__block content__block_right">
        <h2>Отправить сообщение:</h2>
        <form class="block w-100" action="<%= baseUrl + "sendmsg"%>" method="post">
            <label class="label lgray mt-20">Номер телефона</label>
            <input class="input_phone" type="text" name="recipient" placeholder="+7 (___) ___-__-__"/>
            <label class="label lgray mt-20">Текст</label>
            <label class="label label_blue">Тестовое сообщение</label>
            <span class="counter">70</span>
            <textarea class="textarea__message" name="sms"></textarea>
            <input class="btn blbc white mt-20" type="submit" value="Отправить" name="submit" />
        </form>
    </div>
</div>

<script type="text/javascript">
    $.getJSON("<%= request.getContextPath() + "/HighchartsJsonServlet"%>", function (data) {

        Highcharts.setOptions({
            lang: {
                loading: 'Загрузка...',
                months: ['Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь', 'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь'],
                weekdays: ['Воскресенье', 'Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота'],
                shortMonths: ['Янв', 'Фев', 'Март', 'Апр', 'Май', 'Июнь', 'Июль', 'Авг', 'Сент', 'Окт', 'Нояб', 'Дек'],
                exportButtonTitle: "Экспорт",
                printButtonTitle: "Печать",
                rangeSelectorFrom: "С",
                rangeSelectorTo: "По",
                rangeSelectorZoom: "Период",
                downloadPNG: 'Скачать PNG',
                downloadJPEG: 'Скачать JPEG',
                downloadPDF: 'Скачать PDF',
                downloadSVG: 'Скачать SVG',
                printChart: 'Напечатать график'
            }
        });

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

