package eyobkt.restmonitor;

import java.sql.SQLException;

import javax.sql.DataSource;

public class MonitorDaoFactory {
  
  private DataSource dataSource;
  
  public MonitorDaoFactory(DataSource dataSource) {  
    if (dataSource == null) {
      throw new IllegalArgumentException();   
    }
    
    this.dataSource = dataSource; 
  }

  public MonitorDao createMonitorDao() throws SQLException {
    return new MonitorDao(dataSource.getConnection());
  }
}