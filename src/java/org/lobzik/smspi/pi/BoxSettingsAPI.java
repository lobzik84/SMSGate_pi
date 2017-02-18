/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author lobzik
 */
public class BoxSettingsAPI {

    private static final Map<String, String> settings = new HashMap();
    private static final Map<Integer, String> ids = new HashMap();

    public static void initBoxSettings() {
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            List<HashMap> resList = DBSelect.getRows("select * from settings;", conn);
            for (HashMap entry : resList) {
                String name = (String) entry.get("name");
                String value = (String) entry.get("value");
                int id = Tools.parseInt(entry.get("id"), 0);
                settings.put(name, value);
                ids.put(id, name);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveSettings() {
        for (Integer id : ids.keySet()) {
            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                HashMap dbMap = new HashMap();
                String name = ids.get(id);
                dbMap.put("id", id);
                dbMap.put("name", name);
                dbMap.put("value", settings.get(name));
                DBTools.updateRow("settings", dbMap, conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String get(String name) {
        return settings.get(name);
    }

    public static int getInt(String name) {
        return Tools.parseInt(settings.get(name), 0);
    }

    public static double getDouble(String name) {
        return Tools.parseDouble(settings.get(name), 0);
    }

    public static boolean isSet(String name) {
        if (settings.get(name) == null) {
            return false;
        }
        return "true".equals(settings.get(name).trim().toLowerCase());
    }

    public static void set(String settingName, String settingNewValue) {
        settings.put(settingName, settingNewValue);
        saveSettings();

    }

    public static void set(Map newSettings) {
        for (Object key : newSettings.keySet()) {
            if (!(key instanceof String)) {
                continue;
            }
            Object val = newSettings.get(key);
            if (val instanceof String) {
                String valStr = (String) val;
                if (settings.containsKey((String) key)) {
                    settings.put((String) key, valStr);
                }
            } else if (val instanceof String[]) {
                String valStr = ((String[])val)[0];
                if (settings.containsKey((String) key)) {
                    settings.put((String) key, valStr);
                }
            }
        }
        //settings.putAll(newSettings);
        saveSettings();

    }

    public static Map<String, String> getSettingsMap() {
        HashMap<String, String> clone = new HashMap();
        clone.putAll(settings);
        return clone;
    }

}
