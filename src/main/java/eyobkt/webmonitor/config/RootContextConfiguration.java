package eyobkt.webmonitor.config;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.sql.DataSource;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;

@Configuration
@ComponentScan (
  basePackages = "eyobkt.webmonitor",
  excludeFilters = @ComponentScan.Filter({Configuration.class, Controller.class})
)
@PropertySource("classpath:db.properties")
@PropertySource("classpath:email.properties")
public class RootContextConfiguration {
  
  @Autowired
  private Environment environment;
  
  /**
   * Creates a pool of connections to the application's data store
   */
  @Bean
  public DataSource dataSource() {
    BasicDataSource basicDataSource = new BasicDataSource();
    basicDataSource.setUsername(environment.getProperty("db.username"));
    basicDataSource.setPassword(environment.getProperty("db.password"));
    basicDataSource.setDriverClassName(environment.getProperty("db.driver-class-name"));
    basicDataSource.setUrl(environment.getProperty("db.url"));
    basicDataSource.setMaxTotal(20);
    basicDataSource.setMaxIdle(5);
    basicDataSource.setMaxWaitMillis(-1);    
    
    return basicDataSource;
  }  
  
  @Bean
  public String displayName() {
    return environment.getProperty("email.display-name");
  }
  
  @Bean
  public long period() {
    return 59L;
  }  
  
  /**
   * Creates a JavaMail Session 
   */
  @Bean
  public Session session() {
    Properties properties = new Properties();
    properties.setProperty("mail.smtp.host", environment.getProperty("email.host"));
    properties.setProperty("mail.smtp.user", environment.getProperty("email.address"));
    properties.setProperty("password", environment.getProperty("email.password"));
    properties.setProperty("mail.smtp.starttls.enable", "true");
    properties.setProperty("mail.smtp.auth", "true");
    properties.setProperty("mail.smtp.port", "587");
    
    Authenticator authenticator = new Authenticator() {
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(environment.getProperty("email.address")
            , environment.getProperty("email.password"));
      }
    };    
    
    return Session.getInstance(properties, authenticator);
  }
}
