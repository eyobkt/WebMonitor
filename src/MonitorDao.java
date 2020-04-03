import java.sql.ResultSet;
import java.sql.SQLException;

public interface MonitorDao {  
  void insertMonitor(String url, String email, long contentLength) throws SQLException;
  
  void deleteMonitor(String url, String email) throws SQLException;
  
  boolean containsMonitor(String url, String email) throws SQLException;
  
  ResultSet selectMonitor(String url, String email) throws SQLException;
  
  ResultSet selectAllMonitors() throws SQLException;
  
  void updateMonitor(String url, String email, long contentLength) throws SQLException;
  
  void closeConnection() throws SQLException;
}