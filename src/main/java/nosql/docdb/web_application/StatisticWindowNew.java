package nosql.docdb.web_application;

import com.google.gson.Gson;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class StatisticWindowNew extends Window {
        public StatisticWindowNew(MainView.FileFullInfo record) {
                super("Статистика по документу \"" + record.getName() + "\""); // Set window caption
                center();
                setClosable(true);
                setModal(false);
                setWidth("500px");
                setHeight("500px");
                setContent(new TabSheet(){{
                        addTab(new StatisticTab(record),"Cтатистика по количеству картинок");
                        addTab(new StatisticTab(record),"Cтатистика по количеству таблиц");
                        addTab(new VerticalLayout(),"third");

                        setSizeFull();
                }});
        }

}
class StatisticTab extends VerticalLayout{
        private  final  BrowserFrame htmlFrame;
        private final Button buildButton;
        StatisticTab(MainView.FileFullInfo record) {
                setSizeFull();
                htmlFrame = new BrowserFrame() {{
                        setSizeFull();
                        setSource(new ExternalResource("/static/highcharts.html"));
                        setId(UUID.randomUUID().toString());
                }};
                buildButton = new Button("Построить");
                buildButton.addClickListener(e->{
                        //---
                        PlotData allDta = new PlotData("Все документы",new ArrayList<>(),new ArrayList<>());
                        PlotData curData = new PlotData(record.getName(),new ArrayList<>(), new ArrayList<>());
                        allDta.ys = new ArrayList<>(Arrays.asList(30,40,0,50));
                        curData.ys = new ArrayList<>(Arrays.asList(25));
                        allDta.xs = new ArrayList<>(Arrays.asList(130,430,160,250));
                        curData.xs = new ArrayList<>(Arrays.asList(400));
                        //---
                        AnalisisData analisisData = new AnalisisData("Количество страниц","Количество картинок","Зависимость количества картинок от количества страниц");
                        com.vaadin.ui.JavaScript.getCurrent().execute("((document.getElementById('"+htmlFrame.getId()+"').childNodes[0].contentWindow).buildLinearChart("+new Gson().toJson(allDta)+","+ new Gson().toJson(curData)+","+new Gson().toJson(analisisData)+"))");
                });
                addComponents(buildButton,htmlFrame);
                setExpandRatio(htmlFrame,1);
        }

}