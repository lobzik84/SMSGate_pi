<%@page import="org.lobzik.smspi.pi.BoxSettingsAPI"%>
<%@page import="org.lobzik.home_sapiens.entity.Measurement"%>
<%@page import="org.lobzik.smspi.pi.MessageStatus"%>
<%@page import="org.lobzik.home_sapiens.entity.Parameter"%>
<%@page import="org.lobzik.smspi.pi.AppData"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.HashMap"%>
<%@page import="org.lobzik.tools.Tools"%>
<%@page import="org.lobzik.smspi.pi.modules.ModemModule"%>
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

    HashMap rusMsgType = new HashMap();

    rusMsgType.put("outbox", "Исходящее");
    rusMsgType.put("inbox", "Входящее");

    Parameter operP = AppData.parametersStorage.getParameterByAlias("MODEM_OPERATOR");
    String operator = "";
    if (AppData.measurementsCache.getLastMeasurement(operP) != null) {
        operator = AppData.measurementsCache.getLastMeasurement(operP).toStringValue();
    }

    Parameter numP = AppData.parametersStorage.getParameterByAlias("MODEM_NUMBER");
    String simNumber = "";
    if (AppData.measurementsCache.getLastMeasurement(numP) != null) {
        simNumber = "+" + AppData.measurementsCache.getLastMeasurement(numP).toStringValue();
    }

    Parameter balP = AppData.parametersStorage.getParameterByAlias("MODEM_BALANCE");
    String simBalance = "";
    Measurement balM = AppData.measurementsCache.getLastMeasurement(balP);
    String simBalanceHint = "";
    if (balM != null) {
        simBalanceHint = "Баланс проверен в " + Tools.getFormatedDate(new Date(balM.getTime()), "HH:mm dd MMM");
        simBalance = balM.toStringValue();

    }

%>

<jsp:include page="header.jsp" />

<div class="content__layout_main">
    <div class="content__top">
        <div class="chart">
            <div class="mt-15 ml-20 ta-c">
                <label class="inline-b os-r">
                    <span>Группировать: </span>
                </label>
                <div class="radio-group">
                    <input id="grouping_hour" class="input__radio none" type="radio" name="grouping" value="hour"/>
                    <label class="radio-group__item" for="grouping_hour">
                        <span class="radio-group__label">По часам</span>
                    </label>   
                    <input id="grouping_day" class="input__radio none" type="radio" name="grouping" value="day" checked/>
                    <label class="radio-group__item" for="grouping_day">
                        <span class="radio-group__label">По дням</span>
                    </label>   
                    <input id="grouping_week" class="input__radio none" type="radio" name="grouping" value="week"/>
                    <label class="radio-group__item " for="grouping_week">
                        <span class="radio-group__label">По неделям</span>
                    </label>    
                    <input id="grouping_month" class="input__radio none" type="radio" name="grouping" value="month"/>
                    <label class="radio-group__item " for="grouping_month">
                        <span class="radio-group__label">По месяцам</span>
                    </label>
                </div>
            </div>
            <div id="container2" class="w-25 inline-b"></div>
            <div id="container1" class="w-70 inline-b"></div>
        </div>
        <div>
            <form id="pie_data" action="#" method="POST">
                <input id="start" name="start" type="text" hidden>
                <input id="end" name="end" type="text" hidden>
            </form>
        </div><!--
        --><div class="chart__total">
            <div class="chart__mb">
                <div class="mb__top">
                    <div class="mb__top_1"></div>
                    <div class="mb__top_2"></div>
                    <div class="mb__top_3"></div>
                    <div class="mb__top_4"></div>
                    <div class="mb__top_5"></div>
                    <div class="mb__top_6"></div>
                    <div class="mb__top_7"></div>
                    <div class="mb__top_8"></div>
                    <div class="mb__top_9"></div>
                    <div class="mb__top_10"></div>
                    <div class="mb__top_11"></div>
                    <div class="mb__top_12"></div>
                    <div class="mb__top_13"></div>
                    <div class="mb__top_14"></div>
                </div>
                <div class="mb__text">
                    <p class="mb__operator"><%=operator%></p>
                    <p class="chart__balance js-tooltip" title="<%=simBalanceHint%>"><%=simBalance%>
                        <% if (simBalance.length() > 0) { %><span class="rouble">i</span> <%}%>
                    </p>
                    <p class="chart__phone"><%=simNumber%></p>
                </div>
            </div>
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
        <p class="title">Сообщения</p>
        <table class="table__messages">
            <thead>
            </thead>
            <tbody>
                <%for (HashMap hm : messageList) {
                        int id = Tools.parseInt(hm.get("id"), -1);
                        String message = Tools.getStringValue(hm.get("message"), "");
                        String telNo = Tools.getStringValue(hm.get("tel_no"), "");
                        String type = Tools.getStringValue(hm.get("type"), "");
                        String date = Tools.getFormatedDate((java.util.Date) hm.get("date"), "dd.MM.yyyy HH:mm:ss");
                        MessageStatus ms = new MessageStatus(hm.get("status"));
                %>
                <tr>
                    <td class="w-5"><%= id%></td>
                    <td class="w-50 word-b"><%= message%></td>
                    <td class="w-20"><%= telNo%></td>
                    <td class="w-10">
                        <span class="js-tooltip message__icon message__icon_<%= type%>" title="<%= Tools.getStringValue(rusMsgType.get(type), "")%>"></span>
                    </td>
                    <td class="w-15"><%= date%></td>
                    <td class="w-5">
                        <span class="js-tooltip message__status message__status_<%= type%>-<%= ms.CSSClass()%> message__status_<%= ms.CSSClass()%>" title="<%= ms.rus()%>"></span>
                    </td>
                </tr>
                <%}%>
            </tbody>
        </table>

        <p class="title mt-20">Журнал службы <a class="icon icon-download ml-5 js-tooltip" title="Скачать" href="<%=request.getContextPath()%>/file/sms_service.log"></a></p>
        <div class="log">
            <%for (HashMap hm : logData) {
                    String moduleName = Tools.getStringValue(hm.get("module_name"), "");
                    String dated = Tools.getFormatedDate((java.util.Date) hm.get("dated"), "dd.MM.yyyy HH:mm:ss");
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
        <p class="title">Отправить сообщение</p>
        <form class="block w-100" action="<%= baseUrl + "sendmsg"%>" method="post">
            <label class="label lgray mt-20">Номер телефона</label>
            <input class="input_phone" type="text" name="recipient" placeholder="+7 (___) ___-__-__"/>
            <label class="label fl-l lgray mt-20">Текст</label>
            <label class="label label_blue">Тестовое сообщение</label>
            <span class="counter" id="max_msg_length"><%=BoxSettingsAPI.getInt("MaxMessageLength")%></span>
            <textarea class="textarea__message" name="sms"></textarea>
            <input class="btn blbc white mt-20" type="submit" value="Отправить" name="submit" />
        </form>
    </div>
</div>



<script type="text/javascript">



    $.getJSON("<%= request.getContextPath() + "/HighchartsJsonServlet/smsbydate"%>", function (data) {

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
            },
            global: {
                useUTC: false
            }
        });

        var _chart = new Highcharts.stockChart({
            chart: {
                alignTicks: false,
                renderTo: 'container1'
            },
            rangeSelector: {
                buttons: [{
                        type: 'month',
                        count: 1,
                        text: '1 мес'
                    }, {
                        type: 'month',
                        count: 3,
                        text: '3 мес'
                    }, {
                        type: 'month',
                        count: 6,
                        text: '6 мес'
                    }, {
                        type: 'ytd',
                        text: 'Текущий год'
                    }, {
                        type: 'year',
                        text: '1 год'
                    }, {
                        type: 'all',
                        text: 'Всё'
                    }],
                buttonTheme: {
                    width: 70
                },
                selected: 5
            },
            title: {
                text: ''
            },
            xAxis: {
                events: {
                    afterSetExtremes: function () {
                        min = this.min;
                        max = this.max;
                        $('#start').attr('value', min);
                        $('#end').attr('value', max);
                        getDataAndDrowPie(min, max);
                    }
                }
            },
            series: [{
                    type: 'column',
                    name: 'Отправлено',
                    data: data.data1,
                    dataGrouping: {
                        enabled: true,
                        forced: true,
                        dateTimeLabelFormats: {day: ['За день - %A, %b %e, %Y']},
                        units: [[
                                'day',
                                [1]
                            ]]
                    }
                },
                {type: 'column',
                    name: 'Принято',
                    data: data.data2,
                    dataGrouping: {
                        enabled: true,
                        forced: true,
                        dateTimeLabelFormats: {day: ['За день - %A, %b %e, %Y']},
                        units: [[
                                'day', // unit name
                                [1] // allowed multiples
                            ]]
                    }
                }]
        });

        //работа с группировками
        $('input[name=grouping]').change(function () {
            var unit = $(this).val();
            _chart.series.forEach(function (ser) {
                if (unit === "week") {
                    ser.update({
                        dataGrouping: {
                            forced: true,
                            dateTimeLabelFormats: {week: ['Неделя с %A, %b %e, %Y', '%A, %b %e', '-%A, %b %e, %Y']},
                            units: [[unit, [1]]]
                        }
                    }, false);
                } else {
                    ser.update({
                        dataGrouping: {
                            forced: true,
                            units: [[unit, [1]]]
                        }
                    }, false);
                }
            });
            _chart.redraw();
        });

        //Пирог
        min = _chart.xAxis[0].getExtremes().min;
        max = _chart.xAxis[0].getExtremes().max;
        $('#start').attr('value', min);
        $('#end').attr('value', max);

        function getDataAndDrowPie(min, max) {

            $.ajax({
                type: 'POST',
                url: '<%=request.getContextPath()%>/HighchartsJsonServlet/smsbyusers',
                data: {start: min, end: max},
                dataType: 'json'
            }).done(function (data) {
                console.log(data.SC);
                console.log(data.RESULT);

                Highcharts.chart('container2', {
                    chart: {
                        plotBackgroundColor: null,
                        plotBorderWidth: null,
                        plotShadow: false,
                        type: 'pie',
//                        events: {
//                            load: function (event) {
//                                var total = 0;
//                                for (var i = 0, len = this.series[0].yData.length; i < len; i++) {
//                                    total += this.series[0].yData[i];
//                                }
//                                var text = this.renderer.text(
//                                        'Всего задач: ' + total,
//                                        this.plotLeft + 480,
//                                        this.plotTop + 30
//                                        ).attr({
//                                    zIndex: 5
//                                }).add()
//                            }
//                        }
                    },
                    exporting: {
                        enabled: false
                    },
                    title: {
                        text: ''
                    },
                    tooltip: {
                        pointFormat: '{series.name}: <b>{point.percentage:.1f}% ({point.y})</b>'
                    },
                    plotOptions: {
                        pie: {
                            allowPointSelect: true,
                            cursor: 'pointer',
                            dataLabels: {
                                enabled: false, //отображает надписи с палками
                                format: '<b>{point.name}</b>: {point.percentage:.1f} %',
                                style: {
                                    color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                                }
                            }
                        }
                    },
                    series: [{
                            name: 'Сообщений',
                            colorByPoint: true,
                            data: data.RESULT
                        }],
                    lang: {
                        noData: "Нет данных для отображения"
                    },
                    noData: {
                        style: {
                            fontWeight: 'bold',
                            fontSize: '15px',
                            color: '#303030'
                        }
                    }
                });
            });
        }

        getDataAndDrowPie($('#start').val(), $('#end').val());

    });
</script>
</body>
</html>

