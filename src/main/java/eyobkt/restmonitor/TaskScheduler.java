package eyobkt.restmonitor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Runs a Runnable once every number of minutes specified by the period argument
 */
public class TaskScheduler {
  
  ScheduledExecutorService scheduledExecutorService;
  
  public TaskScheduler(Runnable runnable, long period) {      
    if (runnable == null) {
      throw new IllegalArgumentException();   
    }
    
    scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    scheduledExecutorService.scheduleAtFixedRate(runnable, 0, period, TimeUnit.MINUTES);
  }
  
  public void shutdownNow() {
    scheduledExecutorService.shutdownNow();
  }
}