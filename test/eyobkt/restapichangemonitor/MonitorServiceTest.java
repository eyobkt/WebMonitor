package eyobkt.restapichangemonitor;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
  
  @Before
  public void setUp() throws SQLException {
    when(monitorDaoFactory.createMonitorDao()).thenReturn(monitorDao); 
  }  
  
  @Test
  public void doPostWhenGivenNewValidUrlAndEmailRespondsWith201() throws ServletException
      , SQLException, IOException {     
    
    String url = "https://openlibrary.org/api/books?bibkeys=ISBN:0439800994&format=json"; 
    when(request.getParameter("url")).thenReturn(url);
    when(request.getParameter("email")).thenReturn("address@email.com");    
    
    (new MonitorService(monitorDaoFactory)).doPost(request, response);
    
    verify(response, times(1)).setStatus(201);
    verify(response, times(1)).setStatus(anyInt());    
    verify(response, never()).setContentType(anyString());
    verify(response, never()).getWriter();    
  } 
}