package org.kozmobytes;

import java.util.concurrent.*;
import org.slf4j.*;

/**
 * Java's ThreadPoolExecutor is a great library, offering some very useful features. New tasks 
 * (runnables) are submitted via ThreadPoolExecutor 's execute(...) method, which hands off 
 * the task to one of its available workers or queue it.
 * 
 * However, if all the workers are busy, the pool is at its capacity and the queue is full, the
 * task is rejected and execute(...) throws RejectedExecutionException. The responsibility then
 * shifts back to the Caller thread which invoked execute(...) method to deal with the task
 * rejection.
 * 
 * Often times, it is desirable to block (the execute(...) method) Caller if the task cannot be 
 * accepted and to keep retrying task submission. There are several ways to achieve this, as
 * explained in the article: 
 * http://today.java.net/pub/a/today/2008/10/23/creating-a-notifying-blocking-thread-pool-executor.html
 * 
 * The BlockingThreadPoolExecutor class in this file implements a Blocking ThreadPoolExecutor as
 * explained in the above article. We attach a customer RejectedExecutionHandler to ThreadPoolExecutor
 * which keeps retrying task submission forever (until the thread-pool is alive). In addition, on 
 * each subsequent rejection or acceptance, handler methods are called giving them a chance to take
 * some application defined action.
 * 
 * Note: It is trivial to modify RejectedExecutionHandler to give up task submission after a specified
 * number of retries or if a timeout occurs, throwing RejectedExecutionException.
 * 
 * @author umermansoor
 */
public class BlockingThreadPoolExecutor extends ThreadPoolExecutor
{
    protected  final Logger logger = LoggerFactory.getLogger(BlockingThreadPoolExecutor.class);
    
    /**
     * RejectedExecutionHandler
     */
    private static final RejectedExecutionHandler rjeHandler = new RejectedExecutionHandler () {

        public void rejectedExecution(Runnable r, ThreadPoolExecutor tpe) 
        {
            // Retry count for intrumentation and debugging
            int retryCount = 0;
            
            // Try indefinitely to add the task to the queue
            while (true)
            {
                retryCount++;
                
                if (tpe.isShutdown())  // If the executor is shutdown, reject the task and 
                                       // throw RejectedExecutionException
                {
                    ((BlockingThreadPoolExecutor) tpe).taskRejectedGaveUp(r, retryCount);
                    throw new RejectedExecutionException("ThreadPool has been shutdown");
                }
                
                try
                {
                    if (tpe.getQueue().offer(r, 1, TimeUnit.SECONDS))
                    {
                        // Task got accepted! 
                        ((BlockingThreadPoolExecutor) tpe).taskAccepted(r, retryCount);
                        break;
                    }
                    else
                        ((BlockingThreadPoolExecutor) tpe).taskRejectedRetrying(r, retryCount);
                    
                }
                catch (InterruptedException e)
                {
                    throw new AssertionError(e);
                }
            } 
        }
        
    };
    
   
    /**
     * Default constructor. Create a ThreadPool of a single thread with a very large queue.
     */
    public BlockingThreadPoolExecutor()
    {
        this(1, 1, Integer.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("workerthread"));
    }
    
    /**
     * Constructor
     * @param corePoolSize - Number of threads of keep in the pool
     * @param maxPoolSize - Maximum number of threads that can be spawned
     * @param queueCapacity - Size of the task queue
     * @param baseThreadName - Name assigned to each worker thread
     * @param priority - Priority of worker threads
     * @param daemon - If true, workers threads are created as daemons. 
     * 
     */
    public BlockingThreadPoolExecutor(int corePoolSize, int maxPoolSize, int queueCapacity, String baseThreadName, int priority, boolean daemon)
    {
        this(corePoolSize, maxPoolSize, Integer.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(queueCapacity), new PriorityThreadFactory(baseThreadName, priority, daemon));
    }
    
    /**
     * Constructor
     * @param corePoolSize - Number of threads of keep in the pool
     * @param maxPoolSize - Maximum number of threads that can be spawned
     * @param keepAliveTime
     * @param unit
     * @param queue
     * @param threadFactory 
     */
    public BlockingThreadPoolExecutor(int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> queue, ThreadFactory threadFactory)
    {
        super(corePoolSize, maxPoolSize, keepAliveTime, unit, queue, threadFactory);
        allowCoreThreadTimeOut(true);
         
        // Attach a customer RejectedExecutionHandler defined above.
        this.setRejectedExecutionHandler(rjeHandler);
    }
    
    /**
     * Called when giving up on the task and rejecting it for good.
     * @param r
     * @param retryCount 
     */
    protected void taskRejectedGaveUp(Runnable r, int retryCount)
    {
        logger.debug("Gave Up: {}",retryCount );
    }
    
    /**
     * Called when the task that was rejected initially is rejected again.
     * @param r - Task
     * @param retryCount - number of total retries 
     */
    protected void taskRejectedRetrying(Runnable r, int retryCount)
    {
        logger.debug("Retrying: {}",retryCount );
    }
    
    /**
     * Called when the rejected task is finally accepted.
     * @param r - Task
     * @param retryCount - number of retries before acceptance
     */
    protected void taskAccepted(Runnable r, int retryCount)
    {
        logger.debug("Accepted: {}",retryCount );
    }

  

    
    
}
