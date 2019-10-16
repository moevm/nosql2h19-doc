package nosql.docdb.web_application;

import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import lombok.SneakyThrows;
import lombok.Value;
import nosql.docdb.file_utils.FileUtills;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

public class MainView extends VerticalLayout {
    private final BrowserFrame previewFrame;
    private final Grid<ResultRecord> resultsGrid;
    private final TextField queryField;
    private final Button searchButton;

    private final CheckBox definedDocumentStructureCheckBox;
    private final CheckBox documentsWithoutHeadersCheckBox;

    private final Label countOfDocumentLabel;

    @SneakyThrows
    public MainView(){
        setSizeFull();
        setMargin(false);
        previewFrame=new BrowserFrame(){{
            setSizeFull();

            byte[] pdf;
            try(FileInputStream fis=new FileInputStream("Лабы методичка.pdf")) {
                pdf= FileUtills.readAllBytes(fis);
            }

            setSource(new StreamResource(() -> new ByteArrayInputStream(pdf), "Криптография. Лабораторный практикум.pdf"));
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


        definedDocumentStructureCheckBox =new CheckBox("Документы с заданной структурой");
        documentsWithoutHeadersCheckBox =new CheckBox("Документы без заголовков");

        HorizontalSplitPanel horizontalSplit=new HorizontalSplitPanel(){{
            setSizeFull();
            setFirstComponent(new VerticalLayout(){{
                setSizeFull();
                addComponents(

                        new VerticalLayout(definedDocumentStructureCheckBox, documentsWithoutHeadersCheckBox){{
                            setMargin(false);
                            //setSizeFull();
                        }},
                        new HorizontalLayout(queryField,searchButton){{
                            setWidth("100%");
                            setExpandRatio(queryField,1);
                        }},
                        resultsGrid,
                        new VerticalLayout(countOfDocumentLabel) {{
                            setMargin(false);
                        }}
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
