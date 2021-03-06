package nosql.docdb.web_application;

import com.vaadin.server.VaadinServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServletServer {
    public static void startServer() throws IOException, URISyntaxException {
        Server server = new Server(8181);

        ServletContextHandler vaadinHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        vaadinHandler.setContextPath("/application");
        ServletHolder vaadinPages = new ServletHolder(new VaadinServlet());
        vaadinHandler.addServlet(vaadinPages, "/*");
        vaadinHandler.setInitParameter("ui", SimplePage.class.getCanonicalName());

        ServletContextHandler resourceContextHandler=new ServletContextHandler(ServletContextHandler.SESSIONS);
        resourceContextHandler.setContextPath("/static");

        URI staticResourceFolder=URI.create("file:"+getWorkingDirectory()+"/static/");
        System.out.println("static resource folder: "+staticResourceFolder);
        resourceContextHandler.setBaseResource(Resource.newResource(staticResourceFolder));

        resourceContextHandler.addServlet(DefaultServlet.class,"/");

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

    private static String getWorkingDirectory() throws URISyntaxException {
        return new File(ServletServer.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getAbsolutePath();
    }
}
