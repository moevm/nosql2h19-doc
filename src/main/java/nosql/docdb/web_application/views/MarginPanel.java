package nosql.docdb.web_application.views;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Component;

public class MarginPanel extends AbsoluteLayout {
    public MarginPanel(Component source){
        String cssRule = "top: 30px; left: 20px; right: 0px; bottom: 0px";
        System.out.println(cssRule);
        addComponent(source,cssRule);
    }
}
