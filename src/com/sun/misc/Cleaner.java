package com.sun.misc;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class Cleaner extends PhantomReference<Object>
{
    private static final ReferenceQueue<Object> dummyQueue;
    private static Cleaner first;
    private Cleaner next;
    private Cleaner prev;
    private final Runnable thunk;
    
    static {
        dummyQueue = new ReferenceQueue<Object>();
        Cleaner.first = null;
    }
    
    private static synchronized Cleaner add(final Cleaner cl) {
        if (Cleaner.first != null) {
            cl.next = Cleaner.first;
            Cleaner.first.prev = cl;
        }
        return Cleaner.first = cl;
    }
    
    private static synchronized boolean remove(final Cleaner cl) {
        if (cl.next == cl) {
            return false;
        }
        if (Cleaner.first == cl) {
            if (cl.next != null) {
                Cleaner.first = cl.next;
            }
            else {
                Cleaner.first = cl.prev;
            }
        }
        if (cl.next != null) {
            cl.next.prev = cl.prev;
        }
        if (cl.prev != null) {
            cl.prev.next = cl.next;
        }
        cl.next = cl;
        cl.prev = cl;
        return true;
    }
    
    private Cleaner(final Object referent, final Runnable thunk) {
        super(referent, Cleaner.dummyQueue);
        this.next = null;
        this.prev = null;
        this.thunk = thunk;
    }
    
    public static Cleaner create(final Object ob, final Runnable thunk) {
        if (thunk == null) {
            return null;
        }
        return add(new Cleaner(ob, thunk));
    }
    
    public void clean() {
        if (!remove(this)) {
            return;
        }
        try {
            this.thunk.run();
        }
        catch (Throwable x) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    if (System.err != null) {
                        new Error("Cleaner terminated abnormally", x).printStackTrace();
                    }
                    System.exit(1);
                    return null;
                }
            });
        }
    }
}
