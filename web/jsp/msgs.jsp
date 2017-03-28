<%@page import="org.lobzik.smspi.pi.modules.ModemModule"%>
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

    HashMap msgStringStatuses = new HashMap();
    HashMap msgCssClassStatuses = new HashMap();
    msgStringStatuses.put(ModemModule.STATUS_NEW, "Новое");
    msgCssClassStatuses.put(ModemModule.STATUS_NEW, "new");
    msgStringStatuses.put(ModemModule.STATUS_SENT, "Отправлено");
    msgCssClassStatuses.put(ModemModule.STATUS_SENT, "sent");
    msgStringStatuses.put(ModemModule.STATUS_READ, "Прочитано");
    msgCssClassStatuses.put(ModemModule.STATUS_READ, "read");
    msgStringStatuses.put(ModemModule.STATUS_ERROR_SENDING, "Ошибка отправления");
    msgCssClassStatuses.put(ModemModule.STATUS_ERROR_SENDING, "error");
    msgStringStatuses.put(ModemModule.STATUS_ERROR_TOO_OLD, "Слишком старая");
    msgCssClassStatuses.put(ModemModule.STATUS_ERROR_TOO_OLD, "sad_pedobear");
    msgStringStatuses.put(ModemModule.STATUS_ERROR_ATTEMPTS_EXCEEDED, "Превышено колличество попыток отправки");
    msgCssClassStatuses.put(ModemModule.STATUS_ERROR_ATTEMPTS_EXCEEDED, "exceeded");
    msgStringStatuses.put(ModemModule.STATUS_SENDING, "Отправляется");
    msgCssClassStatuses.put(ModemModule.STATUS_SENDING, "sending");
%>

<jsp:include page="header.jsp" />

<div class="content__layout">
    <div class="content__block">

        <p class="title mb-20">Сообщения</p>

        <form id="filters_form" class="mb-30" action="<%=baseUrl + "msgs"%>" method="POST">
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

        <div class="content__messages">

            <%for (HashMap hm : msgsList) {
                    int id = Tools.parseInt(hm.get("id"), -1);
                    String curMessage = Tools.getStringValue(hm.get("message"), "");
                    String curTelNo = Tools.getStringValue(hm.get("tel_no"), "");
                    String curType = Tools.getStringValue(hm.get("type"), "");
                    String curDate = Tools.getFormatedDate((java.util.Date) hm.get("date"), "dd.MM.yyyy HH:mm:SS");
                    int curStatus = Tools.parseInt(hm.get("status"), -1);
            %>

            <div class="message message__<%= curType%>">
                <div class="message__block message__block_<%= curType%>">
                    <p class="message__phone"><%= curTelNo%></p>
                    <p class="message__text"><%= curMessage%></p>
                    <p class="message__date"> 
                        <span class="message__icon message__icon_<%= curType%>"></span>
                        <%= curDate%> 
                        <span class="message__status message__status_<%= Tools.getStringValue(msgCssClassStatuses.get(curStatus), "")%>"></span></p>
                </div>
            </div>

            <%
                }
            %>

        </div>
    </div>
</div>
</body>
</html>
