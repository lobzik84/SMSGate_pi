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
            if (fullPublicKey.length() > 12) {
                String shortPublicKey = fullPublicKey.substring(0, 5) + "..." + fullPublicKey.substring((fullPublicKey.length() - 5));
                hm.put("shortPublicKey", shortPublicKey);
            }
        }
    }
%>
<jsp:include page="header.jsp" />

<div class="content__layout">
    <div class="content__table">

        <p class="title">Зарегистрированные пользователи шлюза</p>

        <input class="btn btn_hide mt-20 mb-5" type="submit" value="Добавить нового пользователя"/>

        <form class="form_hide none" action="<%= baseUrl + "addapp"%>" method="post">
            <div>
                <label class="label mt-10">Name</label>
                <input type="text" name="name" />
            </div>

            <p class="mt-15 os-r">
                <label class="label">Ключ RSA</label>
            <p class="label">Длина ключа 1024 бита, public exponent = 10001 (HEX)</p>
            <p class="label">PublicKey (modulus in HEX, 256 symbols)<span class="label_wrong pl-15">Input or Generate 256 SYMBOLS!</span></p>
            <div class="inline-b">
                <span class="label publicKey_copy mb-5" onclick="generateKey()">Сгенерировать</span>
            </div>
            <textarea class="textarea__key" name="public_key" id="public_key"></textarea>

            <div class="private_key mt-20">
                <label class="label">Generated PrivateKey (multiplicative inverse in HEX, 256 symbols)</label>
                <textarea class="textarea__key" name="private_key" id="private_key"></textarea>
            </div>
            <input hidden type="text" name="REG_ME" value="1"/>

            <input class="btn blbc white mt-20" type="submit" value="Добавить" name="submit" />

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
                    <td class="w-50">
                        public_key
                    </td>
                    <td class="w-10">
                        delete
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
                        <p class="publicKey_copy" title="Скопировать" data-key="">
                            <%= shortPublicKey.length() > 0 ? shortPublicKey : fullPublicKey%>
                        </p>
                        <p class="publicKey_full none">
                            <%= fullPublicKey%>
                        </p>
                    </td>
                    <td>
                        <a class="btn_delete" onclick="return confirm('Удалить пользователя?');" href="<%= baseUrl + "addapp?removeApp=1&id=" + id%>"></a>
                    </td>
                </tr>
                <%}%>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
