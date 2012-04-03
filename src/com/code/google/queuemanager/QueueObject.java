package com.code.google.queuemanager;

/**
 *
 * @author Mateus Freira dos Santos
 */
public interface QueueObject {
    public void run();
    public int getStatus();
    public void setStatus(int i);
    public void whenPoisoned(Exception e);
    public void preRun();
    public void posRun();
}
