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

        <p class="title">Информационные системы - клиенты службы коротких сообщений</p>

        <table class="table mt-20">
            <thead>
                <tr>
                    <td class="bold ta-c w-5">
                        №
                    </td>
                    <td class="w-30">
                        Наименование
                    </td>
                    <td class="w-45">
                        Ключ доступа<span class="hint_i js-tooltip" title="Используется ключ RSA-1024, public exponent = 10001 (HEX) PublicKey(modulus in HEX, 256 symbols)">i</span>
                    </td>
                    <td class="w-20 ta-c">
                        Управление
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
                    <td class="ta-c"><%= id%></td>
                    <td><%= name%></td>
                    <td>
                        <p class="publicKey_copy" title="Скопировать" data-key="">
                            <%= shortPublicKey.length() > 0 ? shortPublicKey : fullPublicKey%>
                        </p>
                        <p class="publicKey_full wb-all none">
                            <%= fullPublicKey%>
                        </p>
                    </td>
                    <td class="ta-c">
                        <a class="btn_delete" onclick="return confirm('Удалить пользователя?');" href="<%= baseUrl + "addapp?removeApp=1&id=" + id%>"></a>
                    </td>
                </tr>
                <%}%>
                <tr>
                    <td></td>
            <form class="form_hide none" action="<%= baseUrl + "addapp"%>" method="post">
                <td> 
                    <div class="btn_add_mech none">
                        <input class="auth_rtrn_checkValue" type="text" name="name" />
                    </div>
                </td>
                <td>
                    <div class="btn_add_mech none w-100">
                        <p class="icon icon-genKey js-tooltip mt-5 mr-10 va-t" title="Сгенерировать ключ доступа" onclick="generateKey()"></p>
                        <div class="inline-b w-90">   
                            <label class="label__sub">Generated PublicKey</label>
                            <textarea class="textarea__key" name="public_key" id="public_key"></textarea>
                            <div class="private_key mt-20">
                                <label class="label__sub">Generated PrivateKey (multiplicative inverse in HEX, 256 symbols)</label>
                                <textarea class="textarea__key mt-5" name="private_key" id="private_key"></textarea>
                            </div>
                        </div>
                    </div>
                </td>
                <td class="ta-c">
                    <div class="btn_add_mech va-t mr-5 none">
                        <input hidden type="text" name="ADD_ME" value="1"/>
                        <input class="btn_add_mech va-t mr-5 none icon icon-save va-m js-tooltip" title="Сохранить" type="submit" value="" name="submit" />
                    </div>
                    <div class="btn_add icon va-m icon-add"></div>
            </form>
            </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
