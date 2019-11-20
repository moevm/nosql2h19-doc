package nosql.docdb.web_application.statistic_window;

import com.vaadin.ui.*;
import nosql.docdb.doc_parser.object_model.DbDocument;
import nosql.docdb.web_application.views.HighChartFrame;

import java.util.ArrayList;
import java.util.Arrays;

public class BarStatisticTab extends VerticalLayout implements Refreshable {
    private final HighChartFrame htmlFrame;
    private final Button buildButton;
    private final CheckBox separatedTextsCheckBox;
    private final TextField searchTextField;

    BarStatisticTab(DbDocument record) {
        System.out.println("INIT");
        setSizeFull();
        htmlFrame = new HighChartFrame("/static/highcharts.html");
        separatedTextsCheckBox = new CheckBox("По всем документам");
        separatedTextsCheckBox.setStyleName("i-hPadding3 small i-small");
        searchTextField = new TextField();
        searchTextField.setStyleName("i-hPadding3 small i-small");
        buildButton = new Button("Построить");
        buildButton.setStyleName("i-hPadding3 small i-small");
        buildButton.addClickListener(e -> {
            //---
            PlotData allDta = new PlotData("Все документы", new ArrayList<>(), new ArrayList<>());
            PlotData curData = new PlotData(record.getName(), new ArrayList<>(), new ArrayList<>());
            allDta.ys = new ArrayList<>(Arrays.asList(3));
            curData.ys = new ArrayList<>(Arrays.asList(8));
            PlotData plot1 = new PlotData("Уголовный кодекс", new ArrayList<>(Arrays.asList(5)));
            PlotData plot2 = new PlotData("Энциклопедия обо всем", new ArrayList<>(Arrays.asList(8)));
            PlotData plot3 = new PlotData("ТОЭ. Методическое пособие", new ArrayList<>(Arrays.asList(1)));
            PlotData plot4 = new PlotData("Криптография. Лабораторный практикум", new ArrayList<>(Arrays.asList(2)));
            ArrayList<PlotData> data = new ArrayList<>();
            if (!separatedTextsCheckBox.getValue()) data.addAll(Arrays.asList(allDta, curData));
            else data.addAll(Arrays.asList(plot1, plot2, plot3, plot4));
            //--
            PlotCaptions analisisData = new PlotCaptions("Документ", "Частота встречаемости", "Популярность слова алгоритм");
            htmlFrame.callJS("buildBarChart", data, analisisData);
        });

        addComponents(new HorizontalLayout(buildButton, searchTextField, separatedTextsCheckBox) {{
            setComponentAlignment(separatedTextsCheckBox, Alignment.MIDDLE_LEFT);
        }}, htmlFrame);
        setExpandRatio(htmlFrame, 1);
    }

    @Override
    public void onRefresh() {
        buildButton.click();
    }
}
