package eyobkt.restmonitor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MonitorScheduler {
  
  ScheduledExecutorService seService;
  
  public MonitorScheduler(MonitorDaoFactory monitorDaoFactory) {      
    seService = Executors.newSingleThreadScheduledExecutor();
    seService.scheduleAtFixedRate(new MonitoringRound(monitorDaoFactory), 0, 2, TimeUnit.MINUTES);
  }
  
  public void shutdownNow() {
    seService.shutdownNow();
  }
}