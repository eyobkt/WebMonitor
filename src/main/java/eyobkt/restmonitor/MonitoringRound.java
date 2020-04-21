package eyobkt.restmonitor;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
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
  private Session mailSession;
  
  public MonitoringRound(MonitorDaoFactory monitorDaoFactory) {
    if (monitorDaoFactory == null) {
      throw new IllegalArgumentException();      
    }
    
    this.monitorDaoFactory = monitorDaoFactory;
    
    try {
      Context c = new InitialContext();
      c = (Context) c.lookup("java:comp/env");
      mailSession = (Session) c.lookup("mail/Session");
    } catch (NamingException e) {
      e.printStackTrace();
      return;
    }    
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
        
        URLConnection uc = url.openConnection();
        
        long newContentLength = uc.getContentLengthLong();
        if (newContentLength != lastContentLength) {
          monitorDao.updateMonitor(url.toString(), email, newContentLength);          
          sendEmail(url.toString(), email);
        }           
      }
    } catch (SQLException | IOException e) {
      e.printStackTrace();
    } finally {
      if (resultSet != null) {
        try {
          resultSet.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
      
      if (monitorDao != null) {
        monitorDao.closeConnection();    
      }            
    }
  }
  
  private void sendEmail(String url, String email) {    
    try {
      MimeMessage mm = new MimeMessage(mailSession);
      mm.setRecipients(Message.RecipientType.TO, email);
      mm.setSubject("There has been an update to an API endpoint you are monitoring.");
      mm.setText("There has been a change to the resource at " + url + ".");      
      
      Transport.send(mm);
    } catch (MessagingException e) {
      e.printStackTrace();
    } 
  }
}