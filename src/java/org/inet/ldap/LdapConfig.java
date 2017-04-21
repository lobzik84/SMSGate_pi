package org.inet.ldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.inet.ldap.entity.LdapReader;

/**
 *
 * @author Dmitry G
 */
public class LdapConfig {

	public static final String PASS_DOMAIN_DEFAULT = "molnet#pass";
	
    private static List<LdapReader> readerList = new ArrayList();

    public static void setReaders(LdapReader... readers) {
        if (readers.length == 0) {
            return;
        }
        readerList = Arrays.asList(readers);
    }
	
	public static LdapReader getReader() {
		return getReader(null);
	}
	
    public static LdapReader getReader(String domainName) {
        if (readerList.isEmpty()) {
            return null;
        }
        if (readerList.size() == 1 || domainName == null || domainName.trim().isEmpty()) {
            return readerList.get(0);
        } 
        domainName = domainName.replace("\\", "").toLowerCase();
        domainName += (domainName.contains(".local")?"":".local");
        for (LdapReader reader : readerList) {
            if (reader.getDomainName().equalsIgnoreCase(domainName)) {
                return reader;
            }
        }
        return null;
    }

    public static List<LdapReader> getReaders() {
        return new ArrayList(readerList);
    }
    
}
