package org.lobzik.smspi.pi.modules;

import java.io.File;
import org.apache.log4j.Logger;
import org.lobzik.smspi.pi.AppData;
import org.lobzik.smspi.pi.event.Event;

/**
 *
 * @author lobzik
 */

import org.apache.log4j.Appender;
import org.lobzik.smspi.pi.BoxCommonData;
import org.lobzik.smspi.pi.ConnJDBCAppender;
import org.lobzik.smspi.pi.event.EventManager;
import org.lobzik.tools.db.mysql.DBTools;

public class ChartModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static ChartModule instance = null;

    private static final String IMG_FILE = "chart.png";


    private static Logger log = null;

    private ChartModule() { //singleton
    }

    public static ChartModule getInstance() {
        if (instance == null) {
            instance = new ChartModule(); //lazy init
            log = Logger.getLogger(instance.MODULE_NAME);
            Appender appender = ConnJDBCAppender.getAppenderInstance(AppData.dataSource, instance.MODULE_NAME);
            log.addAppender(appender);
        }
        return instance;
    }

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void start() {
        try {
            String imgFile = AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + IMG_FILE;
            draw();
            EventManager.subscribeForEventType(this, Event.Type.TIMER_EVENT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
        switch (e.type) {
            case TIMER_EVENT:
                if (e.name.equals("build_chart")) {
                    draw();
                }
                break;
        }

    }

    private void draw() {
       // try (Connection conn  = DBTools.openConnection(BoxCommonData.dataSourceName))
    }

    public static void finish() {
        
    }

}
