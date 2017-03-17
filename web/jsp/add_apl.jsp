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
        for (HashMap hm : appList) {
            String fullPublicKey = Tools.getStringValue(hm.get("public_key"), "");
            String shortPublicKey = fullPublicKey.substring(0, 5) + "..." + fullPublicKey.substring((fullPublicKey.length() - 5));
            hm.put("shortPublicKey", shortPublicKey);
        }
    }

%>
<jsp:include page="header.jsp" />

<div class="content__table">

    <h2>Зарегестрированные пользователи шлюза</h2>

    <input class="btn blbc white mt-20 mb-5" type="submit" value="Добавить нового пользователя"/>

    <form action="<%= baseUrl + "addapp"%>" method="post">
        <div class="inline-b">
            <label class="label mt-10">Name:</label>
            <input type="text" name="name" />
        </div>
        <div class="inline-b ml-10 mr-10 va-t">
            <label class="label mt-10">PublicKey:</label>
            <textarea type="text" name="public_key"></textarea>
        </div>
        <input hidden type="text" name="REG_ME" value="1"/>

        <input class="btn blbc white" type="submit" value="Добавить" name="submit" />
    </form>

    <table class="table mt-20">
        <thead>
            <tr>
                <td class="w-10">
                    ID
                </td>
                <td class="w-30">
                    name
                </td>
                <td class="w-60">
                    public_key
                </td>
            </tr>
        </thead>
        <tbody>
            <%for (HashMap hm : appList) {
                    int id = Tools.parseInt(hm.get("id"), -1);
                    String fullPublicKey = Tools.getStringValue(hm.get("public_key"), "");
                    String shortPublicKey = Tools.getStringValue(hm.get("shortPublicKey"), "");
                    String name = Tools.getStringValue(hm.get("name"), "");
            %>
            <tr>
                <td><%= id%></td>
                <td><%= name%></td>
                <td>
                    <p>
                        <%= shortPublicKey%>
                    </p>
                    <p>
                        <%= fullPublicKey%>
                    </p>
                </td>
            </tr>
            <%}%>
        </tbody>
    </table>
</div>
</body>
</html>
