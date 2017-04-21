package org.inet.ldap.entity;

import org.inet.ldap.com.LdapDomainException;

/**
 *
 * @author Dmitry G
 */
public class LdapReader {

    final String username;
    final String password;
    final String domainName;
    final String serverName;
    final String serverIp;
    final int serverPort;

    private LdapReader(Builder builder) {
        this.username = builder.username;
        this.password = builder.password;
        this.domainName = builder.domainName;
        this.serverIp = builder.serverIp;
        this.serverPort = builder.serverPort;
        this.serverName = builder.serverName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getServerIpPort() {
        return serverIp + ":" + serverPort;
    }

    public static class Builder {

        final String username;
        final String password;
        String domainName = null;
        String serverName = null;
        String serverIp = null;
        int serverPort = 389;

        public Builder(String readerUsername, String readerPassword, String domainName) throws LdapDomainException {
            if (domainName == null || domainName.trim().isEmpty()) {
                throw new LdapDomainException();
            }
            this.username = readerUsername;
            this.password = readerPassword;
            this.domainName = domainName;
            this.domainName = this.domainName.replace("\\", "").toLowerCase();
            this.domainName += (this.domainName.contains(".local") ? "" : ".local");
        }

        public Builder setServerName(String serverName) {
            this.serverName = serverName;
            return this;
        }

        public Builder setServerIp(String serverIp) {
            this.serverIp = serverIp;
            return this;
        }

        public Builder setServerPort(int serverPort) {
            this.serverPort = serverPort;
            return this;
        }

        public LdapReader build() {
            return new LdapReader(this);
        }

    }

}
