package eyobkt.webmonitor.checkingtask;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailSenderFactory {
  
  private Session session;
  private InternetAddress fromAddress;
  
  /**
   * @param displayName the name the recipient of an email will see alongside the sender address
   */
  @Autowired
  public EmailSenderFactory(Session session, String displayName) 
      throws UnsupportedEncodingException {
    
    if (session == null || displayName == null) {
      throw new IllegalArgumentException();   
    }
    
    this.session = session;
    fromAddress = new InternetAddress(session.getProperty("mail.smtp.user"), displayName);
  }
  
  public EmailSender create() throws MessagingException {
    MimeMessage mimeMessage = new MimeMessage(session);
    mimeMessage.setFrom(fromAddress);
    
    return new EmailSender(mimeMessage);    
  }
}
