package eyobkt.restapichangemonitor;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class MonitoringRound implements Runnable { 
  
  private MonitorDaoFactory monitorDaoFactory;
  
  public MonitoringRound(MonitorDaoFactory monitorDaoFactory) {
    if (monitorDaoFactory == null) {
      throw new IllegalArgumentException();      
    }
    
    this.monitorDaoFactory = monitorDaoFactory;
  }
  
  public void run() {    
    MonitorDao monitorDao = null;   
    ResultSet resultSet = null;
    try {   
      monitorDao = monitorDaoFactory.createMonitorDao();  
      resultSet = monitorDao.selectAllMonitors();  
      while (resultSet.next()) {        
        URL url = new URL(resultSet.getString("url"));
        String email = resultSet.getString("email");
        long lastContentLength = resultSet.getLong("last_content_length");
        
        HttpURLConnection uc = (HttpURLConnection) url.openConnection();
        
        long contentLength = uc.getContentLengthLong();
        if (contentLength != lastContentLength) {
          monitorDao.updateMonitor(url.toString(), email, contentLength);          
          sendEmail(url.toString(), email);
          return;
        }           
      }
    } catch (SQLException | IOException e) {
      e.printStackTrace();
    } finally {
      if (monitorDao != null) {
        monitorDao.closeConnection();    
      }
      
      if (resultSet != null) {
        try {
          resultSet.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
            
    }
  }
  
  private void sendEmail(String url, String email) {    
    try {
      Context c = new InitialContext();
      c = (Context) c.lookup("java:comp/env");
      Session s = (Session) c.lookup("mail/Session");
      
      MimeMessage mm = new MimeMessage(s);
      mm.setRecipients(Message.RecipientType.TO, email);
      mm.setSubject("There has been an update to an API endpoint you are monitoring.");
      mm.setText("There has been a change to the resource at " + url + ".");
      
      Transport.send(mm);
    } catch (NamingException | MessagingException e) {
      e.printStackTrace();
    } 
  }
}