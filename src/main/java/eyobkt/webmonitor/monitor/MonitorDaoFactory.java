package eyobkt.webmonitor.monitor;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MonitorDaoFactory {
  
  private DataSource dataSource;
  
  @Autowired
  public MonitorDaoFactory(DataSource dataSource) {  
    if (dataSource == null) {
      throw new IllegalArgumentException();   
    }
    
    this.dataSource = dataSource; 
  }

  public MonitorDao create() throws SQLException {
    return new MonitorDao(dataSource.getConnection());
  }
}
