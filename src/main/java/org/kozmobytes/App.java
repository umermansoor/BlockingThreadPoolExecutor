package org.kozmobytes;

import java.util.concurrent.*;

/**
 * Hello world!
 *
 */
public class App 
{
    private BlockingThreadPoolExecutor tpe;
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        
        App a = new App();
        a.go();

        for (int i=0; i < 10; i++)
            try { a.newTask("task" + i); } catch (RejectedExecutionException rje) { System.out.println("task" + i + " Rejected");} 

        
        System.out.println("done here");
    }
                
    public ThreadPoolExecutor getTpe()
    {
        return tpe;
    }
    
    public void go()
    {
        tpe = new BlockingThreadPoolExecutor(3, 4, 2, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(2), new PriorityThreadFactory("thread"));
        
    }
    
    public void newTask(final String id) throws RejectedExecutionException
    {
        Runnable r = new Runnable()
                {
                    public void run()
                    {
                        int i = 0;
                        System.err.println(id + " is running");
                        
                        while (i++ < 50)
                        {
                            try { Thread.sleep(100); } catch (InterruptedException e){ System.out.println("Intrrupted"); continue;}
                        }
                        
                        //System.err.println(id + " is dead");
                        
                    }
                    
                };
        
        
        tpe.execute(r);
                
    }
}
