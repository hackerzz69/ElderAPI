package com.zenyte.sql;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueryExecutor {
    
    private static final Queue<SQLRunnable> QUEUE = new ConcurrentLinkedQueue<>();
    
    static void process() {
        if (QUEUE.isEmpty())
            return;
        Iterator<SQLRunnable> i = QUEUE.iterator();
        while (i.hasNext()) {
            SQLRunnable entry = i.next();
            entry.run();
            i.remove();
        }
    }
    
    public static void submit(SQLRunnable query) {
        if (query != null)
            QUEUE.add(query);
    }
    
}
