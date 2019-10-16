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
        setContent(new MainView());
    }
}
