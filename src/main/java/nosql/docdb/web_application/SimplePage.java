package nosql.docdb.web_application;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.*;

import java.util.Date;

@Title("Light Vaadin")
@Theme("valo")
public class SimplePage extends UI {
    protected void init(VaadinRequest request) {
        VerticalLayout layout = new VerticalLayout();
        setContent(layout);
        layout.addComponent(new Label("Hello world"));
        layout.addComponent(new Button("Click me", event -> {
            layout.addComponent(new Label("CLICKED"));
            Notification.show("Hello at " + new Date());
        }));
    }
}
