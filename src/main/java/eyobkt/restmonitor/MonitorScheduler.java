package eyobkt.restmonitor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MonitorScheduler {
  
  ScheduledExecutorService scheduledExecutorService;
  
  public MonitorScheduler(MonitoringRound monitoringRound) {      
    scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    scheduledExecutorService.scheduleAtFixedRate(monitoringRound, 0, 10, TimeUnit.MINUTES);
  }
  
  public void shutdownNow() {
    scheduledExecutorService.shutdownNow();
  }
}