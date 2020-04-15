package eyobkt.restapichangemonitor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

public class MonitorDao {  
  
  private Connection connection;    
  
  public MonitorDao(DataSource dataSource) throws SQLException {    
    connection = dataSource.getConnection();
  }
    
  public void insertMonitor(String url, String email, long contentLength) throws SQLException {
    String sql = "INSERT INTO monitor "  
               + "VALUES(?, ?, ?, null)";
    try {      
      PreparedStatement ps = connection.prepareStatement(sql);
      ps.setString(1, url);
      ps.setString(2, email);
      ps.setLong(3, contentLength);
      ps.executeUpdate(); 
    } catch (SQLException e) {
      if (e.getErrorCode() == 1062) {
        throw new PrimaryKeyConstraintViolationException();
      } else {
        throw e;
      }
    }    
  }
  
  public int deleteMonitor(String url, String email) throws SQLException {    
    String sql = "DELETE FROM monitor "
               + "WHERE url = ? AND email = ?";
    PreparedStatement ps = connection.prepareStatement(sql);
    ps.setString(1, url);
    ps.setString(2, email);
    return ps.executeUpdate();  
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
    return ps.executeQuery();
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
  
  public void closeConnection() {
    try {
      connection.close();
    } catch (SQLException e) {
    }
  }
}