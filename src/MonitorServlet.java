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

public class MonitorServlet extends HttpServlet {        
  
  protected void doPost(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException {
    
    String url = request.getParameter("url");
    String email = request.getParameter("email");    
    
    checkParams(url, email).ifPresent((errorMessage) -> {
      setResponse(response, 400, errorMessage);
      return;  
    });
    
    validateUrl(url).ifPresent((errorMessage) -> {
      setResponse(response, 400, errorMessage);
      return;      
    });
    
    validateEmail(email).ifPresent((errorMessage) -> {
      setResponse(response, 400, errorMessage);
      return;  
    });    
       
    URLConnection uc = null;    
    try {
      uc = (new URL(url)).openConnection(); 
    } catch (IOException e) {
      response.setStatus(500);
      return;
    }    
    long contentLength = uc.getContentLengthLong();     
    
    MonitorDao md = null;
    try {
      md = new MonitorDaoImpl(); 
      
      if (md.containsMonitor(url, email)) {
        setResponse(response, 409, "Resource already exists");
        return;
      }
      
      md.insertMonitor(url, email, contentLength);
    } catch (SQLException e) {
      response.setStatus(500);
      return;
    } finally {
      if (md != null) {
        md.closeConnection();
      }      
    }
	 
    response.setStatus(201);
  }
  
  protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException, IOException {
    String url = request.getParameter("url");   
    String email = request.getParameter("email");
    
    checkParams(url, email).ifPresent((errorMessage) -> {
      setResponse(response, 400, errorMessage);
      return;  
    });
    
    MonitorDao md = null;
    try {
      md = new MonitorDaoImpl(); 
      
      if (!md.containsMonitor(url, email)) {
        response.setStatus(404);
        return;
      }
      
      md.deleteMonitor(url, email);
    } catch (SQLException e) {
      response.setStatus(500);
      return;
    } finally {
      if (md != null) {
        md.closeConnection();
      }      
    }
 
    response.setStatus(204);
  }	
  
  public static Optional<String> checkParams(String url, String email) {       
    if (url == null) {
      return Optional.of("No URL provided");
    }
    
    if (email == null) {
      return Optional.of("No email provided");
    }
    
    return Optional.empty();
  }
  
  public static void setResponse(HttpServletResponse response, int status, String message) {   
    try {
      response.getWriter().write(message);
    } catch (IOException e) {
      response.setStatus(status);
      return;
    }
    
    response.setStatus(status);
    response.setContentType("text/plain");    
  }
  
  public static Optional<String> validateUrl(String url) {
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
  
  public static Optional<String> validateEmail(String email) {    
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