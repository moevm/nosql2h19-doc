package nosql.docdb.web_application;

import com.vaadin.server.VaadinServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ServletServer {
    public static void startServer(){
        Server server = new Server(8181);

        ServletContextHandler vaadinHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        vaadinHandler.setContextPath("/application");
        ServletHolder vaadinPages = new ServletHolder(new VaadinServlet());
        vaadinHandler.addServlet(vaadinPages, "/*");
        vaadinHandler.setInitParameter("ui", SimplePage.class.getCanonicalName());

        ServletContextHandler resourceContextHandler=new ServletContextHandler(ServletContextHandler.SESSIONS);
        resourceContextHandler.setContextPath("/static");
        ServletHolder resourceHolder=new ServletHolder(new DefaultServlet());
        resourceContextHandler.addServlet(resourceHolder,"/*");

        HandlerList handlers=new HandlerList();
        handlers.setHandlers(new Handler[]{vaadinHandler,resourceContextHandler});
        server.setHandler(handlers);


        try {
            server.start();
            server.join();

        } catch (Exception ex) {
            Logger.getLogger(ServletServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
