package nosql.docdb.web_application.views;

import com.google.gson.Gson;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.JavaScript;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HighChartFrame extends BrowserFrame {
    public HighChartFrame(String htmlUrl) {
        setSizeFull();
        setSource(new ExternalResource(htmlUrl));
        setId(UUID.randomUUID().toString());
    }

    public void callJS(String functionName, Object... args) {
        String params = Stream.of(args)
                .map(arg -> new Gson().toJson(arg))
                .collect(Collectors.joining(","));
        String js = "((document.getElementById('" + getId() + "').childNodes[0].contentWindow)." + functionName + "(" + params + "))";

        JavaScript.getCurrent()
                .execute("setTimeout(()=>" + js + ",200)");
    }

}
