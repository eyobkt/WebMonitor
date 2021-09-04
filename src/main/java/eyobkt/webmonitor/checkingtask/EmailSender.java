package eyobkt.webmonitor.checkingtask;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

public class EmailSender {
  
  private MimeMessage mimeMessage;
  
  public EmailSender(MimeMessage mimeMessage) throws MessagingException {    
    if (mimeMessage == null) {
      throw new IllegalArgumentException();   
    }
    
    this.mimeMessage = mimeMessage;  
  }
  
  public void sendEmail(String toAddress, String subject, String content) throws MessagingException {    
    mimeMessage.setRecipients(Message.RecipientType.TO, toAddress);  
    mimeMessage.setSubject(subject);
    mimeMessage.setText(content);      
    
    Transport.send(mimeMessage);
  }
}