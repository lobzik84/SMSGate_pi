package org.inet.ldap.com;

import org.inet.ldap.entity.LdapUser;
import org.inet.ldap.entity.LdapGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;
import javax.naming.directory.SearchResult;
import org.inet.ldap.LdapConnection;

/**
 *
 * @author Dmitry G.
 */
public class LdapTools {

    public static final String LDAP_SEARCH_MESSAGE = "LDAP_SEARCH_MESSAGE";

    public static LdapUser getUser(String username, HashMap jspData, LdapConnection conn) throws NamingException {
        if (conn.getConnectionContext() != null) {
            String domainName = null;
            String authenticatedUser = (String) conn.getConnectionContext().getEnvironment().get(Context.SECURITY_PRINCIPAL);
            if (authenticatedUser.contains("@")) {
                domainName = authenticatedUser.substring(authenticatedUser.indexOf("@") + 1);
            }
            String searchString;
            if (domainName != null) {
                searchString = "(" + LdapAttributes.userPrincipalName + "=" + username + "@" + domainName + ")";
            } else {
                searchString = "(" + LdapAttributes.sAMAccountName + "=" + username + ")";
            }
            NamingEnumeration<SearchResult> answer = answer(searchString, jspData, conn);
            if (answer.hasMore()) {
                Attributes attr = answer.next().getAttributes();
                Attribute user = attr.get("userPrincipalName");
                if (user != null) {
                    return new LdapUser(attr);
                }
            }
        }
        return null;
    }

    public static List<LdapUser> getAllDomainUsers(HashMap jspData, LdapConnection conn) throws NamingException {
        return getUA("(objectClass=user)", -1, jspData, conn);
    }

    public static List<LdapUser> getGroupMembers(String groupDistinguishedName, HashMap jspData, LdapConnection conn) throws NamingException {
        return getUA("(& (memberOf=" + groupDistinguishedName + ")(objectClass=user))", -1, jspData, conn);
    }

    public static List<LdapUser> getUsersByBaseAccauntData(String cnString, int limit, HashMap jspData, LdapConnection conn) throws NamingException {
        return getUA("(& (|(CN=*" + cnString + "*)(mail=*" + cnString + "*)(sAMAccountName=*" + cnString + "*))(objectClass=user))", limit, jspData, conn);
    }
    
    public static List<LdapUser> getUsersLikeCN(String cnString, int limit, HashMap jspData, LdapConnection conn) throws NamingException {
        return getUA("(& (CN=*" + cnString + "*)(objectClass=user))", limit, jspData, conn);
    }
    
    public static List<LdapUser> getUsersLikeLogin(String cnString, int limit, HashMap jspData, LdapConnection conn) throws NamingException {
        return getUA("(& (sAMAccountName=*" + cnString + "*)(objectClass=user))", limit, jspData, conn);
    }
    
    public static List<LdapUser> getUsersLikeEmail(String cnString, int limit, HashMap jspData, LdapConnection conn) throws NamingException {
        return getUA("(& (mail=*" + cnString + "*)(objectClass=user))", limit, jspData, conn);
    }

    public static List<LdapUser> getUsers(String search, int limit, HashMap jspData, LdapConnection conn) throws NamingException {
        return getUA(search, limit, jspData, conn);
    }

    public static List<LdapGroup> getAllDomainGroups(HashMap jspData, LdapConnection conn) throws NamingException {
        return getGA("(objectclass=group)", -1, jspData, conn);
    }

    public static List<LdapGroup> getGroupsLikeCN(String cn, int limit, HashMap jspData, LdapConnection conn) throws NamingException {
        return getGA("(& (cn=*" + cn + "*)(objectClass=group))", limit, jspData, conn);
    }

    public static List<LdapGroup> getGroups(String search, int limit, HashMap jspData, LdapConnection conn) throws NamingException {
        return getGA(search, limit, jspData, conn);
    }

    public static Attributes getObjectByGUID(String guid, LdapConnection conn) throws NamingException {
        Attributes attr = null;
        if (conn.getConnectionContext() != null) {
            String searchGUID = "<GUID=" + guid + ">";
            try {
                attr = conn.getConnectionContext().getAttributes(searchGUID);
            } catch (Exception e) {
            }
        }
        return attr;
    }

    public static Class getObjectClass(Attributes attr) {
        try {
            String category = (String) attr.get(LdapAttributes.objectCategory).get();
            if (category.contains("CN=Person")) {
                return LdapUser.class;
            }
            if (category.contains("CN=Group")) {
                return LdapGroup.class;
            }
        } catch (Exception e) {
        }
        return null;
    }

    private static String toDC(String domainName) {
        StringBuilder buf = new StringBuilder();
        for (String token : domainName.split("\\.")) {
            if (token.length() == 0) {
                continue;
            }
            if (buf.length() > 0) {
                buf.append(",");
            }
            buf.append("DC=").append(token);
        }
        return buf.toString();
    }

    public static String toString(String val) {
        if (val == null) {
            return "";
        } else {
            return val;
        }
    }

    public static String getCN(String str) {
        String r = "";
        try {
            if (str.contains("CN=")) {
                String[] s = str.split(",");
                for (int i = 0; i < s.length; i++) {
                    if (s[i].contains("CN=")) {
                        r = s[i].replace("CN=", "");
                        break;
                    }
                }
            }
        } catch (Exception e) {
        }
        return r;
    }

    private static List<LdapGroup> getGA(String search, int limit, HashMap jspData, LdapConnection conn) throws NamingException {
        List<LdapGroup> groups = new ArrayList<LdapGroup>();
        NamingEnumeration<SearchResult> en = answer(search, jspData, conn);
        if (en != null) {
            try {
                int j = 0;
                while (en.hasMore()) {
                    Attributes attr = en.next().getAttributes();
                    Attribute user = attr.get(LdapAttributes.cn);
                    if (user != null) {
                        j++;
                        if (limit != -1 && j > limit) {
                            if (jspData != null) {
                                jspData.put(LDAP_SEARCH_MESSAGE,
                                        "По заданным параметром найдено слишком много групп<br>"
                                        + "Скорректируйте поисковой запрос"
                                );
                            }
                            break;
                        }
                        groups.add(new LdapGroup(attr, conn));
                    }
                }
            } catch (Exception e) {
            }
        }
        return groups;
    }

    private static List<LdapUser> getUA(String search, int limit, HashMap jspData, LdapConnection conn) throws NamingException {
        List<LdapUser> users = new ArrayList<LdapUser>();
        NamingEnumeration<SearchResult> en = answer(search, jspData, conn);
        if (en != null) {
            try {
                int j = 0;
                while (en.hasMore()) {
                    Attributes attr = en.next().getAttributes();
                    Attribute user = attr.get(LdapAttributes.userPrincipalName);
                    if (user != null) {
                        j++;
                        if (limit != -1 && j > limit) {
                            if (jspData != null) {
                                jspData.put(LDAP_SEARCH_MESSAGE,
                                        "По заданным параметром найдено слишком много пользователей<br>"
                                        + "Скорректируйте поисковой запрос"
                                );
                            }
                            break;
                        }
                        users.add(new LdapUser(attr));
                    }
                }
            } catch (Exception e) {
            }
        }
        return users;
    }

    private static NamingEnumeration<SearchResult> answer(String search, HashMap jspData, LdapConnection conn) throws NamingException {
        if (conn.getConnectionContext() != null) {
            String domainName = conn.getReaderDomain();
            if (domainName == null) {
                String authenticatedUser = (String) conn.getConnectionContext().getEnvironment().get(Context.SECURITY_PRINCIPAL);
                domainName = authenticatedUser.substring(authenticatedUser.indexOf("@") + 1);
            }
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SUBTREE_SCOPE);
            controls.setReturningAttributes(LdapAttributes.attributes);
            controls.setTimeLimit(10000);
            NamingEnumeration<SearchResult> answer = null;
            try {
                answer = conn.getConnectionContext().search(toDC(domainName), search, controls);
            } catch (Exception e) {
                if (conn.connect(conn.getConnectionContext().getEnvironment(), jspData)) {
                    answer = conn.getConnectionContext().search(toDC(domainName), search, controls);
                }
            }
            return answer;
        }
        return null;
    }
	
	public static String getDomainFromUsername(String username) {
		String result = "";
		String delimeter = "\\";
		if (username.contains("/")) {
			delimeter = "/";
		}
		if (username.contains(delimeter)) {
			result = username.substring(0, username.indexOf(delimeter));
		} else if (username.contains("@")) {
			result = username.substring(username.indexOf("@") + 1, username.length());
			if (result.contains(".")) {
				result = result.substring(0, username.indexOf("."));
			}
		}
		return result.toLowerCase();		
	}

	public static String[] splitUsername(String username) {
		String d = "", u = username;
		String delimeter = "\\";
		if (username.contains("/")) {
			delimeter = "/";
		}
		if (username.contains(delimeter)) {
			d = username.substring(0, username.indexOf(delimeter));
			u = username.substring(username.indexOf(delimeter) + 1, username.length());
		} else if (username.contains("@")) {
			d = username.substring(username.indexOf("@") + 1, username.length());
			if (d.contains(".")) {
				d = d.substring(0, d.indexOf("."));
			}
			u = username.substring(0, username.indexOf("@"));
		}
		return new String[] {u,d};
	}
	
    public static String getGUID(Attributes attrs) throws NamingException {
        String strGUID = "";
        if (attrs != null) {
            try {
                byte[] GUID = (byte[]) attrs.get(LdapAttributes.objectGUID).get();
                String byteGUID = "";
                for (int c = 0; c < GUID.length; c++) {
                    byteGUID = byteGUID + "\\" + addLeadingZero((int) GUID[c] & 0xFF);
                }
                strGUID = strGUID + addLeadingZero((int) GUID[3] & 0xFF);
                strGUID = strGUID + addLeadingZero((int) GUID[2] & 0xFF);
                strGUID = strGUID + addLeadingZero((int) GUID[1] & 0xFF);
                strGUID = strGUID + addLeadingZero((int) GUID[0] & 0xFF);
                strGUID = strGUID + "-";
                strGUID = strGUID + addLeadingZero((int) GUID[5] & 0xFF);
                strGUID = strGUID + addLeadingZero((int) GUID[4] & 0xFF);
                strGUID = strGUID + "-";
                strGUID = strGUID + addLeadingZero((int) GUID[7] & 0xFF);
                strGUID = strGUID + addLeadingZero((int) GUID[6] & 0xFF);
                strGUID = strGUID + "-";
                strGUID = strGUID + addLeadingZero((int) GUID[8] & 0xFF);
                strGUID = strGUID + addLeadingZero((int) GUID[9] & 0xFF);
                strGUID = strGUID + "-";
                strGUID = strGUID + addLeadingZero((int) GUID[10] & 0xFF);
                strGUID = strGUID + addLeadingZero((int) GUID[11] & 0xFF);
                strGUID = strGUID + addLeadingZero((int) GUID[12] & 0xFF);
                strGUID = strGUID + addLeadingZero((int) GUID[13] & 0xFF);
                strGUID = strGUID + addLeadingZero((int) GUID[14] & 0xFF);
                strGUID = strGUID + addLeadingZero((int) GUID[15] & 0xFF);
            } catch (NullPointerException e) {
                System.err.println("Problem listing attributes: " + e);
            }
        }
        return strGUID;
    }

    private static String addLeadingZero(int k) {
        return (k < 0xF) ? "0" + Integer.toHexString(k) : Integer.toHexString(k);
    }

}