package nosql.docdb.web_application;

import com.google.gson.Gson;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.*;

import java.util.ArrayList;

public class StatisticWindow extends Window {
        private final BrowserFrame htmlFrame;
        private PlotData countOfImagesFromPages=
                new PlotData("Все документы",new ArrayList<>(),new ArrayList<>());

        public StatisticWindow(MainView.FileFullInfo record) {
                super("Статистика по документу \""+record.getName()+"\""); // Set window caption
                center();
                setClosable(true);
                setModal(false);
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
                Button analisisOfImagesCount = new Button("Статистика по количеству картинок");
                analisisOfImagesCount.addClickListener(e->{
                        AnalisisData analisisData = new AnalisisData("Количество страниц","Количество картинок","Зависимость количества картинок от количества страниц");
                        com.vaadin.ui.JavaScript.getCurrent().execute("((document.getElementById(\"brf\").childNodes[0].contentWindow).buildLinearChart("+new Gson().toJson(countOfImagesFromPages)+","+ new Gson().toJson(curData)+","+new Gson().toJson(analisisData)+"))");
                });
                Button analisisOfWords = new Button("Частота встречаемости слова");
                analisisOfWords.addClickListener(e->{
                        AnalisisData analisisData = new AnalisisData("Слово","Частота встречаемости","Популярность слова");
                        com.vaadin.ui.JavaScript.getCurrent().execute("((document.getElementById(\"brf\").childNodes[0].contentWindow).buildBarChart())");
                });
                layoutButtons.addComponents(analisisOfImagesCount,analisisOfWords);
                horizontalSplitPanel.addComponents(layoutButtons, layoutPlot);
                setContent(horizontalSplitPanel);

                //layout.setExpandRatio(dateOfcreate, 1);

        }
}
class PlotData{


        public PlotData(String parametrName,ArrayList<Double> xs, ArrayList<Double> ys) {
                this.parametrName = parametrName;
                this.xs = xs;
                this.ys = ys;
        }

        String parametrName;

        ArrayList<Double> xs = new ArrayList<>();
        ArrayList<Double> ys = new ArrayList<>();
}

class AnalisisData{
        public AnalisisData(String xAxis, String yAxis, String plotName) {
                this.xAxis = xAxis;
                this.yAxis = yAxis;
                this.plotName = plotName;
        }

        String xAxis;
        String yAxis;
        String plotName;
}