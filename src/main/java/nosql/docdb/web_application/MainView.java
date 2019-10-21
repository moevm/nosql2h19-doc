package nosql.docdb.web_application;

import com.github.daishy.rangeslider.RangeSlider;
import com.github.daishy.rangeslider.client.Range;
import com.vaadin.contextmenu.GridContextMenu;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import lombok.SneakyThrows;
import lombok.Value;
import nosql.docdb.file_utils.FileUtills;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MainView extends VerticalLayout {
    private final BrowserFrame previewFrame;
    private final Grid<FileFullInfo> resultsGrid;
    private final TextField queryField;
    private final Button searchButton;
    private final Button exportDB;
    private final Button showStatisticButton;
    private final CheckBox searchInTextCheckBox;
    private final CheckBox definedDocumentStructureCheckBox;
    private final CheckBox documentsWithoutHeadersCheckBox;
    private final Label countOfDocumentLabel;
    private final RangeSlider limitCountOfPagesSlider;
    private final RangeSlider limitWeightSlider;

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
        limitCountOfPagesSlider  = new RangeSlider("Количество страниц", new Range(0, 1000));
        limitCountOfPagesSlider.setValue(new Range(50, 100));
        limitCountOfPagesSlider.setStep(10);
        limitWeightSlider  = new RangeSlider("Объем документа, Мб", new Range(0, 1000));
        limitWeightSlider.setValue(new Range(50, 100));
        limitWeightSlider.setStep(10);
        resultsGrid=new Grid<FileFullInfo>(){{
            setSizeFull();
            addColumn(FileFullInfo::getName).setCaption("Документ");
            addColumn(FileFullInfo::getCountOfPages).setCaption("Количество страниц");
            addColumn(FileFullInfo::getSize).setCaption("Объем документа, Мб");

            setItems(genSampleRecords());
            new GridContextMenu<FileFullInfo>(this){{
                addGridFooterContextMenuListener(e->removeItems());
                addGridHeaderContextMenuListener(e->removeItems());
                addGridBodyContextMenuListener(e->{
                    removeItems();
                    if(!getSelectedItems().contains(e.getItem()))return;
                    addItem("Дополнительно",ee->{
                        // open page
                        UI.getCurrent().addWindow(new FileInfoWindow(e.getItem()));
                    });
                    FileFullInfo fileFullInfo =e.getItem();

                });
            }};
        }};

        queryField=new TextField(){{
            setWidth("100%");
        }};
        searchButton=new Button("Поиск",e->{

        });
        searchInTextCheckBox = new CheckBox("Искать в тексте");
        countOfDocumentLabel =new Label("Количество документов: 4");
        exportDB = new Button("Страница администратора");
        exportDB.setStyleName("i-hPadding3 small i-small");
        showStatisticButton = new Button( "Статистика");
        showStatisticButton.setStyleName("i-hPadding3 small i-small");
        showStatisticButton.addClickListener(e->{
            Optional<FileFullInfo> selected=resultsGrid.getSelectedItems().stream().findFirst();
            selected.ifPresent(fileFullInfo -> UI.getCurrent().addWindow(new StatisticWindow(fileFullInfo)));
        });


        exportDB.addClickListener(e->{
            UI.getCurrent().addWindow(new AdminWindow());
        });

        definedDocumentStructureCheckBox =new CheckBox("Документы с заданной структурой");
        documentsWithoutHeadersCheckBox =new CheckBox("Документы без заголовков");

        HorizontalLayout upperPanel = new HorizontalLayout(
                new VerticalLayout(definedDocumentStructureCheckBox, documentsWithoutHeadersCheckBox) {{
                    setMargin(false);
                    //setSizeFull();
                }},
                new VerticalLayout(limitCountOfPagesSlider,limitWeightSlider) {{
                    setMargin(false);
                    setSizeFull();
                    limitCountOfPagesSlider.setSizeFull();
                    limitWeightSlider.setSizeFull();

                }}
                ,showStatisticButton
        )   {{
            setWidth("100%");
            setComponentAlignment(showStatisticButton,Alignment.MIDDLE_RIGHT);
        }};
        HorizontalSplitPanel horizontalSplit=new HorizontalSplitPanel(){{
            setSizeFull();
            setFirstComponent(new VerticalLayout(){{
                setSizeFull();
                addComponents(
                       upperPanel,

                        new HorizontalLayout(queryField,searchButton,searchInTextCheckBox){{
                            setWidth("100%");
                            setComponentAlignment(searchInTextCheckBox,Alignment.MIDDLE_CENTER);
                            setExpandRatio(queryField,1);
                        }},
                        resultsGrid,
                        new HorizontalLayout(countOfDocumentLabel,exportDB) {{
                            setMargin(false);
                            setWidth("100%");
                            setComponentAlignment(exportDB,Alignment.MIDDLE_RIGHT);
                               setExpandRatio(exportDB,1);
                               setExpandRatio(countOfDocumentLabel,3);

                        }}
                );
                setExpandRatio(resultsGrid,1);
            }});
            setSecondComponent(previewFrame);
        }};


        addComponent(horizontalSplit);
    }

    @Value
    public static class FileFullInfo {
        final String name;
        final int countOfImages;
        final int countOfTables;
        final int countOfPages;
        final double size;
        final String dateOfCreate;
        final String dateOfLoad;

        public String getOfDateCreateString(){
            return dateOfCreate;
        }
        public String getDateOfLoadString(){
            return dateOfLoad;
        }
    }


    public static List<FileFullInfo> genSampleRecords(){
        return Arrays.asList(
                new FileFullInfo("Энциклопедия обо всем",100,100, 300,20.2,"01.01.2015","01.02.2015"),
                new FileFullInfo("Уголовный кодекс", 0, 0,600,10.1,"11.12.2015","05.11.2016"),
                new FileFullInfo("ТОЭ. Методическое пособие", 8,10,115,5.9,"01.11.2012","01.02.2015"),
                new FileFullInfo("Криптография. Лабораторный практикум",18,11,58,3.2,"31.01.2018","15.04.2019")
        );
    }
}

