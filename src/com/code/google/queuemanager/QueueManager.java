package com.code.google.queuemanager;

import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author Mateus Freira dos Santos
 */
public class QueueManager implements Runnable {

    public static final int RUNING = 1;
    public static final int WAIT = 2;
    public static final int PROCESSED = 3;
    public static final int POISONED = 4;
    public static final String NAME_QUEUE = "queue";
    private static volatile Hashtable instances = new Hashtable();
    private volatile Vector objects = new Vector();
    private volatile Vector objectsProcessing = new Vector();
    private Thread thread;
    private int waitTime = 100;
    private boolean canceled = false;
    private QueueObject activeObject = null;

    public QueueManager() {
        thread = new Thread(this);
    }

    public int size() {
        return QueueManager.instances.size();

    }

    public static QueueManager getInstance() {
        return getIstastance(NAME_QUEUE);
    }

    public static QueueManager getIstastance(String name) {
        QueueManager queue = (QueueManager) instances.get(name);
        if (queue == null) {
            queue = new QueueManager();
            instances.put(name, queue);
        }
        return queue;
    }

    public void run() {
        do {
            synchronized (objectsProcessing) {
                synchronized (objects) {
                    objectsProcessing.removeAllElements();
                    for (int i = 0; i < objects.size(); i++) {
                        Object o = objects.elementAt(i);
                        objectsProcessing.addElement(o);
                        objects.removeElementAt(i);
                    }
                }
                for (int i = 0; i < objectsProcessing.size(); i++) {
                    QueueObject o = (QueueObject) objectsProcessing.elementAt(i);
                    this.activeObject = o;
                    o.preRun();
                    o.setStatus(RUNING);
                    try{
                        o.run();
                        o.posRun();
                        o.setStatus(PROCESSED);                        
                    }catch(Exception e){
                        o.setStatus(POISONED);
                        o.whenPoisoned(e);
                    }
                    objectsProcessing.removeElementAt(i);
                }
                this.activeObject = null;
            }
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } while (!isCanceled());
        this.canceled = false;
        thread = null;
    }

    public void start() {
        if (thread != null) {
            thread = new Thread(this);
        }
        thread.start();
    }

    /**
     * @return the canceled
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * @param canceled the canceled to set
     */
    public void Cancel() {
        this.canceled = true;
    }

    public void addObject(QueueObject obj) {
        obj.setStatus(WAIT);
        this.objects.addElement(obj);
    }

    public QueueObject getActiveObject() {
        return this.activeObject;
    }

    public boolean isActiveObject() {
        return this.activeObject == null;
    }

    public void setWaitTime(int waitTime){
        this.waitTime = waitTime;
    }
}
