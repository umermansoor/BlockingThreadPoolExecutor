package org.kozmobytes;

/**
 * Application Entry Point. Maven generated.
 *
 */
public class App 
{
    private BlockingThreadPoolExecutor tpe;
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        
        TestBlockingThreadPoolExecutor testExecutors = new TestBlockingThreadPoolExecutor();
        testExecutors.go();
    }
                
    
}
