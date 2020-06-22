package eyobkt.restmonitor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of a Data Access Object for Monitors
 */
public class MonitorDao implements AutoCloseable {  
  
  private Connection connection;    
  
  public MonitorDao(Connection connection) throws SQLException {  
    if (connection == null) {
      throw new IllegalArgumentException();      
    }
    
    this.connection = connection;
  }
    
  public void insertMonitor(Monitor monitor) throws SQLException {
    String sql = "INSERT INTO monitor "  
               + "VALUES(?, ?, 0, 0, ?)";
    PreparedStatement preparedStatement = null;
    
    try {      
      preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setString(1, monitor.getUrl());
      preparedStatement.setString(2, monitor.getEmail());
      preparedStatement.setString(3, monitor.getLastContent());
      
      preparedStatement.executeUpdate(); 
    } catch (SQLException e) {
      if (e.getErrorCode() == 1062) {
        throw new PrimaryKeyConstraintViolationException();
      } else {
        throw e;
      }
    } finally {
      if (preparedStatement != null) {
        try {
          preparedStatement.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }        
      }
    }
  }
  
  public int deleteMonitor(String url, String email) throws SQLException {    
    String sql = "DELETE FROM monitor "
               + "WHERE url = ? AND email = ?";
    PreparedStatement preparedStatement = null;
    
    try {      
      preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setString(1, url);
      preparedStatement.setString(2, email);
      
      return preparedStatement.executeUpdate(); 
    } catch (SQLException e) {
      throw e;
    } finally {
      if (preparedStatement != null) {
        try {
          preparedStatement.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }        
      }
    }
  }
  
  public List<Monitor> selectAllMonitors() throws SQLException {
    String sql = "SELECT * "
               + "FROM monitor";
    Statement statement = null;
    ResultSet resultSet = null;
    
    try {
      statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE
          , ResultSet.CONCUR_UPDATABLE); 
      resultSet = statement.executeQuery(sql);    
      
      ArrayList<Monitor> monitors = new ArrayList<>();
      
      while(resultSet.next()) {
        String url = resultSet.getString("url");
        String email = resultSet.getString("email");
        String lastContent = resultSet.getString("last_content");
        
        monitors.add(new Monitor(url, email, lastContent));
      }         
      
      return monitors;
    } catch(SQLException e) {
      throw e;
    } finally {
      if (resultSet != null) {
        try {
          resultSet.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }        
      }      
      
      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }        
      }      
    }          
  }
  
  public void updateMonitor(Monitor monitor) throws SQLException {
    String sql = "UPDATE monitor "
               + "SET last_content = ? "
               + "WHERE url = ? AND email = ?";
    PreparedStatement preparedStatement = null;
    
    try {      
      preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setString(1, monitor.getLastContent());
      preparedStatement.setString(2, monitor.getUrl());
      preparedStatement.setString(3, monitor.getEmail());    
      
      preparedStatement.executeUpdate();    
    } catch (SQLException e) {
      throw e;
    } finally {
      if (preparedStatement != null) {
        try {
          preparedStatement.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }        
      }
    }   
  }
  
  public void close() {
    try {
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}