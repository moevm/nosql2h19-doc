package nosql.docdb.web_application.statistic_window;

import com.google.gson.Gson;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.*;
import nosql.docdb.doc_parser.object_model.DbDocument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatisticWindowNew extends Window {
    public StatisticWindowNew(DbDocument record) {
        super("Статистика по документу \"" + record.getName() + "\""); // Set window caption

        //---
        PlotData allDta = new PlotData("Все документы", new ArrayList<>(), new ArrayList<>());
        PlotData curData = new PlotData(record.getName(), new ArrayList<>(), new ArrayList<>());
        allDta.ys = new ArrayList<>(Arrays.asList(30, 40, 0, 50));
        curData.ys = new ArrayList<>(Arrays.asList(25));
        allDta.xs = new ArrayList<>(Arrays.asList(130, 430, 160, 250));
        curData.xs = new ArrayList<>(Arrays.asList(400));

        PlotData allDta1 = new PlotData("Все документы", new ArrayList<>(), new ArrayList<>());
        PlotData curData1 = new PlotData(record.getName(), new ArrayList<>(), new ArrayList<>());
        allDta1.ys = new ArrayList<>(Arrays.asList(3, 5, 0, 14));
        curData1.ys = new ArrayList<>(Arrays.asList(25));
        allDta1.xs = new ArrayList<>(Arrays.asList(130, 430, 160, 250));
        curData1.xs = new ArrayList<>(Arrays.asList(400));
        //---


        center();
        setClosable(true);
        setModal(false);
        setWidth("500px");
        setHeight("500px");
        AnalisisData analisisImageData = new AnalisisData("Количество страниц", "Количество картинок", "Зависимость количества картинок от количества страниц");
        AnalisisData analisisTablesData = new AnalisisData("Количество страниц", "Количество таблиц", "Зависимость количества таблиц от количества страниц");
        setContent(new TabSheet() {{
            addTab(new LinearStatisticTab(record, allDta, curData, analisisImageData), "Cтатистика по количеству картинок");
            addTab(new LinearStatisticTab(record, allDta1, curData1, analisisTablesData), "Cтатистика по количеству таблиц");
            addTab(new BarStatisticTab(record), "Частота встречаемости слова");
            addSelectedTabChangeListener(e -> {
                Component selectedTab = e.getTabSheet().getSelectedTab();
                if (selectedTab instanceof Refreshable) ((Refreshable) selectedTab).onRefresh();
            });
            setSizeFull();
            if (getSelectedTab() instanceof Refreshable) ((Refreshable) getSelectedTab()).onRefresh();
        }});
    }

}


class HighChartFrame extends BrowserFrame {
    HighChartFrame(String htmlUrl) {
        setSizeFull();
        setSource(new ExternalResource(htmlUrl));
        setId(UUID.randomUUID().toString());
    }

    void callJS(String functionName, Object... args) {
        String params = Stream.of(args)
                .map(arg -> new Gson().toJson(arg))
                .collect(Collectors.joining(","));
        String js = "((document.getElementById('" + getId() + "').childNodes[0].contentWindow)." + functionName + "(" + params + "))";

        JavaScript.getCurrent()
                .execute("setTimeout(()=>" + js + ",200)");
    }

}

interface Refreshable extends Component {
    void onRefresh();
}

class LinearStatisticTab extends VerticalLayout implements Refreshable {
    private final HighChartFrame htmlFrame;
    private final Button buildButton;

    LinearStatisticTab(DbDocument record, /**/PlotData allDta, PlotData curData /**/, AnalisisData analisisData) {
        System.out.println("INIT");
        setSizeFull();
        htmlFrame = new HighChartFrame("/static/highcharts.html");
        buildButton = new Button("Построить");
        buildButton.setStyleName("i-hPadding3 small i-small");
        buildButton.addClickListener(e -> {
            System.out.println("click ");
            htmlFrame.callJS("buildLinearChart", allDta, curData, analisisData);
        });
        addComponents(buildButton, htmlFrame);
        setExpandRatio(htmlFrame, 1);
    }

    @Override
    public void onRefresh() {
        buildButton.click();
    }
}

class BarStatisticTab extends VerticalLayout implements Refreshable {
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
            AnalisisData analisisData = new AnalisisData("Документ", "Частота встречаемости", "Популярность слова алгоритм");
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