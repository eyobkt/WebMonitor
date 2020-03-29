import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DataAccessObject {  
  private Connection connection; 
  private ResultSet resultSet;
  
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
  
  public DataAccessObject() {
    try {
      connection = dataSource.getConnection();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
    
  public void insertMonitor(String url, String email) {
    try {    
      String sql = "INSERT INTO monitors "  
                 + "VALUES(?, ?, -1, null)";
      PreparedStatement ps = connection.prepareStatement(sql);
      ps.setString(1, url);
      ps.setString(2, email);
      ps.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }    
  }
  
  public void deleteMonitor(String url, String email) {
    try {      
      String sql = "DELETE FROM monitors "
                 + "WHERE url = ? AND email = ?";
      PreparedStatement ps = connection.prepareStatement(sql);
      ps.setString(1, url);
      ps.setString(2, email);
      ps.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }    
  }
  
  public ResultSet selectAllMonitors() throws SQLException {
    String sql = "SELECT * "
               + "FROM monitors";
    Statement s = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE
        , ResultSet.CONCUR_UPDATABLE); 
    resultSet = s.executeQuery(sql);
    return resultSet;
  }
  
  public void updateLastContentLength(long contentLength) {
    try {
      resultSet.updateLong("last_content_length", contentLength);
      resultSet.updateRow();
    } catch (SQLException e) {
      e.printStackTrace();
    }    
  }
  
  public void closeConnection() {
    try {
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}