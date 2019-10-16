package nosql.docdb.web_application;

import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import lombok.SneakyThrows;
import lombok.Value;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

public class MainView extends VerticalLayout {
    private final BrowserFrame previewFrame;
    private final Grid<ResultRecord> resultsGrid;
    private final TextField queryField;
    private final Button searchButton;

    private final CheckBox option1;
    private final CheckBox option2;

    private final Label countOfDocumentLabel;
    private final Label countOfDocumentWithoutHeadersLabel;
    private final Label countOfDocumentWithDefinedStructureLabel;

    @SneakyThrows
    public MainView(){
        setSizeFull();

        previewFrame=new BrowserFrame(){{
            setSizeFull();

            byte[] pdf= new FileInputStream("Лабы методичка.pdf").readAllBytes();
            setSource(new StreamResource(() -> new ByteArrayInputStream(pdf), "file.pdf"));
        }};

        resultsGrid=new Grid<ResultRecord>(){{
            setSizeFull();
            addColumn(ResultRecord::getName).setCaption("Документ");
            addColumn(ResultRecord::getCountOfImages).setCaption("Количество изображений");
            addColumn(ResultRecord::getCountOfTables).setCaption("Количество таблиц");
            setItems(genSampleRecords());
        }};

        queryField=new TextField(){{
            setWidth("100%");
        }};
        searchButton=new Button("Поиск",e->{

        });

        countOfDocumentLabel =new Label("Количество документов: 4");
        countOfDocumentWithoutHeadersLabel =new Label("Количество документов без заголовков: 2");
        countOfDocumentWithDefinedStructureLabel =new Label("Количество документов с заданной структурой: 3");

        option1=new CheckBox("Документы с заданной структурой");
        option2=new CheckBox("Документы без заголовков");

        HorizontalSplitPanel horizontalSplit=new HorizontalSplitPanel(){{
            setSizeFull();
            setFirstComponent(new VerticalLayout(){{
                setSizeFull();
                addComponents(
                        new VerticalLayout(option1,option2){{
                            setWidth("100%");
                        }},
                        new HorizontalLayout(queryField,searchButton){{
                            setWidth("100%");
                            setExpandRatio(queryField,1);
                        }},
                        resultsGrid,
                        new VerticalLayout(countOfDocumentLabel,countOfDocumentWithDefinedStructureLabel,countOfDocumentWithoutHeadersLabel)
                );
                setExpandRatio(resultsGrid,1);
            }});
            setSecondComponent(previewFrame);
        }};


        addComponent(horizontalSplit);
    }

    @Value
    public static class ResultRecord{
        String name;
        int countOfImages;
        int countOfTables;
    }


    public static List<ResultRecord> genSampleRecords(){
        return Arrays.asList(
                new ResultRecord("Энциклопедия обо всем",100,100),
                new ResultRecord("Уголовный кодекс", 0, 0),
                new ResultRecord("ТОЭ. Методическое пособие", 8,10),
                new ResultRecord("Криптография. Лабораторный практикум",18,11)
        );
    }
}
