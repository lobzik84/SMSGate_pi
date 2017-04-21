package org.inet.ldap.com;

/**
 *
 * @author Dmitry G.
 */
public class LdapAttributes {

	public static final String objectGUID     = "objectGUID";
	public static final String objectCategory = "objectCategory";
	public static final String distinguishedName = "distinguishedName";
	public static final String cn = "cn";
	public static final String name = "name";
	public static final String uid = "uid";
	public static final String sn = "sn";
	public static final String givenName = "givenname";
	public static final String sAMAccountName = "samaccountname";
	public static final String userPrincipalName = "userPrincipalName";
	public static final String mail = "mail";
	public static final String phone = "telephonenumber";
	public static final String photo = "thumbnailPhoto";
	public static final String memberOf = "memberof";
	public static final String member = "member";
	
	public static final String[] attributes = {
			distinguishedName,	
			cn,					
			name,					
			uid,					
			sn,					
			givenName,			
			memberOf,				
			sAMAccountName,		
			userPrincipalName,	
			mail,					
			phone,		
			photo,      
			member,				
			objectGUID,				
			objectCategory  		
		};
	
	
}
