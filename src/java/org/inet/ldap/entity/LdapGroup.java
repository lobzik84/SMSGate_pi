package org.inet.ldap.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import org.lobzik.tools.Tools;
import org.inet.ldap.com.LdapAttributes;
import org.inet.ldap.LdapConnection;
import org.inet.ldap.com.LdapTools;

/**
 *
 * @author Dmitry G.
 */
public class LdapGroup {
		
	public static final String KEY_USERS_DN   = "KEY_USERS_DN";	
	public static final String KEY_USERS_LIST = "KEY_USERS_LIST";	
	
	private Attributes attrs;
	private String GUID;
	private String distinguishedName;
	private String commonName;
	private HashMap users = new HashMap();
		
	public LdapGroup(Attributes attr) throws NamingException {
		try {
			init(attr);
		} catch (Exception e) {
		}
    }
	
	public LdapGroup(Attributes attr, LdapConnection conn) {
		try {
			init(attr);
			users.put(KEY_USERS_LIST, LdapTools.getGroupMembers(distinguishedName, null, conn));
		} catch (Exception e) {
		}
	}
	
	private void init(Attributes attr) throws NamingException {
		attrs = attr;
		GUID = LdapTools.getGUID(attr);	
		distinguishedName = Tools.getStringValue(attr.get(LdapAttributes.distinguishedName).get(), "");
		commonName = Tools.getStringValue(attr.get(LdapAttributes.cn).get(), "");
		users.put(KEY_USERS_DN, getUsers(attr));
	}
	
	
	private List<String> getUsers(Attributes attr) {
			List<String> list = new ArrayList();
			try {
				int size = attr.get(LdapAttributes.member).size();
				for (int j=0; j<size; j++) {
					list.add(Tools.getStringValue(attr.get(LdapAttributes.member).get(j), ""));
				}
			} catch (Exception e) {}
			return list;
		}
	
	public String getGUID() {
		return LdapTools.toString(GUID);
	}
		
	public String getDistinguishedName(){
        return LdapTools.toString(distinguishedName);
    }
		
    public String getCommonName(){
        return LdapTools.toString(commonName);
    }
		
	public HashMap getUsers() {
		return users;
	}
	
}
