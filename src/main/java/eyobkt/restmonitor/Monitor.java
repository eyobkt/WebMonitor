package eyobkt.restmonitor;

import eyobkt.restmonitor.emailsender.EmailSender;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class Monitor {
  
  /**
   * The URL of the page that is checked
   */
  private String url;  
  
  /**
   * The email address to which notifications are sent
   */
  private String email;  
  
  /**
   * The text on the page the last time it was checked
   */
  private String lastContent;
  
  private static ChromeOptions chromeOptions = new ChromeOptions();  
  
  static {
    WebDriverManager.chromedriver().setup();
    
    chromeOptions.addArguments("--window-size=1920,1080");  
  }  
  
  /**
   * The constructor to be used when a Monitor is first created. Validates inputs
   */
  Monitor(String url, String email) throws IllegalArgumentException, InterruptedException {    
    Optional<String> errorMessage = validateUrl(url);
    
    if (errorMessage.isPresent()) {    
      throw new IllegalArgumentException(errorMessage.get());
    }    
    
    errorMessage = validateEmail(email);
    if (errorMessage.isPresent()) {    
      throw new IllegalArgumentException(errorMessage.get());
    }    
    
    this.url = url;
    this.email = email;
    
    try {
      this.lastContent = getContent(url);
    } catch (InterruptedException e) {
      e.printStackTrace();
      
      throw e;
    }    
  }
  
  /**
   * The constructor to be used when a Monitor is reloaded from the data store
   */
  Monitor(String url, String email, String lastContent) {    
    this.url = url;
    this.email = email;
    this.lastContent = lastContent;
  }
 
  /**
   * @return an Optional containing a String describing an error, if an error was found; an empty 
   *     Optional, otherwise
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
  
    if (url.length() > 2083) {
      return Optional.of("Provided URL greater than 2083 characters");
    }   
    
    return Optional.empty();
  }  
  
  /**
   * @return an Optional containing a String describing an error, if an error was found; an empty 
   *     Optional, otherwise
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
  
  public String getUrl() {
    return url;
  }
  
  public String getEmail() {
    return email;
  }
  
  public String getLastContent() {
    return lastContent;
  }
  
  /**
   * @return true, if a change has been detected and a notification has been sent; false, otherwise
   */
  public boolean checkPageForChanges(EmailSender emailSender) throws MessagingException
      , InterruptedException {
    
    String newContent = getContent(url);
    
    if (!newContent.equals(lastContent)) {
      lastContent = newContent;
      
      sendEmailNotification(emailSender);
      
      return true;
    }
    
    return false;
  }
  
  private String getContent(String url) throws InterruptedException {
    WebDriver webDriver = new ChromeDriver(chromeOptions);
    
    webDriver.get(url);
    
    try {
      // Wait for page to fully load
      Thread.sleep(10000);
      
      return webDriver.findElement(By.tagName("body")).getText();
    } catch (InterruptedException e) {
      e.printStackTrace();
      
      throw e;
    } finally {
      webDriver.quit();
    }
  }
  
  private void sendEmailNotification(EmailSender emailSender) throws MessagingException {
    String subject = "A web page that you are monitoring has been updated";
    String content = "There has been a change at " + url + ".";        
    
    emailSender.sendEmail(email, subject, content);
  }
}