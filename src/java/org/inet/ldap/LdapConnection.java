package org.inet.ldap;

import org.inet.ldap.com.LdapAttributes;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import org.inet.ldap.entity.LdapReader;

/**
 *
 * @author Dmitry G.
 */
public class LdapConnection {

    public static final String LDAP_CONNECTION_MESSAGE_ERROR = "LDAP_CONNECTION_MESSAGE_ERROR";

    private String readerDomain = "";    
    private LdapContext connectionContext = null;

    public boolean connect(LdapReader reader, HashMap jspData) throws NamingException {
        try {
            disconnect();
            connectionContext = new InitialLdapContext(getEnvironment(reader.getUsername(), reader.getPassword(), reader.getDomainName(), reader.getServerName(), reader.getServerIp(), reader.getServerIpPort()), null);
            if (connectionContext != null) {
                readerDomain = reader.getDomainName();
                return true;
            }
        } catch (javax.naming.CommunicationException e) {
            if (jspData != null) {
                jspData.put("LDAP_CONNECTION_MESSAGE_ERROR", "Failed to connect to " + reader.getDomainName() + " through " + ((reader.getServerIp() == null) ? reader.getServerName() : reader.getServerIpPort()));
            }
            System.out.println("ERROR to connect -> InitialLdapContext (" + reader.getUsername() + ", " + reader.getPassword() + ", " + reader.getDomainName() + ", " + reader.getServerName() + ")");
            e.printStackTrace();
        } catch (NamingException e) {
            if (jspData != null) {
                jspData.put("LDAP_CONNECTION_MESSAGE_ERROR", "Failed to authenticate <readerUsername>" + "@" + reader.getDomainName() + " through " + ((reader.getServerIp() == null) ? reader.getServerName() : reader.getServerIpPort()));
            }
            e.printStackTrace();
        }
        return false;
    }

    public boolean connect(Hashtable environment, HashMap jspData) throws NamingException {
        try {
            disconnect();
            connectionContext = new InitialLdapContext(environment, null);
            if (connectionContext != null) {
                return true;
            }
        } catch (javax.naming.CommunicationException e) {
            if (jspData != null) {
                jspData.put("LDAP_CONNECTION_MESSAGE_ERROR", "Failed to connect to LDAP domain");
            }
        } catch (NamingException e) {
            if (jspData != null) {
                jspData.put("LDAP_CONNECTION_MESSAGE_ERROR", "Failed to authenticate <readerUsername>");
            }
        }
        return false;
    }

    public void disconnect() {
        if (connectionContext != null) {
            try {
                connectionContext.close();
            } catch (Exception e) {
            } finally {
                connectionContext = null;
            }
        }
    }

    public static boolean checkAuthorization(LdapReader reader) {
        try {
            LdapContext cntx = new InitialLdapContext(getEnvironment(reader.getUsername(), reader.getPassword(), reader.getDomainName(), reader.getServerName(), reader.getServerIp(), reader.getServerIpPort()), null);
            cntx.close();
            return true;
        } catch (javax.naming.CommunicationException e) {
            System.out.println("Failed to connect to " + reader.getDomainName() + " through " + ((reader.getServerIp() == null) ? reader.getServerName() : reader.getServerIpPort()));
        } catch (NamingException e) {
            System.out.println("Failed to authenticate " + reader.getUsername() + "@" + reader.getDomainName());
        }
        return false;
    }

    private static Hashtable getEnvironment(String username, String password, String domainName, String serverName, String serverIP, String serverIpPort) {
        if (domainName == null) {
            try {
                String chn = InetAddress.getLocalHost().getCanonicalHostName();
                if (chn.split("\\.").length > 1) {
                    domainName = chn.substring(chn.indexOf(".") + 1);
                }
            } catch (UnknownHostException e) {
            }
        }
        String principalName = username + "@" + domainName;
        String ldapUrl = "ldap://";
        if (serverIP == null || serverIP.trim().isEmpty()) {
            ldapUrl += (serverName == null || serverName.trim().isEmpty()) ? domainName : (serverName + "." + domainName);
        } else {
            ldapUrl += serverIpPort;
        }
        ldapUrl += "/";
        Hashtable props = new Hashtable();
        props.put(Context.SECURITY_PRINCIPAL, principalName);
        props.put(Context.SECURITY_CREDENTIALS, password.trim());
        props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        props.put(Context.PROVIDER_URL, ldapUrl);
        props.put("com.sun.jndi.ldap.connect.timeout", "5000");
        props.put("com.sun.jndi.ldap.connect.pool.timeout", "300000");
        props.put("com.sun.jndi.ldap.connect.pool.maxsize", "125");
        props.put("java.naming.ldap.attributes.binary", LdapAttributes.objectGUID);
        return props;
    }

    public LdapContext getConnectionContext() {
        return connectionContext;
    }

    public String getReaderDomain() {
        return readerDomain;
    }

}
