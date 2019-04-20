package springapp.tests.async;

import java.util.function.Supplier;

public class AsyncDemoTask implements Supplier<String> {

	@Override
	public String get() {
		
		System.out.println("Execute method asynchronously - " + Thread.currentThread().getName());
		
		try {
	        Thread.sleep(250);
	        
	        System.out.println("Task did run 250 ms.");
	        return "hello world";
	        
	    } catch (InterruptedException e) {
	    	System.out.println("InterruptedException");
	    } catch (Throwable e) {
	    	System.out.println("Task threw an exception " + e);
		}
		
		return null;
	}

	
	
}
