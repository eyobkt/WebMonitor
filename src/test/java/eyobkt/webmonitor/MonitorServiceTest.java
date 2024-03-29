package eyobkt.webmonitor;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import eyobkt.webmonitor.exception.PrimaryKeyConstraintViolationException;
import eyobkt.webmonitor.monitor.Monitor;
import eyobkt.webmonitor.monitor.MonitorController;
import eyobkt.webmonitor.monitor.MonitorDao;
import eyobkt.webmonitor.monitor.MonitorDaoFactory;

@RunWith(MockitoJUnitRunner.class)
public class MonitorServiceTest {  
  @Mock
  private MonitorDao monitorDao;  
  @Mock
  private MonitorDaoFactory monitorDaoFactory;  
  @Mock
  private HttpServletRequest request;  
  @Mock
  private HttpServletResponse response;  
  @Mock
  private PrintWriter printWriter;  
  @InjectMocks
  private MonitorController monitorService;
  
  @Before
  public void setUp() throws SQLException, IOException {
    when(response.getWriter()).thenReturn(printWriter);
    when(monitorDaoFactory.create()).thenReturn(monitorDao);    
  }  
  
  @Test
  public void doPost_newValidUrlAndEmail_respondsWith201() throws ServletException
      , SQLException, IOException {     
    
    when(request.getParameter("url")).thenReturn(
        "https://openlibrary.org/api/books?bibkeys=ISBN:0439800994&format=json");
    when(request.getParameter("email")).thenReturn("email@example.com");    
    
    monitorService.doPost(request, response);
    
    verify(response, times(1)).setStatus(201);
    verify(response, times(1)).setStatus(anyInt());    
    verify(response, never()).setContentType(anyString());
    verify(response, never()).getWriter();    
  } 
  
  @Test
  public void doPost_nullUrl_respondsWith400() throws ServletException
      , SQLException, IOException {     
    
    when(request.getParameter("url")).thenReturn(null);
    when(request.getParameter("email")).thenReturn("email@example.com");    
    
    monitorService.doPost(request, response);
    
    verify(response, times(1)).setStatus(400);
    verify(response, times(1)).setStatus(anyInt());    
    verify(response, times(1)).setContentType("text/plain");
    verify(response, times(1)).setContentType(anyString());
    verify(printWriter, times(1)).write("No URL provided");   
    verify(printWriter, times(1)).write(anyString());
  } 
  
  @Test
  public void doPost_malformedUrl_respondsWith400() throws ServletException
      , SQLException, IOException {     
    
    when(request.getParameter("url")).thenReturn("twitter");
    when(request.getParameter("email")).thenReturn("email@example.com");    
    
    monitorService.doPost(request, response);
    
    verify(response, times(1)).setStatus(400);
    verify(response, times(1)).setStatus(anyInt());    
    verify(response, times(1)).setContentType("text/plain");
    verify(response, times(1)).setContentType(anyString());
    verify(printWriter, times(1)).write("Provided URL malformed");   
    verify(printWriter, times(1)).write(anyString());
  } 
  
  @Test
  public void doPost_urlProtocolNotHttpOrHttps_respondsWith400() throws ServletException
      , SQLException, IOException {     
    
    when(request.getParameter("url")).thenReturn("ftp://test.rebex.net");
    when(request.getParameter("email")).thenReturn("email@example.com");    
    
    monitorService.doPost(request, response);
    
    verify(response, times(1)).setStatus(400);
    verify(response, times(1)).setStatus(anyInt());    
    verify(response, times(1)).setContentType("text/plain");
    verify(response, times(1)).setContentType(anyString());
    verify(printWriter, times(1)).write("Provided URL's protocol not HTTP or HTTPS");   
    verify(printWriter, times(1)).write(anyString());
  } 
  
  @Test
  public void doPost_duplicateRequest_respondsWith409() throws ServletException
      , SQLException, IOException {      
    
    when(request.getParameter("url")).thenReturn(
        "https://openlibrary.org/api/books?bibkeys=ISBN:0439800994&format=json");
    when(request.getParameter("email")).thenReturn("email@example.com");      
    doThrow(PrimaryKeyConstraintViolationException.class).when(monitorDao)
        .insertMonitor(any(Monitor.class));
    
    monitorService.doPost(request, response);
    
    verify(response, times(1)).setStatus(409);
    verify(response, times(1)).setStatus(anyInt());    
    verify(response, times(1)).setContentType("text/plain");
    verify(response, times(1)).setContentType(anyString());
    verify(printWriter, times(1)).write("Resource already exists");   
    verify(printWriter, times(1)).write(anyString());
  } 
  
  @Test
  public void doDelete_urlAndEmailExistInDb_respondsWith204() throws ServletException
      , SQLException, IOException {     
    
    String url = "https://openlibrary.org/api/books?bibkeys=ISBN:0439800994&format=json";
    String email = "email@example.com";
    when(request.getParameter("url")).thenReturn(url);
    when(request.getParameter("email")).thenReturn(email);  
    when(monitorDao.deleteMonitor(url, email)).thenReturn(1);
    
    monitorService.doDelete(request, response);
    
    verify(response, times(1)).setStatus(204);
    verify(response, times(1)).setStatus(anyInt());    
    verify(response, never()).setContentType(anyString());
    verify(response, never()).getWriter();    
  } 
  
  @Test
  public void doDelete_urlAndEmailAbsentFromDb_respondsWith404() throws ServletException
      , SQLException, IOException {     
    
    String url = "https://openlibrary.org/api/books?bibkeys=ISBN:0439800994&format=json";
    String email = "email@example.com";
    when(request.getParameter("url")).thenReturn(url);
    when(request.getParameter("email")).thenReturn(email);  
    when(monitorDao.deleteMonitor(url, email)).thenReturn(0);
    
    monitorService.doDelete(request, response);
    
    verify(response, times(1)).setStatus(404);
    verify(response, times(1)).setStatus(anyInt());    
    verify(response, never()).setContentType(anyString());
    verify(response, never()).getWriter();    
  } 
  
  @Test
  public void doDelete_nullEmail_respondsWith400() throws ServletException
      , SQLException, IOException {     
    
    when(request.getParameter("url")).thenReturn(
        "https://openlibrary.org/api/books?bibkeys=ISBN:0439800994&format=json");
    when(request.getParameter("email")).thenReturn(null);    
    
    monitorService.doDelete(request, response);
    
    verify(response, times(1)).setStatus(400);
    verify(response, times(1)).setStatus(anyInt());    
    verify(response, times(1)).setContentType("text/plain");
    verify(response, times(1)).setContentType(anyString());
    verify(printWriter, times(1)).write("No email provided");   
    verify(printWriter, times(1)).write(anyString());
  } 
}