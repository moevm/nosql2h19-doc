package nosql.docdb.web_application.statistic_window;

import com.vaadin.ui.*;
import nosql.docdb.database.MongoDB;
import nosql.docdb.doc_parser.object_model.DbDocument;
import nosql.docdb.web_application.views.HighChartFrame;

import java.util.ArrayList;
import java.util.Arrays;

public class StatisticWindowNew extends Window {
    private static final PlotCaptions imageChartCaptions = new PlotCaptions(
            "Количество страниц",
            "Количество картинок",
            "Зависимость количества картинок от количества страниц"
    );

    private static final PlotCaptions tablesChartCaptions = new PlotCaptions(
            "Количество страниц",
            "Количество таблиц",
            "Зависимость количества таблиц от количества страниц"
    );

    public StatisticWindowNew(MongoDB mongoDB, MongoDB.Query query) {
        super("Статистика по документам");

        //---
        PlotData allDta = new PlotData("Все документы", new ArrayList<>(), new ArrayList<>());
        PlotData curData = new PlotData("Выбранные документы", new ArrayList<>(), new ArrayList<>());
        allDta.ys = new ArrayList<>(Arrays.asList(30, 40, 0, 50));
        curData.ys = new ArrayList<>(Arrays.asList(25));
        allDta.xs = new ArrayList<>(Arrays.asList(130, 430, 160, 250));
        curData.xs = new ArrayList<>(Arrays.asList(400));

        center();
        setClosable(true);
        setModal(false);
        setWidth("500px");
        setHeight("500px");

        setContent(new TabSheet() {{
            addTab(new LinearStatisticTab(mongoDB::getImagesAndPages, query, imageChartCaptions), "Cтатистика по количеству картинок");
            addTab(new LinearStatisticTab(mongoDB::getTablesAndPages, query, tablesChartCaptions), "Cтатистика по количеству таблиц");

            addTab(new BarStatisticTab(null), "Частота встречаемости слова");
            addSelectedTabChangeListener(e -> {
                Component selectedTab = e.getTabSheet().getSelectedTab();
                if (selectedTab instanceof Refreshable) ((Refreshable) selectedTab).onRefresh();
            });
            setSizeFull();
            if (getSelectedTab() instanceof Refreshable) ((Refreshable) getSelectedTab()).onRefresh();
        }});
    }

}


