package org.inet.ldap.com;

/**
 *
 * @author Dmitry G
 */
public class LdapDomainException extends Exception {
    
    public LdapDomainException() {
        super("Не указано доменное имя");
    }
    
}