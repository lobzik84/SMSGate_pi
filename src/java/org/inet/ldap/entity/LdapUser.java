package org.inet.ldap.entity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.lobzik.tools.Tools;
import org.inet.ldap.com.LdapAttributes;
import org.inet.ldap.com.LdapTools;

/**
 *
 * @author Dmitry G.
 */
public class LdapUser {

    private Attributes attrs;

    private String GUID;
    private String distinguishedName;
    private String userPrincipal;
    private String accauntName;
    private String commonName;
    private String password;

    private String surname;
    private String firstname;
    private String middlename;

    private String mail;
    private String phone;
    private List<String> groupsDistinguishedNames;

    public LdapUser(Attributes attr) throws javax.naming.NamingException {
        attrs = attr;
        try {
            GUID = LdapTools.getGUID(attr);

            distinguishedName = Tools.getStringValue(attr.get(LdapAttributes.distinguishedName).get(), "");
            userPrincipal = Tools.getStringValue(attr.get(LdapAttributes.userPrincipalName).get(), "");
            accauntName = Tools.getStringValue(attr.get(LdapAttributes.sAMAccountName).get(), "");
            commonName = Tools.getStringValue(attr.get(LdapAttributes.cn).get(), "");
            surname = Tools.getStringValue(attr.get(LdapAttributes.sn).get(), "");
            firstname = Tools.getStringValue(attr.get(LdapAttributes.givenName).get(), "");

            middlename = commonName.replace(surname, "").replace(firstname, "").trim();
            
            Object p = attr.get(LdapAttributes.mail);
            if (p != null) {
                mail = Tools.getStringValue(attr.get(LdapAttributes.mail).get(), "");
            } else {
                mail = "";
            }
            p = attr.get(LdapAttributes.phone);
            if (p != null) {
                phone = Tools.getStringValue(attr.get(LdapAttributes.phone).get(), "");
            } else {
                phone = "";
            }
            groupsDistinguishedNames = getGroups(attr);

        } catch (Exception e) {
        }
    }

    private List<String> getGroups(Attributes attr) {
        List<String> list = new ArrayList();
        try {
            int size = attr.get(LdapAttributes.memberOf).size();
            for (int j = 0; j < size; j++) {
                list.add(Tools.getStringValue(attr.get(LdapAttributes.memberOf).get(j), ""));
            }
        } catch (Exception e) {
        }
        return list;
    }

    private boolean savePhoto(String path) throws NamingException {
        byte[] photo = (byte[]) attrs.get(LdapAttributes.photo).get();
        if (!path.endsWith("\\")) {
            path += "\\";
        }
        String dist = path + commonName + ".jpg";
        try {
            if (!path.endsWith("\\")) {
                path += "\\";
            }
            FileOutputStream os = new FileOutputStream(dist);
            os.write(photo);
            os.flush();
            os.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new File(dist).isFile();
    }

    public String getGUID() {
        return LdapTools.toString(GUID);
    }

    public String getDistinguishedName() {
        return LdapTools.toString(distinguishedName);
    }

    public String getUserPrincipal() {
        return LdapTools.toString(userPrincipal);
    }

    public String getAccauntName() {
        return LdapTools.toString(accauntName);
    }

    public String getCommonName() {
        return LdapTools.toString(commonName);
    }

    public String getMail() {
        return LdapTools.toString(mail);
    }

    public String getPhone() {
        return LdapTools.toString(phone);
    }

    public String getSurname() {
        return Tools.getStringValue(surname, "");
    }

    public String getFirstname() {
        return Tools.getStringValue(firstname, "");
    }
    
    public String getMiddlename() {
        return Tools.getStringValue(middlename, "");
    }

    public List<String> getGroupsDistinguishedNames() {
        return (groupsDistinguishedNames == null) ? (new ArrayList()) : groupsDistinguishedNames;
    }

    /**
     * Used to change the user password. Throws an IOException if the Domain
     * Controller is not LdapS enabled.
     *
     * @param trustAllCerts If true, bypasses all certificate and host name
     * validation. If false, ensure that the LdapS certificate has been imported
     * into a trust store and sourced before calling this method. Example:
     * String keystore = "/usr/java/jdk1.5.0_01/jre/lib/security/cacerts";
     * System.setProperty("javax.net.ssl.trustStore",keystore);
     */
    public void changePassword(String oldPass, String newPass, boolean trustAllCerts, LdapContext context)
            throws java.io.IOException, NamingException {
        String dn = getDistinguishedName();

        //Switch to SSL/TLS
        StartTlsResponse tls = null;
        try {
            tls = (StartTlsResponse) context.extendedOperation(new StartTlsRequest());
        } catch (Exception e) {
            //"Problem creating object: javax.naming.ServiceUnavailableException: [Ldap: error code 52 - 00000000: LdapErr: DSID-0C090E09, comment: Error initializing SSL/TLS, data 0, v1db0"
            throw new java.io.IOException("Failed to establish SSL connection to the Domain Controller. Is LdapS enabled?");
        }

        //Exchange certificates
        if (trustAllCerts) {
            tls.setHostnameVerifier(DO_NOT_VERIFY);
            SSLSocketFactory sf = null;
            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, TRUST_ALL_CERTS, null);
                sf = sc.getSocketFactory();
            } catch (java.security.NoSuchAlgorithmException e) {
            } catch (java.security.KeyManagementException e) {
            }
            tls.negotiate(sf);
        } else {
            tls.negotiate();
        }

        //Change password
        try {
            //ModificationItem[] modificationItems = new ModificationItem[1];
            //modificationItems[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodePwd", getPassword(newPass)));

            ModificationItem[] modificationItems = new ModificationItem[2];
            modificationItems[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("unicodePwd", getPassword(oldPass)));
            modificationItems[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("unicodePwd", getPassword(newPass)));
            context.modifyAttributes(dn, modificationItems);
        } catch (javax.naming.directory.InvalidAttributeValueException e) {
            String error = e.getMessage().trim();
            if (error.startsWith("[") && error.endsWith("]")) {
                error = error.substring(1, error.length() - 1);
            }
            System.err.println(error);
            //e.printStackTrace();
            tls.close();
            throw new NamingException(
                    "New password does not meet Active Directory requirements. "
                    + "Please ensure that the new password meets password complexity, "
                    + "length, minimum password age, and password history requirements."
            );
        } catch (NamingException e) {
            tls.close();
            throw e;
        }

        //Close the TLS/SSL session
        tls.close();
    }

    private static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    private static TrustManager[] TRUST_ALL_CERTS = new TrustManager[]{
        new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }
        }
    };

    private byte[] getPassword(String newPass) {
        String quotedPassword = "\"" + newPass + "\"";
        //return quotedPassword.getBytes("UTF-16LE");
        char unicodePwd[] = quotedPassword.toCharArray();
        byte pwdArray[] = new byte[unicodePwd.length * 2];
        for (int i = 0; i < unicodePwd.length; i++) {
            pwdArray[i * 2 + 1] = (byte) (unicodePwd[i] >>> 8);
            pwdArray[i * 2 + 0] = (byte) (unicodePwd[i] & 0xff);
        }
        return pwdArray;
    }
}
