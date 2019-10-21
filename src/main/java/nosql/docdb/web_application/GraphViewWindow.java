package nosql.docdb.web_application;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;


public class GraphViewWindow extends Window {
    private final BrowserFrame htmlFrame;

    public GraphViewWindow(){
        setCaption("График");
        setWidth("50%");
        setHeight("50%");
        center();
        UI.getCurrent().addWindow(this);


        htmlFrame=new BrowserFrame(){{
            setSizeFull();
            setSource(new ExternalResource("/static/highcharts.html"));
        }};

        setContent(htmlFrame);
    }
}
