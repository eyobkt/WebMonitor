import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MonitorServlet extends HttpServlet {         
  protected void doPost(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException, IOException {
    String url = request.getParameter("url");	  
    String email = request.getParameter("email");
	  
    DataAccessObject dao = new DataAccessObject();
    dao.insertMonitor(url, email);
    dao.closeConnection();
	 
    response.setStatus(201);
  }
	
  protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException, IOException {
    String url = request.getParameter("url");   
    String email = request.getParameter("email");
    
    DataAccessObject dao = new DataAccessObject();
    dao.deleteMonitor(url, email);
    dao.closeConnection();
   
    response.setStatus(204);
  }	
}