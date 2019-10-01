package nosql.docdb.web_application;

import com.vaadin.server.VaadinServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ServletServer {
    public static void startServer(){
        Server server = new Server(8181);

        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");

        ServletHolder sh = new ServletHolder(new VaadinServlet());
        contextHandler.addServlet(sh, "/*");
        contextHandler.setInitParameter("ui", SimplePage.class.getCanonicalName());

        server.setHandler(contextHandler);

        try {
            server.start();
            server.join();

        } catch (Exception ex) {
            Logger.getLogger(ServletServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
