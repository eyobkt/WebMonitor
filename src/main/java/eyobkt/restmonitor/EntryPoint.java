package eyobkt.restmonitor;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import eyobkt.restmonitor.emailsender.EmailSenderFactory;

/**
 * The class first loaded. contextInitialized() is called at the beginning of the application, and 
 * contextDestroyed() is called at the end 
 */
@WebListener
public class EntryPoint implements ServletContextListener {
  
  private TaskScheduler taskScheduler;
  
  /**
   * Creates a MonitorService servlet and calls scheduleCheckingTasks(). The former is done programmatically
   * , rather than through the deployment descriptor or by annotation, so that the servlet's dependencies 
   * can be injected
   */
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    ServletContext servletContext = servletContextEvent.getServletContext();   
    
    DataSource dataSource = createDataSource(servletContext);
    MonitorDaoFactory monitorDaoFactory = new MonitorDaoFactory(dataSource);     
    
    servletContext.addServlet("monitorService", new MonitorService(monitorDaoFactory))
        .addMapping("/api/v1/monitor");
    
    try {
      scheduleCheckingTasks(servletContext, monitorDaoFactory);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }   
  }  
  
  /**
   * Uses the context init parameters defined in the deployment descriptor to create a pool of 
   * connections to the application's data store
   */
  private DataSource createDataSource(ServletContext servletContext) {
    BasicDataSource basicDataSource = new BasicDataSource();
    basicDataSource.setUsername(servletContext.getInitParameter("DB_USERNAME"));
    basicDataSource.setPassword(servletContext.getInitParameter("DB_PASSWORD"));
    basicDataSource.setDriverClassName(servletContext.getInitParameter("DB_DRIVER_CLASS_NAME"));
    basicDataSource.setUrl(servletContext.getInitParameter("DB_URL"));
    basicDataSource.setMaxTotal(20);
    basicDataSource.setMaxIdle(5);
    basicDataSource.setMaxWaitMillis(-1);    
    
    return basicDataSource;
  }
  
  /**
   * Creates a TaskScheduler that runs a CheckingTask once every 59 minutes
   */
  private void scheduleCheckingTasks(ServletContext servletContext
      , MonitorDaoFactory monitorDaoFactory) throws UnsupportedEncodingException {
    
    Session emailSession = createEmailSession(servletContext);
    EmailSenderFactory emailSenderFactory = new EmailSenderFactory(emailSession, "REST Monitor");    
    CheckingTask checkingTask = new CheckingTask(monitorDaoFactory, emailSenderFactory);
    
    taskScheduler = new TaskScheduler(checkingTask, 59); 
  }  
  
  /**
   * Uses the context init parameters defined in the deployment descriptor to create a JavaMail 
   * Session 
   */
  private Session createEmailSession(ServletContext servletContext) {
    Properties properties = new Properties();
    properties.setProperty("mail.smtp.host", servletContext.getInitParameter("EMAIL_HOST"));
    properties.setProperty("mail.smtp.user", servletContext.getInitParameter("EMAIL_ADDRESS"));
    properties.setProperty("password", servletContext.getInitParameter("EMAIL_PASSWORD"));
    properties.setProperty("mail.smtp.starttls.enable", "true");
    properties.setProperty("mail.smtp.auth", "true");
    properties.setProperty("mail.smtp.port", "587");
    
    return Session.getInstance(properties, new Authenticator() {
      
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(properties.getProperty("mail.smtp.user"), properties.getProperty("password"));
      }
    });
  }  

  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    taskScheduler.shutdownNow();
  }
}