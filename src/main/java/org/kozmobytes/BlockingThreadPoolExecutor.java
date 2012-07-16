package org.kozmobytes;

import java.util.concurrent.*;

/**
 *
 * @author umermansoor
 */
public class BlockingThreadPoolExecutor extends ThreadPoolExecutor
{
    public BlockingThreadPoolExecutor()
    {
        // Single core thread, normal priority
        this(1, 1, Integer.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("workerthread"));
    }
    
    public BlockingThreadPoolExecutor(int corePoolSize, int maxPoolSize, int capacity, String baseThreadName, int priority)
    {
        this(corePoolSize, maxPoolSize, Integer.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(capacity), new PriorityThreadFactory(baseThreadName, priority));
    }

  

    public BlockingThreadPoolExecutor(int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> queue, ThreadFactory threadFactory)
    {
        super(corePoolSize, maxPoolSize, keepAliveTime, unit, queue, threadFactory);
        allowCoreThreadTimeOut(true);
         
        /*
         * Default behaviour of ThreadPoolExecutor's execute(...) function is to reject new tasks
         * (runnables) in the work queue has reached it's capacity and no new threads can be created. 
         * In this case, execute(...) throws RejectedExecutionException. However, in many cases, it 
         * is desired to block on queue (keep retrying) until it has room. A custom RejectedExecutionHandler
         * is provided below to that effect.
         * [source: http://today.java.net/pub/a/today/2008/10/23/creating-a-notifying-blocking-thread-pool-executor.html]
         */
        
        /**
         * Alternatively, ThreadPoolExecutor also supports other actions. See the following link 
         * for more info:
         * [http://docs.oracle.com/javase/1.5.0/docs/api/java/util/concurrent/ThreadPoolExecutor.html]
         */
        //this.setRejectedExecutionHandler(blockingExecutionHandler);
    }
    
}
