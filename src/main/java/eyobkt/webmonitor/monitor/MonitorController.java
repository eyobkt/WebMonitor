package eyobkt.webmonitor.monitor;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eyobkt.webmonitor.exception.PrimaryKeyConstraintViolationException;

/**
 * Handles all web service requests
 */

public class MonitorController extends HttpServlet {  
  
  private static final long serialVersionUID = 1L;
  
  private MonitorDaoFactory monitorDaoFactory;
  
  @Autowired
  public MonitorController(MonitorDaoFactory monitorDaoFactory) {
    if (monitorDaoFactory == null) {
      throw new IllegalArgumentException();      
    }
    
    this.monitorDaoFactory = monitorDaoFactory;
  }
  
  /**
   * Creates a Monitor with the values of the request parameters as arguments and stores it in the 
   * data store
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException {
    
    String url = request.getParameter("url");
    String email = request.getParameter("email");       
    Monitor monitor = null;
    
    try {
      monitor = new Monitor(url, email);
    } catch (IllegalArgumentException e) {
      setResponse(response, 400, e.getMessage());
      
      return;  
    } catch (InterruptedException e) {
      response.setStatus(500);
      
      e.printStackTrace();
      
      return;
    }

    try (MonitorDao monitorDao = monitorDaoFactory.create()) {      
      monitorDao.insertMonitor(monitor);
    } catch (SQLException e) {      
      if (e instanceof PrimaryKeyConstraintViolationException) {
        setResponse(response, 409, "Resource already exists");      
      } else {
        response.setStatus(500);
        
        e.printStackTrace();
      }           
      
      return;
    } 
	 
    response.setStatus(201);
  }
  
  /**
   * Deletes a Monitor from the data store with url and email that match the values of the 
   * request parameters  
   */
  public void doDelete(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException {
    
    String url = request.getParameter("url");   
    String email = request.getParameter("email");
    
    if (url == null) {
      setResponse(response, 400, "No URL provided");
      
      return;  
    }
    
    if (email == null) {
      setResponse(response, 400, "No email provided");
      
      return;  
    }
    
    try (MonitorDao monitorDao = monitorDaoFactory.create()) {    
      int numRowsDeleted = monitorDao.deleteMonitor(url, email);     
      
      if (numRowsDeleted == 0) {
        response.setStatus(404);
        
        return;
      }      
    } catch (SQLException e) {      
      response.setStatus(500);
      
      e.printStackTrace();
      
      return;
    } 
    
    response.setStatus(204);
  }	
  
  private void setResponse(HttpServletResponse httpServletResponse, int statusCode
      , String body) {   
    
    httpServletResponse.setStatus(statusCode);      
    
    try {
      httpServletResponse.getWriter().write(body);
    } catch (IOException e) {
      e.printStackTrace();
      
      return;
    }  
    
    httpServletResponse.setContentType("text/plain");
  }
}