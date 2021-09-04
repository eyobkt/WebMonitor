package eyobkt.webmonitor.checkingtask;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Random;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eyobkt.webmonitor.monitor.Monitor;
import eyobkt.webmonitor.monitor.MonitorDao;
import eyobkt.webmonitor.monitor.MonitorDaoFactory;

/**
 * When an instance of this class is executed by a thread, the checkPageForChanges method of 
 * each Monitor is called once. Changes are stored in the Monitor data store  
 */
@Component
public class CheckingTask implements Runnable { 
  
  private MonitorDaoFactory monitorDaoFactory;
  private EmailSenderFactory emailSenderFactory;
  
  @Autowired
  public CheckingTask(MonitorDaoFactory monitorDaoFactory, EmailSenderFactory emailSenderFactory) 
      throws UnsupportedEncodingException {
    
    if (monitorDaoFactory == null || emailSenderFactory == null) {
      throw new IllegalArgumentException();      
    }
    
    this.monitorDaoFactory = monitorDaoFactory;
    this.emailSenderFactory = emailSenderFactory;    
  }
  
  public void run() {      
    Random random = new Random();
    try {
      Thread.sleep(random.nextInt(120001));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    
    try (MonitorDao monitorDao = monitorDaoFactory.create()) {
      Iterator<Monitor> monitorsIterator = monitorDao.selectAllMonitors().iterator();      
      EmailSender emailSender = emailSenderFactory.create();      
      
      while (monitorsIterator.hasNext()) {
        Monitor monitor = monitorsIterator.next();          
        
        try {
          if (monitor.checkPageForChanges(emailSender)) {
            monitorDao.updateMonitor(monitor);
          }
        } catch (SQLException | MessagingException | InterruptedException e) {
          e.printStackTrace();
        }
      }
    } catch (MessagingException | SQLException e) {
      e.printStackTrace();
    } 
  }
}
