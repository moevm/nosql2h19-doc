package nosql.docdb.web_application;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.ui.*;


@Title("Light Vaadin")
@Theme("valo")
@Push(value = PushMode.AUTOMATIC,transport = Transport.LONG_POLLING)
public class SimplePage extends UI {
    protected void init(VaadinRequest request) {
        setContent(new MainView());
    }
}
