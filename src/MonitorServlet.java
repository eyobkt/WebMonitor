import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;

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
    
    String result = checkParams(url, email);
    
    if (!result.equals("OK")) {
      setResponse(response, 400, result);
      return;
    }
    
    result = validateUrl(url);
    
    if (!result.equals("OK")) {
      setResponse(response, 400, result);
      return;
    }
    
    result = validateEmail(email);    
    if (!result.equals("OK")) {
      setResponse(response, 400, result);
      return;
    }	  
       
    URLConnection uc = null;    
    try {
      uc = (new URL(url)).openConnection(); 
    } catch (IOException e) {
      response.setStatus(500);
      return;
    }    
    long contentLength = uc.getContentLengthLong();     
    
    try {
      MonitorDao md = new MonitorDaoImpl(); 
      
      if (md.containsMonitor(url, email)) {
        setResponse(response, 409, "Resource already exists");
        return;
      }
      
      md.insertMonitor(url, email, contentLength);
      md.closeConnection();
    } catch (SQLException e) {
      response.setStatus(500);
      return;
    }   
	 
    response.setStatus(201);
  }
  
  protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException, IOException {
    String url = request.getParameter("url");   
    String email = request.getParameter("email");
    
    MonitorDaoImpl dao = new MonitorDaoImpl();
    dao.deleteMonitor(url, email);
    dao.closeConnection();
   
    response.setStatus(204);
  }	
  
  private static String checkParams(String url, String email) {
    if (url == null && email == null) {         
      return "No URL or email provided";
    } else if (url == null) {
      return "No URL provided";
    } else if (email == null) {
      return "No email provided";
    } 
    return "OK";
  }
  
  private static void setResponse(HttpServletResponse response, int status, String message) {   
    try {
      response.getWriter().write(message);
    } catch (IOException e) {
      response.setStatus(500);
      return;
    }
    
    response.setStatus(status);
    response.setContentType("text/plain");    
  }
  
  private static String validateUrl(String url) {
    URL urlObject = null;
    
    try {
      urlObject = new URL(url);      
    } catch (MalformedURLException e) {      
      return "Provided URL malformed";
    } 
    
    if (!urlObject.getProtocol().equalsIgnoreCase("http") 
        && !urlObject.getProtocol().equalsIgnoreCase("https")) {      
      
      return "Provided URL's protocol not HTTP or HTTPS";
    }
    
    HttpURLConnection huc = null;
    
    try {
      huc = (HttpURLConnection) urlObject.openConnection();
      
      if (huc.getResponseCode() != 200) {
        return "Request to provided URL failed";
      }
    } catch (IOException e) {
      return "Request to provided URL failed";
    }
    
    String contentType = huc.getContentType();
    if (!contentType.startsWith("application/json")) {
      return "Provided URL's response type not JSON";
    }
    
    if (url.length() > 2083) {
      return "Provided URL greater than 2083 characters";
    }   
    
    return "OK";
  }
  
  private static String validateEmail(String email) {    
    try {
      (new InternetAddress(email)).validate();
    } catch (AddressException e) {
      return "Provided email malformed";
    }    
    
    if (email.length() > 2083) {
      return "Provided email greater than 255 characters";
    }  
    
    return "OK";
  }  
}