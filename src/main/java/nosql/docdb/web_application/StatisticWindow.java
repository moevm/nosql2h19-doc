package nosql.docdb.web_application;

import com.google.gson.Gson;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.*;
import nosql.docdb.doc_parser.object_model.DbDocument;

import java.util.ArrayList;
import java.util.Arrays;

public class StatisticWindow extends Window {
        private final BrowserFrame htmlFrame;
        private final Button analisisOfImagesCountButton;
        private final Button analisisOfTablesCountButton;
        private final TextField textField;
        private final Button analisisOfWordsButton;
        private final CheckBox separatedTextsCheckBox;
        private PlotData allDta =
                new PlotData("Все документы",new ArrayList<>(),new ArrayList<>());
        public StatisticWindow(DbDocument record) {
                super("Статистика по документу \""+record.getName()+"\""); // Set window caption
                center();
                setClosable(true);
                setModal(false);
                setWidth("500px");
                setHeight("500px");

                PlotData curData = new PlotData(record.getName(),new ArrayList<>(), new ArrayList<>());
                htmlFrame=new BrowserFrame(){{
                        setSizeFull();
                        setSource(new ExternalResource("/static/highcharts.html"));
                }};
                htmlFrame.setId("brf");
                HorizontalSplitPanel horizontalSplitPanel = new HorizontalSplitPanel();
                VerticalLayout layoutButtons = new VerticalLayout();
                VerticalLayout layoutPlot = new VerticalLayout();
                layoutPlot.addComponent(htmlFrame);
                layoutPlot.setHeight("100%");
                htmlFrame.setWidth("100%");
                separatedTextsCheckBox = new CheckBox("Документы по отдельности");
                analisisOfImagesCountButton = new Button("Статистика по количеству картинок");
                analisisOfImagesCountButton.addClickListener(e->{
                        //---
                        allDta.ys = new ArrayList<>(Arrays.asList(30,40,0,50));
                        curData.ys = new ArrayList<>(Arrays.asList(25));
                        allDta.xs = new ArrayList<>(Arrays.asList(130,430,160,250));
                        curData.xs = new ArrayList<>(Arrays.asList(400));
                        //---
                        AnalisisData analisisData = new AnalisisData("Количество страниц","Количество картинок","Зависимость количества картинок от количества страниц");
                        com.vaadin.ui.JavaScript.getCurrent().execute("((document.getElementById(\"brf\").childNodes[0].contentWindow).buildLinearChart("+new Gson().toJson(allDta)+","+ new Gson().toJson(curData)+","+new Gson().toJson(analisisData)+"))");
                });
                analisisOfTablesCountButton = new Button("Статистика по количеству таблиц");
                analisisOfTablesCountButton.addClickListener(e->{
                        //---
                        allDta.ys = new ArrayList<>(Arrays.asList(3,5,0,14));
                        curData.ys = new ArrayList<>(Arrays.asList(25));
                        allDta.xs = new ArrayList<>(Arrays.asList(130,430,160,250));
                        curData.xs = new ArrayList<>(Arrays.asList(400));
                        //---
                        AnalisisData analisisData = new AnalisisData("Количество страниц","Количество таблиц","Зависимость количества таблиц от количества страниц");
                        com.vaadin.ui.JavaScript.getCurrent().execute("((document.getElementById(\"brf\").childNodes[0].contentWindow).buildLinearChart("+new Gson().toJson(allDta)+","+ new Gson().toJson(curData)+","+new Gson().toJson(analisisData)+"))");
                });
                HorizontalLayout analisisOfWordLayout = new HorizontalLayout();
                textField = new TextField();
                analisisOfWordsButton = new Button("Частота встречаемости слова");
                analisisOfWordsButton.addClickListener(e->{
                        AnalisisData analisisData = new AnalisisData("Документ","Частота встречаемости","Популярность слова алгоритм");
                        //---
                        allDta.ys = new ArrayList<>(Arrays.asList(3));
                        curData.ys = new ArrayList<>(Arrays.asList(8));
                        PlotData plot1 = new PlotData("Уголовный кодекс",new ArrayList<>(Arrays.asList(5)));
                        PlotData plot2 = new PlotData("Энциклопедия обо всем",new ArrayList<>(Arrays.asList(8)));
                        PlotData plot3 = new PlotData("ТОЭ. Методическое пособие",new ArrayList<>(Arrays.asList(1)));
                        PlotData plot4 = new PlotData("Криптография. Лабораторный практикум",new ArrayList<>(Arrays.asList(2)));
                        ArrayList<PlotData> data = new ArrayList<>();
                        if(!separatedTextsCheckBox.getValue())data.addAll(Arrays.asList(allDta,curData));
                        else data.addAll(Arrays.asList(plot1,plot2,plot3,plot4));
                        //--
                        com.vaadin.ui.JavaScript.getCurrent().execute("((document.getElementById(\"brf\").childNodes[0].contentWindow).buildBarChart("+new Gson().toJson(data)+","+new Gson().toJson(analisisData)+"))");
                });
                analisisOfWordsButton.setWidth("100%");
                analisisOfWordLayout.addComponents(new HorizontalLayout(analisisOfWordsButton,new VerticalLayout(separatedTextsCheckBox,textField){{
                        setMargin(false);
                        textField.setWidth("100%");
                }}));
                //analisisOfWordLayout.setComponentAlignment(separatedTextsCheckBox,Alignment.MIDDLE_LEFT);

                layoutButtons.addComponents(new HorizontalLayout(analisisOfImagesCountButton, analisisOfTablesCountButton),analisisOfWordLayout);

                horizontalSplitPanel.addComponents(layoutButtons, layoutPlot);
                setContent(horizontalSplitPanel);

                //layout.setExpandRatio(dateOfcreate, 1);

        }
}
