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

    ArrayList<HashMap> appList = new ArrayList<HashMap>();
    if (JspData != null) {
        appList = (ArrayList<HashMap>) Tools.isNull(JspData.get("APP_LIST"), new ArrayList<HashMap>());
    }
%>
<jsp:include page="header.jsp" />
<br>
<h3>Зарегестрированные пользователи шлюза</h3>
<table border ="1" >
    <thead>
        <tr>
            <td>
                ID
            </td>
            <td>
                public_key 
            </td>
            <td>
                name 
            </td>
        </tr>
    </thead>
    <tbody>
        <%for (HashMap hm : appList) {
                int id = Tools.parseInt(hm.get("id"), -1);
                String publicKey = Tools.getStringValue(hm.get("public_key"), "");
                String name = Tools.getStringValue(hm.get("name"), "");
        %>
        <tr>
            <td><%= id%></td>
            <td><%= publicKey%></td>
            <td><%= name%></td>
        </tr>
        <%}%>
    </tbody>
</table>
<br>
<br>
<h3>Добавить нового пользователя</h3>

<form action="<%= baseUrl + "addapp"%>" method="post">
    Name:<input type="text" name="name" /><br>
    PublicKey:<input type="text" name="public_key" /><br>
    <input hidden type="text" name="REG_ME" value="1"/>
    <input type="submit" value="Добавить" name="submit" />
</form>
<br>
<br>
</body>
</html>
