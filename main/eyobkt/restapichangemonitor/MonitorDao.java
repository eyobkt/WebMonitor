package eyobkt.restapichangemonitor;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface MonitorDao {
  
  public void insertMonitor(String url, String email, long contentLength) throws SQLException;
  
  public int deleteMonitor(String url, String email) throws SQLException;   
  
  public ResultSet selectAllMonitors() throws SQLException;
  
  public void updateMonitor(String url, String email, long contentLength) throws SQLException;
  
  public void closeConnection();
}