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
  public void run() {    
    DataAccessObject dataAccessObject = new DataAccessObject();    
    try {      
      ResultSet resultSet = dataAccessObject.selectAllMonitors();  
      while (resultSet.next()) {        
        URL url = new URL(resultSet.getString("url"));
        String email = resultSet.getString("email");
        long lastContentLength = resultSet.getLong("last_content_length");
        
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        
        long contentLength = huc.getContentLengthLong();
        if (contentLength != lastContentLength) {
          dataAccessObject.updateLastContentLength(contentLength);          
          sendEmail(url.toString(), email);
          return;
        }           
      }
    } catch (SQLException | IOException e) {
      e.printStackTrace();
    } finally {
      dataAccessObject.closeConnection();      
    }
  }
  
  private void sendEmail(String url, String email) {    
    try {
      Context c = new InitialContext();
      c = (Context) c.lookup("java:comp/env");
      Session s = (Session) c.lookup("mail/Session");
      
      MimeMessage mm = new MimeMessage(s);
      mm.setRecipients(Message.RecipientType.TO, email);
      mm.setSubject("There has been an update to an API endpoint you are monitoring");
      mm.setText("There has been a change to the resource at " + url);
      
      Transport.send(mm);
    } catch (NamingException | MessagingException e) {
      e.printStackTrace();
    } 
  }
}