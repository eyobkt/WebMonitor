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

@WebListener
public class EntryPoint implements ServletContextListener {
  
  private MonitorScheduler monitorScheduler;
  
  public void contextInitialized(ServletContextEvent sce) {
    ServletContext servletContext = sce.getServletContext();     
    
    DataSource ds = createDataSource(servletContext);
    MonitorDaoFactory monitorDaoFactory = new MonitorDaoFactory(ds);     
    
    servletContext.addServlet("monitorService", new MonitorService(monitorDaoFactory))
        .addMapping("/api/v1/monitor");
    
    Session s = createMailSession(servletContext);    
    MonitoringRound mr = null;
    try {
      mr = new MonitoringRound(monitorDaoFactory, s);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return;
    }    
    monitorScheduler = new MonitorScheduler(mr);    
  }
  
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
  
  private Session createMailSession(ServletContext servletContext) {
    Properties p = new Properties();
    p.setProperty("mail.smtp.host", servletContext.getInitParameter("EMAIL_HOST"));
    p.setProperty("mail.smtp.user", servletContext.getInitParameter("EMAIL_ADDRESS"));
    p.setProperty("password", servletContext.getInitParameter("EMAIL_PASSWORD"));
    p.setProperty("mail.smtp.starttls.enable", "true");
    p.setProperty("mail.smtp.auth", "true");
    p.setProperty("mail.smtp.port", "587");
    
    return Session.getInstance(p, new Authenticator() {
      
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(p.getProperty("mail.smtp.user"), p.getProperty("password"));
      }
    });
  }
  
  public void contextDestroyed(ServletContextEvent sce) {
    monitorScheduler.shutdownNow();
  }
}