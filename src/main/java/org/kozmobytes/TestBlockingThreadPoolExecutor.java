package org.kozmobytes;

import java.util.concurrent.*;
import org.slf4j.*;

/**
 * Simple way to test the BlockingThreadPoolExecutor side by side with ThreadPoolExecutor.
 * @author umermansoor
 */
public class TestBlockingThreadPoolExecutor 
{
    private final BlockingThreadPoolExecutor blockingTpe;
    private final ThreadPoolExecutor defaultTpe;
    private static final Logger logger = LoggerFactory.getLogger(TestBlockingThreadPoolExecutor.class);
    
    public TestBlockingThreadPoolExecutor()
    {
        int corePoolSize = 3,
            maxPoolSize = 4,
            queueCapacity = 2;
        
        // Create ThreadPools
        logger.debug("Creating BlockingThreadPoolExecutor and ThreadPoolExecutor with: corePoolSize: {}, maxPoolSize: {}, queueCapacity: {}", 
                                                    new Object[]{corePoolSize, maxPoolSize, queueCapacity});
        blockingTpe = new BlockingThreadPoolExecutor(corePoolSize, maxPoolSize, Integer.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(queueCapacity), new PriorityThreadFactory("workerThread"));
        defaultTpe = new ThreadPoolExecutor(corePoolSize, maxPoolSize, Integer.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(queueCapacity), new PriorityThreadFactory("workerThread"));
    }
    
    public void go()
    {
        //Create some tasks and submit to both ThreadPools
        int rejectCount = 0;
        
        logger.debug("Submitting tasks to ThreadPoolExecutor");
        for (int i=0; i < 10; i++)
            try { createAndSubmitTask("thread" + i, defaultTpe); } catch (RejectedExecutionException rje) { logger.error("task" + i + ": Rejected"); rejectCount++;} 
        logger.debug("Done with ThreadPoolExecutor. Total task rejections: {}", rejectCount);
         
        logger.debug("Submitting tasks to BlockingThreadPoolExecutor");
        rejectCount =0;
        for (int i=0; i < 10; i++)
            try { createAndSubmitTask("thread" + i, blockingTpe); } catch (RejectedExecutionException rje) { logger.error("task" + i + ": Rejected"); rejectCount++;} 
        logger.debug("Done with BlockingThreadPoolExecutor. Total task rejections: {}", rejectCount);
     
    }
    
    
    private void createAndSubmitTask(final String taskId, ThreadPoolExecutor tpe) throws RejectedExecutionException
    {
        // Create a dummy task to execute for a few seconds
        Runnable r = new Runnable()
                {
                    public void run()
                    {
                        int i = 0;
                        logger.debug(taskId + ": is running");
                        
                        while (i++ < 50)
                        {
                            try { Thread.sleep(100); } catch (InterruptedException e){ logger.error("Intrrupted"); break;}
                        }
                        
                        logger.debug(taskId + ": is done");
                    }
                    
                };
        
        //Submit task to ThreadPoolExecutor
        // Will call the correct execute(...) method using dynamic binding.
        tpe.execute(r);     
    }
    
    
}
