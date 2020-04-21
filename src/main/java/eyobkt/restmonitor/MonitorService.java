package eyobkt.restmonitor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.Optional;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MonitorService extends HttpServlet {  
  
  private static final long serialVersionUID = 1L;
  
  private MonitorDaoFactory monitorDaoFactory;
  
  MonitorService(MonitorDaoFactory monitorDaoFactory) {
    if (monitorDaoFactory == null) {
      throw new IllegalArgumentException();      
    }
    
    this.monitorDaoFactory = monitorDaoFactory;
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException {
    
    String url = request.getParameter("url");
    String email = request.getParameter("email");    
    
    Optional<String> errorMessage = checkParams(url, email);
    
    if (errorMessage.isPresent()) {    
      setResponse(response, 400, errorMessage.get());
      return;  
    }
    
    errorMessage = validateUrl(url);    
    if (errorMessage.isPresent()) {    
      setResponse(response, 400, errorMessage.get());
      return;  
    }
    
    errorMessage = validateEmail(email);
    if (errorMessage.isPresent()) {    
      setResponse(response, 400, errorMessage.get());
      return;  
    }
       
    URLConnection uc = null;    
    try {
      uc = (new URL(url)).openConnection(); 
    } catch (IOException e) {
      response.setStatus(500);
      e.printStackTrace();
      return;
    }    
    long contentLength = uc.getContentLengthLong();     
    
    MonitorDao md = null;
    try {
      md = monitorDaoFactory.createMonitorDao();       
      md.insertMonitor(url, email, contentLength);
    } catch (SQLException e) {      
      if (e instanceof PrimaryKeyConstraintViolationException) {
        setResponse(response, 409, "Resource already exists");      
      } else {
        response.setStatus(500);
        e.printStackTrace();
      }                  
      return;
    } finally {
      if (md != null) {
        md.closeConnection();
      }      
    }
	 
    response.setStatus(201);
  }
  
  public void doDelete(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException {
    
    String url = request.getParameter("url");   
    String email = request.getParameter("email");
    
    Optional<String> errorMessage = checkParams(url, email);    
    if (errorMessage.isPresent()) {    
      setResponse(response, 400, errorMessage.get());
      return;  
    }
    
    MonitorDao md = null;
    try {
      md = monitorDaoFactory.createMonitorDao();       
      int numRowsDeleted = md.deleteMonitor(url, email);      
      if (numRowsDeleted == 0) {
        response.setStatus(404);
        return;
      }      
    } catch (SQLException e) {      
      response.setStatus(500);
      e.printStackTrace();
      return;
    } finally {
      if (md != null) {
        md.closeConnection();
      }      
    }
 
    response.setStatus(204);
  }	
  
  private Optional<String> checkParams(String url, String email) {       
    if (url == null) {
      return Optional.of("No URL provided");
    }
    
    if (email == null) {
      return Optional.of("No email provided");
    }
    
    return Optional.empty();
  }
  
  private void setResponse(HttpServletResponse response, int status, String message) {   
    response.setStatus(status);      
    
    try {
      response.getWriter().write(message);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }  
    
    response.setContentType("text/plain");
  }
  
  private Optional<String> validateUrl(String url) {    
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
    
    HttpURLConnection huc = null;
    
    try {
      huc = (HttpURLConnection) urlObject.openConnection();
      
      if (huc.getResponseCode() != 200) {
        return Optional.of("Request to provided URL failed");
      }
    } catch (IOException e) {
      return Optional.of("Request to provided URL failed");
    }
    
    String contentType = huc.getContentType();
    if (!contentType.startsWith("application/json")) {
      return Optional.of("Provided URL's response type not JSON");
    }
    
    if (url.length() > 2083) {
      return Optional.of("Provided URL greater than 2083 characters");
    }   
    
    return Optional.empty();
  }  
  
  private Optional<String> validateEmail(String email) {    
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
}