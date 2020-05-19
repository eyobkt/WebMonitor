package eyobkt.restmonitor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Runs a CheckingTask once an hour 
 */
public class CheckingTaskScheduler {
  
  ScheduledExecutorService scheduledExecutorService;
  
  public CheckingTaskScheduler(CheckingTask checkingTask) {      
    if (checkingTask == null) {
      throw new IllegalArgumentException();   
    }
    
    scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    scheduledExecutorService.scheduleAtFixedRate(checkingTask, 0, 60, TimeUnit.MINUTES);
  }
  
  public void shutdownNow() {
    scheduledExecutorService.shutdownNow();
  }
}