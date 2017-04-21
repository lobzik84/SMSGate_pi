/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi;

import java.io.File;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.inet.ldap.LdapConfig;
import org.inet.ldap.com.LdapDomainException;
import org.inet.ldap.entity.LdapReader;
import org.lobzik.smspi.pi.modules.ActualDataStorageModule;
import org.lobzik.smspi.pi.modules.ChartModule;
import org.lobzik.smspi.pi.modules.DisplayModule;
import org.lobzik.smspi.pi.modules.InternalSensorsModule;
import org.lobzik.smspi.pi.modules.DBDataWriterModule;

import org.lobzik.smspi.pi.modules.ModemModule;
import org.lobzik.smspi.pi.modules.SystemModule;
import org.lobzik.smspi.pi.modules.TimerModule;

/**
 * Web application lifecycle listener.
 *
 * @author lobzik
 */
@WebListener()
public class AppListener implements ServletContextListener {

    private static final Logger log = Logger.getRootLogger();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AppData.init(sce);
        try {
            PatternLayout layout = new PatternLayout("%d{yyyy.MM.dd HH:mm:ss} %c{1} %-5p: %m%n");
            ConsoleAppender consoleAppender = new ConsoleAppender(layout);
            BasicConfigurator.configure(consoleAppender);
            log.info("Root Log init ok");
            log.info("Starting hs app. Modules start!");
            BoxSettingsAPI.initBoxSettings();
            initLdapSettings();
            AppData.setGraphicsWorkDir(new File(sce.getServletContext().getRealPath("img")));
            DisplayModule.getInstance().start();
            InternalSensorsModule.getInstance().start();
            ModemModule.getInstance().start();
            ActualDataStorageModule.getInstance().start();
            TimerModule.getInstance().start();
            DBDataWriterModule.getInstance().start();
            SystemModule.getInstance().start();

            ChartModule.getInstance().start();

        } catch (Throwable ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            log.info("Context Destroyed called. Stopping application modules!");

            TimerModule.finish();
            ModemModule.finish();
            InternalSensorsModule.finish();
            DisplayModule.finish();

            AppData.eventManager.finish();

            BasicConfigurator.resetConfiguration();

        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    private void initLdapSettings() {
        try {
            LdapReader ldapReader = new LdapReader.Builder(
                    BoxSettingsAPI.get("ldap.server.login"),
                    BoxSettingsAPI.get("ldap.server.password"),
                    BoxSettingsAPI.get("ldap.server.domain"))
                    .setServerIp(BoxSettingsAPI.get("ldap.server.ip"))
                    .build();
            LdapConfig.setReaders(ldapReader);
        } catch (LdapDomainException ex) {
            log.error("Error while ldap init");
            log.error(ex.getMessage());
        }
    }

}
