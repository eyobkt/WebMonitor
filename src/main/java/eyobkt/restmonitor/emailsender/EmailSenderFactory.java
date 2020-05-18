package eyobkt.restmonitor.emailsender;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSenderFactory {
  
  private Session emailSession;
  private InternetAddress fromAddress;
  
  /**
   * @param displayName the name the recipient of an email will see alongside the sender address
   */
  public EmailSenderFactory(Session emailSession, String displayName) 
      throws UnsupportedEncodingException {
    
    if (emailSession == null || displayName == null) {
      throw new IllegalArgumentException();   
    }
    
    this.emailSession = emailSession;
    fromAddress = new InternetAddress(emailSession.getProperty("mail.smtp.user"), displayName);
  }
  
  public EmailSender createEmailSender() throws MessagingException {
    MimeMessage mimeMessage = new MimeMessage(emailSession);
    mimeMessage.setFrom(fromAddress);
    
    return new EmailSender(mimeMessage);    
  }
}