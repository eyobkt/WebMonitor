package eyobkt.restmonitor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import eyobkt.restmonitor.emailsender.EmailSender;

public class Monitor {
  
  /**
   * The URL that is checked
   */
  private URL url;  
  
  /**
   * The email address to which notifications are sent
   */
  private String email;  
  
  /**
   * The HTTP status code returned the last time url was checked
   */
  private int lastStatusCode;  
  
  /**
   * The value of the Content-Length header the last time url was checked and the status code 
   * returned was 200. -1 if there was no such header
   */
  private long lastContentLength;  
  
  /**
   * The response body the last time url was checked and the status code returned was 200
   */
  private String lastContent;

  /**
   * The constructor to be used when a Monitor is first created. Validates inputs 
   * 
   * @param url the URL to be checked
   * @param email the email address to which notifications are to be sent
   */
  Monitor(String url, String email) throws IllegalArgumentException, IOException {    
    Optional<String> errorMessage = validateUrl(url);
    
    if (errorMessage.isPresent()) {    
      throw new IllegalArgumentException(errorMessage.get());
    }    
    
    errorMessage = validateEmail(email);
    if (errorMessage.isPresent()) {    
      throw new IllegalArgumentException(errorMessage.get());
    }    
    
    this.url = new URL(url);
    this.email = email;
    lastStatusCode = 200;
    
    HttpURLConnection httpUrlConnection = (HttpURLConnection) this.url.openConnection();
    lastContentLength = httpUrlConnection.getContentLengthLong();
    lastContent = getContent(httpUrlConnection.getInputStream());
  }
  
  /**
   * The constructor to be used when a Monitor is reloaded from the data store
   * 
   * @param url the URL to be checked
   * @param email the email address to which notifications are to be sent
   * @param lastStatusCode the HTTP status code returned the last time url was checked
   * @param lastContentLength the value of the Content-Length header the last time url was checked 
   *     and the status code returned was 200
   * @param lastContent the response body the last time url was checked and the status code 
   *     returned was 200     
   */
  Monitor(String url, String email, int lastStatusCode, long lastContentLength, String lastContent) {    
    try {
      this.url = new URL(url);
    } catch (MalformedURLException e) {
      // url already validated
    }
    
    this.email = email;
    this.lastStatusCode = lastStatusCode;
    this.lastContentLength = lastContentLength;
    this.lastContent = lastContent;
  }
 
  /**
   * @return an Optional containing a String describing an error, if an error was found
   *     ; an empty Optional, otherwise
   */
  private Optional<String> validateUrl(String url) {    
    if (url == null) {
      return Optional.of("No URL provided");
    }
    
    URL urlObject = null;
    
    try {
      urlObject = new URL(url);      
    } catch (MalformedURLException e) { 
      return Optional.of("Provided URL malformed");
    } 
    
    if (!urlObject.getProtocol().equalsIgnoreCase("http") 
        && !urlObject.getProtocol().equalsIgnoreCase("https")) {      
      
      return Optional.of("Provided URL's protocol not HTTP or HTTPS");
    }
    
    HttpURLConnection httpUrlConnection = null;
    
    try {
      httpUrlConnection = (HttpURLConnection) urlObject.openConnection();
      
      if (httpUrlConnection.getResponseCode() != 200) {
        return Optional.of("Request to provided URL failed");
      }
    } catch (IOException e) {
      return Optional.of("Request to provided URL failed");
    }
    
    String contentType = httpUrlConnection.getContentType();
    if (!contentType.startsWith("application/json")) {
      return Optional.of("MIME type of response from provided URL not application/json");
    }
    
    if (url.length() > 2083) {
      return Optional.of("Provided URL greater than 2083 characters");
    }   
    
    return Optional.empty();
  }  
  
  /**
   * @return an Optional containing a String describing an error, if an error was found
   *     ; an empty Optional, otherwise
   */
  private Optional<String> validateEmail(String email) {   
    if (email == null) {
      return Optional.of("No email provided");
    }      
    
    try {
      (new InternetAddress(email)).validate();
    } catch (AddressException e) {
      return Optional.of("Provided email malformed");
    }    
    
    if (email.length() > 2083) {
      return Optional.of("Provided email greater than 255 characters");
    }  
    
    return Optional.empty();
  }  
  
  public URL getUrl() {
    return url;
  }
  
  public String getEmail() {
    return email;
  }
  
  public int getLastStatusCode() {
    return lastStatusCode;
  }
  
  public long getLastContentLength() {
    return lastContentLength;
  }
  
  public String getLastContent() {
    return lastContent;
  }
  
  /**
   * Checks url for changes to its response. Sends a notification to email if a change 
   * has been detected  
   * 
   * @return true, if a change has been detected and a notification has been sent; false, otherwise
   */
  public boolean checkUrlResponseForChanges(EmailSender emailSender) throws MessagingException
      , IOException {
       
    HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();   
    
    int newStatusCode = httpUrlConnection.getResponseCode();
    
    if (newStatusCode != 200) {      
      if (lastStatusCode == 200) {
        lastStatusCode = newStatusCode;
        sendEmailNotification(emailSender);
        return true;
      }
      
      return false;
    }
        
    long newContentLength = httpUrlConnection.getContentLengthLong();     
    String newContent = getContent(httpUrlConnection.getInputStream());

    if (urlResponseHasChanged(newStatusCode, newContentLength, newContent)) {
      sendEmailNotification(emailSender);
      return true;
    }
    
    return false;
  }
  
  private void sendEmailNotification(EmailSender emailSender) throws MessagingException {
    String subject = "There has been an update to an API resource you are monitoring.";
    String content = "There has been a change to the resource at " + url + ".";        
    
    emailSender.sendEmail(email, subject, content);
  }
  
  private String getContent(InputStream inputStream) throws IOException {
    int length = 0;
    byte[] buffer = new byte[1024];
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    
    while ((length = inputStream.read(buffer)) != -1) {
      byteArrayOutputStream.write(buffer, 0, length);
    }
    
    return byteArrayOutputStream.toString();
  }
  
  /**
   * First, checks for matching status codes. Next, matching content lengths. Then, matching content  
   * 
   * @return true, if a change has been detected; false, otherwise 
   */
  public boolean urlResponseHasChanged(int newStatusCode, long newContentLength, String newContent) {    
    if (newStatusCode != lastStatusCode) {
      lastStatusCode = newStatusCode;
      lastContentLength = newContentLength;
      lastContent = newContent;
      return true;
    }
  
    if (newContentLength != lastContentLength) {
      lastContentLength = newContentLength;
      lastContent = newContent;
      return true;
    }
    
    if (!newContent.equals(lastContent)) {
      lastContent = newContent;
      return true;
    }
    
    return false;    
  }
}