package eyobkt.restmonitor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MonitoringRound implements Runnable { 
  
  private MonitorDaoFactory monitorDaoFactory;
  private Session mailSession;
  private InternetAddress fromAddress;
  
  public MonitoringRound(MonitorDaoFactory monitorDaoFactory, Session mailSession) 
      throws UnsupportedEncodingException {
    
    if (monitorDaoFactory == null || mailSession == null) {
      throw new IllegalArgumentException();      
    }
    
    this.monitorDaoFactory = monitorDaoFactory;
    this.mailSession = mailSession;    
    this.fromAddress = new InternetAddress(mailSession.getProperty("mail.smtp.user"), "REST Monitor");
  }
  
  public void run() {    
    MonitorDao monitorDao = null;   
    ResultSet resultSet = null;
    Transport transport = null;
    try {   
      monitorDao = monitorDaoFactory.createMonitorDao();  
      resultSet = monitorDao.selectAllMonitors();   
      
      transport = mailSession.getTransport();
      transport.connect();
      
      while (resultSet.next()) {        
        URL url = new URL(resultSet.getString("url"));
        String email = resultSet.getString("email");
        long lastContentLength = resultSet.getLong("last_content_length");        
        
        URLConnection uc = url.openConnection();           
        long newContentLength = uc.getContentLengthLong();
        
        if (hasUpdated(lastContentLength, newContentLength)) {
          monitorDao.updateMonitor(url.toString(), email, newContentLength);          
          sendEmail(transport, url.toString(), email);
        }                 
      }
    } catch (SQLException | IOException | MessagingException e) {
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
      
      if (transport != null) {
        try {
          transport.close();
        } catch (MessagingException e) {
          e.printStackTrace();
        }
      }
    }
  }
  
  private boolean hasUpdated(long lastContentLength, long newContentLength) {
    if (newContentLength != lastContentLength) {
      return true;
    }
    
    return false;
  }
  
  private void sendEmail(Transport transport, String url, String email) {    
    try {
      MimeMessage mm = new MimeMessage(mailSession);
      mm.setRecipients(Message.RecipientType.TO, email);
      mm.setFrom(fromAddress);
      mm.setSubject("There has been an update to an API endpoint you are monitoring.");
      mm.setText("There has been a change to the resource at " + url + ".");      
      
      transport.sendMessage(mm, mm.getAllRecipients());
    } catch (MessagingException e) {
      e.printStackTrace();
    } 
  }
}