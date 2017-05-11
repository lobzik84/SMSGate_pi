<%@page import="org.lobzik.smspi.pi.MessageStatus"%>
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

    HashMap rusMsgType = new HashMap();


    rusMsgType.put("outbox", "Исходящее");
    rusMsgType.put("inbox", "Входящее");
%>

<jsp:include page="header.jsp" />

<div class="content__layout">
    <div class="content__block">

        <p class="title mb-20">Сообщения</p>

        <form id="filters_form" class="mb-30" action="<%=baseUrl + "msgs"%>" method="GET">
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
                    <textarea id="SearchText" class="textarea__text" name="search_text"><%=searchText%></textarea>
                </div>
                <div class="inline-b mb-15 ml-15">
                    <label class="label_inline" for="TelNo">Номер<br>телефона</label>
                    <input id="SearchText" class="inline-b va-t" type="text" name="tel_no" value="<%=telNo%>"/>
                </div>
            </div>
            <input hidden type="text" name="FLTR_DATA" value="1"/>
            <br>
            <input id="analytics-submit--true" class="btn blbc white mr-15" type="submit" name="filter_submit" value="Найти"/>
            <a class="btn_a" onclick="return confirm('Сбросить фильтры?');" href="<%= baseUrl + "msgs"%>">Сбросить фильтры</a>
        </form>

        <div class="content__messages">

            <%for (HashMap hm : msgsList) {
                    int id = Tools.parseInt(hm.get("id"), -1);
                    String curMessage = Tools.getStringValue(hm.get("message"), "");
                    String curTelNo = Tools.getStringValue(hm.get("tel_no"), "");
                    if (hm.get("name") != null) {curTelNo = hm.get("name") + " -> " + curTelNo;}
                    else if (hm.get("login") != null) {curTelNo = hm.get("login") + " -> " + curTelNo;}
                    else if ("outbox".equals(hm.get("type")) ){curTelNo = "smsity -> " + curTelNo;}
                    String curType = Tools.getStringValue(hm.get("type"), "");
                    String curDate = Tools.getFormatedDate((java.util.Date) hm.get("date"), "dd.MM.yyyy HH:mm:ss");
                    MessageStatus ms = new MessageStatus(hm.get("status"));
            %>

            <div class="message message__<%= curType%>">
                <div class="message__block message__block_<%= curType%>">
                    <p class="message__phone"><%= curTelNo%></p>
                    <p class="message__text"><%= curMessage%></p>
                    <p class="message__date"> 
                        <span class="js-tooltip message__icon_small message__icon_small-<%= curType%>" title="<%= Tools.getStringValue(rusMsgType.get(curType), "")%>"></span>
                        <%= curDate%> 
                        <span class="js-tooltip message__status message__status_<%= curType%>-<%= ms.CSSClass()%> message__status_<%= ms.CSSClass()%>" title="<%= ms.rus()%>"></span>
                    </p>
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
