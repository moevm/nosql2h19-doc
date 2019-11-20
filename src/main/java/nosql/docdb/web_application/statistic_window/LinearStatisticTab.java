package nosql.docdb.web_application.statistic_window;

import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import nosql.docdb.database.MongoDB;
import nosql.docdb.web_application.views.HighChartFrame;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LinearStatisticTab extends VerticalLayout implements Refreshable {
    private final HighChartFrame htmlFrame;
    private final Button buildButton;

    LinearStatisticTab(Function<MongoDB.Query, List<Pair<Integer,Double>>> loader , MongoDB.Query query, PlotCaptions plotCaptions) {

        setSizeFull();
        htmlFrame = new HighChartFrame("/static/highcharts.html");
        buildButton = new Button("Построить");
        buildButton.setStyleName("i-hPadding3 small i-small");
        buildButton.addClickListener(e -> {
            List<Pair<Integer,Double>> base=loader.apply(MongoDB.Query.builder().build());
            List<Pair<Integer,Double>> selected=loader.apply(query);

            htmlFrame.callJS(
                    "buildLinearChart",
                    new PlotData(
                            "Все документы",
                            base.stream().map(Pair::getKey).collect(Collectors.toList()),
                            base.stream().map(Pair::getValue).collect(Collectors.toList())
                    ),
                    new PlotData(
                            "Выбранные документы",
                            selected.stream().map(Pair::getKey).collect(Collectors.toList()),
                            selected.stream().map(Pair::getValue).collect(Collectors.toList())
                    ),
                    plotCaptions
            );
        });
        addComponents(buildButton, htmlFrame);
        setExpandRatio(htmlFrame, 1);
    }

    @Override
    public void onRefresh() {
        buildButton.click();
    }
}
