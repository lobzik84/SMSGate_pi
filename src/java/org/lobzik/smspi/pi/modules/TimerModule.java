/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi.modules;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.*;
import org.lobzik.smspi.pi.AppData;
import org.lobzik.smspi.pi.BoxCommonData;
import org.lobzik.smspi.pi.event.Event;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author lobzik
 */
public class TimerModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static TimerModule instance = null;
    private static final HashMap<Integer, Timer> timers = new HashMap();
    private final static String datePattern = "yyyy.MM.dd HH:mm:ss";

    private TimerModule() { //singleton
    }

    public static TimerModule getInstance() {
        if (instance == null) {
            instance = new TimerModule(); //lazy init
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
            Calendar c = new GregorianCalendar();
            if (c.get(Calendar.YEAR) < 2016) {
                //todo event?
                throw new Exception("Invalid date! Timers not started");

            }
            Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName);
            List<HashMap> timerDBList = getDBTimers(conn);
            conn.close();
            for (HashMap timerMap : timerDBList) {
                startTimer(timerMap);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
    }

    public static void finish() {
        for (int id : timers.keySet()) {
            Timer timer = timers.get(id);
            timer.cancel();
        }
    }

    private void startTimer(HashMap timerMap) throws Exception {
        Date startDate = (Date) timerMap.get("start_date");
        int period = Tools.parseInt(timerMap.get("period"), 0);
        int id = Tools.parseInt(timerMap.get("id"), 0);
        int enabled = Tools.parseInt(timerMap.get("enabled"), 0);
        if (enabled == 0) {
            return;
        }
        if (startDate == null && period == 0) {
            return;
        }
        if (startDate == null) {
            startDate = new Date();
        }

        Calendar cal = Calendar.getInstance();
        int yearNow = cal.get(Calendar.YEAR);
        cal.setTime(startDate);
        cal.set(Calendar.YEAR, yearNow);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        //cal.set(Calendar.DAY_OF_MONTH, 1);
        
        if (cal.getTimeInMillis() <= System.currentTimeMillis() && period > 0) {
            while (cal.getTimeInMillis() <= System.currentTimeMillis()) {
                cal.add(Calendar.SECOND, period);
            }
        }

        String name = (String) timerMap.get("name");
        Timer timer = new Timer(name + "-Timer", true);
        SignalTask signalTask = new SignalTask(name, null);
        if (period > 0) {
            timer.scheduleAtFixedRate(signalTask, cal.getTime(), period * 1000);
        } else {
            timer.schedule(signalTask, cal.getTime());
        }
        timers.put(id, timer);
    }

    private List<HashMap> getDBTimers(Connection conn) throws Exception {
        String sSQL = " select * from timer ";
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        List<HashMap> result = DBSelect.getRows(sSQL, conn);
        for (HashMap timer : result) {
            if (timer.get("start_date") != null) {
                timer.put("start_date_str", sdf.format(timer.get("start_date")));
            } else {
                timer.put("start_date_str", "Сразу");
            }
            int period = Tools.parseInt(timer.get("period"), -1);
            int periodUnits = 1;
            if (period % 60 == 0) {
                periodUnits = 60;
                period = period / 60;
                if (period % 60 == 0) {
                    periodUnits = 3600;
                    period = period / 60;

                }
            }
            timer.put("period_u", period);
            timer.put("period_units", periodUnits);
        }
        return result;
    }
}

class SignalTask extends TimerTask {

    private String name = null;
    private Map data = null;

    public SignalTask(String nameP, Map dataP) {
        super();
        name = nameP;
        data = dataP;
    }

    @Override
    public void run() {
        //System.out.println("Timer signal: " + name + "=" + value);

        Event e = new Event(name, data, Event.Type.TIMER_EVENT);

        AppData.eventManager.newEvent(e);
    }
}
