/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi.event;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;
import org.lobzik.smspi.pi.modules.Module;

/**
 *
 * @author lobzik
 */
public class EventManager extends Thread {

    private static final Map<Event.Type, List> subscribers = new ConcurrentHashMap();
    private static final Queue<Event> eventList = new ConcurrentLinkedQueue();
    private static boolean run = true;
    private static Logger log = null;
    private static EventManager instance = null;
    private Object sync = null;

    private EventManager() {

    }

//TODO subscription by event name?
    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager(); //lazy init
            log = Logger.getLogger(instance.getClass().getSimpleName());
        }
        return instance;
    }

    @Override
    public void run() {
        setName(this.getClass().getSimpleName() + "-Thread");
        while (run) {
            try {
                while (eventList.size() > 0) {
                    Event e = eventList.poll();
                    if (subscribers.get(e.type) != null) {
                        List<Module> subscribersList = subscribers.get(e.type);
                        //System.out.println("Event type " + e.type + ", notifying " + subscribersList.size() + " subscribers");
                        for (Module m : subscribersList) {
                            if (e.recipient == null || e.recipient.equals(m.getModuleName())) {
                                try {
                                    m.handleEvent(e);
                                } catch (Exception ex) {
                                   log.error("Error in " + m.getModuleName() + ": " + ex.getMessage());
                                }
                            }
                            //System.out.println("Notifying " + m.getModuleName());

                        }
                    }
                }
                if (sync != null) {
                    synchronized (sync) {
                        sync.notify();
                    }
                }
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException ie) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void lockForEvent(Event event, Object sync) {
        this.sync = sync;
        newEvent(event);

        try {
            synchronized (sync) {
                sync.wait();
            }
        } catch (InterruptedException ie) {
        }
        this.sync = null;
    }

    public void newEvent(Event event) {
        //System.out.println("New event! " + event.data);
        if (event == null) {
            return;
        }
        eventList.add(event);
        synchronized (this) {
            notify();
        }
    }

    public void finish() {
        run = false;
        synchronized (this) {
            notify();
        }
    }

    public static void subscribeForEventType(Module module, Event.Type type) {

        if (subscribers.get(type) == null) {
            List<Module> modulesList = new CopyOnWriteArrayList();
            subscribers.put(type, modulesList);
        }
        if (!subscribers.get(type).contains(module)) {
            subscribers.get(type).add(module); //subscribe only once
        }
    }

}
