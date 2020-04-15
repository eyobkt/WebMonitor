package eyobkt.restapichangemonitor;

import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class MonitorDaoFactory {
  
  private DataSource dataSource;
  
  public MonitorDaoFactory() {  
    try {
      Context c = new InitialContext();
      c = (Context) c.lookup("java:comp/env");
      dataSource = (DataSource) c.lookup("jdbc/rest_api_change_monitor_db");
    } catch (NamingException e) {
      e.printStackTrace();
      return;
    } 
  }

  public MonitorDao createMonitorDao() throws SQLException {
    return new MonitorDao(dataSource);
  }
}