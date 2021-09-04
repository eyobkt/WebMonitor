package eyobkt.webmonitor.checkingtask;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Runs a Runnable once every number of minutes specified by the period argument
 */
@Component
public class TaskScheduler {
  
  ScheduledExecutorService scheduledExecutorService;
  
  @Autowired
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
