import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class MonitorDaoImpl implements MonitorDao {  
  
  private Connection connection; 
  
  private static DataSource dataSource;   
  
  static {    
    try {
      Context c = new InitialContext();
      c = (Context) c.lookup("java:comp/env");
      dataSource = (DataSource) c.lookup("jdbc/restApiChangeMonitorDb");
    } catch (NamingException e) {
      e.printStackTrace();
    }
  }  
  
  public MonitorDaoImpl() throws SQLException {
    connection = dataSource.getConnection();
  }
    
  public void insertMonitor(String url, String email, long contentLength) throws SQLException {
    String sql = "INSERT INTO monitor "  
               + "VALUES(?, ?, ?, null)";
    PreparedStatement ps = connection.prepareStatement(sql);
    ps.setString(1, url);
    ps.setString(2, email);
    ps.setLong(3, contentLength);
    ps.executeUpdate(); 
  }
  
  public void deleteMonitor(String url, String email) throws SQLException {
    String sql = "DELETE FROM monitor "
               + "WHERE url = ? AND email = ?";
    PreparedStatement ps = connection.prepareStatement(sql);
    ps.setString(1, url);
    ps.setString(2, email);
    ps.executeUpdate();  
  }
  
  public boolean containsMonitor(String url, String email) throws SQLException {    
    return selectMonitor(url, email).next();
  }
  
  public ResultSet selectMonitor(String url, String email) throws SQLException {
    String sql = "SELECT * "
               + "FROM monitor "
               + "WHERE url = ? AND email = ?";
    PreparedStatement ps = connection.prepareStatement(sql);
    ps.setString(1, url);
    ps.setString(2, email);
    return ps.executeQuery(sql);
  }
  
  public ResultSet selectAllMonitors() throws SQLException {
    String sql = "SELECT * "
               + "FROM monitor";
    Statement s = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE
        , ResultSet.CONCUR_UPDATABLE); 
    return s.executeQuery(sql);
  }
  
  public void updateMonitor(String url, String email, long contentLength) throws SQLException {
    String sql = "UPDATE monitor "
               + "SET last_content_length = ? "
               + "WHERE url = ? AND email = ?";
    PreparedStatement ps = connection.prepareStatement(sql);
    ps.setLong(1, contentLength);
    ps.setString(2, url);
    ps.setString(3, email);    
    ps.executeUpdate();      
  }
  
  public void closeConnection() throws SQLException {
    connection.close();
  }
}