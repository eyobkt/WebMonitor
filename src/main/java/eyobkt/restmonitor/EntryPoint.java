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
 * The class first loaded
 */
@WebListener
public class EntryPoint implements ServletContextListener {
  
  private CheckingTaskScheduler checkingTaskScheduler;
  
  /**
   * Creates a MonitorService servlet and a CheckingTaskScheduler at the beginning of the application. 
   * The former is done programmatically, rather than through the deployment descriptor or by 
   * annotation, so that the servlet's dependencies can be injected
   */
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    ServletContext servletContext = servletContextEvent.getServletContext();     
    
    DataSource dataSource = createDataSource(servletContext);
    MonitorDaoFactory monitorDaoFactory = new MonitorDaoFactory(dataSource);     
    
    servletContext.addServlet("monitorService", new MonitorService(monitorDaoFactory))
        .addMapping("/api/v1/monitor");
    
    try {
      checkingTaskScheduler = createCheckingTaskScheduler(servletContext, monitorDaoFactory);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return;
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
  
  private CheckingTaskScheduler createCheckingTaskScheduler(ServletContext servletContext
      , MonitorDaoFactory monitorDaoFactory) throws UnsupportedEncodingException {
    
    Session emailSession = createEmailSession(servletContext);
    EmailSenderFactory emailSenderFactory = new EmailSenderFactory(emailSession, "REST Monitor");    
    CheckingTask checkingTask = new CheckingTask(monitorDaoFactory, emailSenderFactory);
    
    return new CheckingTaskScheduler(checkingTask); 
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
  
  /**
   * Shuts down the CheckingTaskScheduler at the ending of the application 
   */
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    checkingTaskScheduler.shutdownNow();
  }
}