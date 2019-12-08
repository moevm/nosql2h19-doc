package nosql.docdb.web_application.statistic_window;

import com.vaadin.ui.*;
import nosql.docdb.database.MongoDB;
import nosql.docdb.doc_parser.object_model.DbDocument;
import nosql.docdb.web_application.views.HighChartFrame;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BarStatisticTab extends VerticalLayout implements Refreshable {
    private final HighChartFrame htmlFrame;
    private final Button buildButton;
    private final TextField searchTextField;

    public BarStatisticTab(Function<String, List<Pair<String,Integer>>> loader) {
        setSizeFull();
        htmlFrame = new HighChartFrame("/static/highcharts.html");
        searchTextField = new TextField();
        searchTextField.setStyleName("i-hPadding3 small i-small");
        buildButton = new Button("Построить");
        buildButton.setStyleName("i-hPadding3 small i-small");
        buildButton.addClickListener(e -> {
            List<PlotData> data =loader.apply(searchTextField.getValue())
                    .stream()
                    .map(p->new PlotData(p.getKey(),Arrays.asList(p.getValue())))
                    .collect(Collectors.toList());
            PlotCaptions analisisData = new PlotCaptions("Документ", "Частота встречаемости", "Популярность слова \""+searchTextField.getValue()+"\"");
            htmlFrame.callJS("buildBarChart", data, analisisData);
        });

        addComponents(new HorizontalLayout(buildButton, searchTextField) {{
        }}, htmlFrame);
        setExpandRatio(htmlFrame, 1);
    }

    @Override
    public void onRefresh() {
        buildButton.click();
    }
}
