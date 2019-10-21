package nosql.docdb.web_application;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Window;


public class GraphViewWindow extends Window {
    private final BrowserFrame htmlFrame=new BrowserFrame();

    public GraphViewWindow(){
        setCaption("График");
        setWidth("50%");
        setHeight("50%");
        center();

        //htmlFrame.setSource();

        setContent(htmlFrame);
    }
}
