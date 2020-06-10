package eyobkt.restmonitor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.mail.MessagingException;

import eyobkt.restmonitor.emailsender.EmailSender;
import eyobkt.restmonitor.emailsender.EmailSenderFactory;

/**
 * When an instance of this class is executed by a thread, the checkUrlResponseForChanges method 
 * of each Monitor is called once. Changes are stored in the Monitor data store  
 */
public class CheckingTask implements Runnable { 
  
  private MonitorDaoFactory monitorDaoFactory;
  private EmailSenderFactory emailSenderFactory;
  
  public CheckingTask(MonitorDaoFactory monitorDaoFactory, EmailSenderFactory emailSenderFactory) 
      throws UnsupportedEncodingException {
    
    if (monitorDaoFactory == null || emailSenderFactory == null) {
      throw new IllegalArgumentException();      
    }
    
    this.monitorDaoFactory = monitorDaoFactory;
    this.emailSenderFactory = emailSenderFactory;    
  }
  
  public void run() {                    
    try (MonitorDao monitorDao = monitorDaoFactory.createMonitorDao()) {      
      List<Monitor> monitors = monitorDao.selectAllMonitors();
      Iterator<Monitor> monitorsIterator = monitors.iterator();
      
      EmailSender emailSender = emailSenderFactory.createEmailSender();      
      
      while (monitorsIterator.hasNext()) {
        Monitor monitor = monitorsIterator.next();          
        
        try {
          if (monitor.checkUrlResponseForChanges(emailSender)) {
            monitorDao.updateMonitor(monitor);
          }
        } catch (IOException | SQLException | MessagingException e) {
          e.printStackTrace();
        }
      }
    } catch (MessagingException | SQLException e) {
      e.printStackTrace();
    } 
  }
}