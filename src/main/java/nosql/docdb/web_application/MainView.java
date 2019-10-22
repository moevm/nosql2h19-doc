package nosql.docdb.web_application;

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

    private final Label countOfDocumentLabel;
    private final Button filterButton;
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
        filterButton = new Button("Фильтры");
        filterButton.addClickListener(e->{
            UI.getCurrent().addWindow(new FilterWindow());

        });
        filterButton.setStyleName("i-hPadding3 small i-small");

        searchInTextCheckBox = new CheckBox("Искать в тексте");
        countOfDocumentLabel =new Label("Количество документов: 4");
        exportDB = new Button("Страница администратора");
        exportDB.setStyleName("i-hPadding3 small i-small");
        showStatisticButton = new Button( "Статистика");
        showStatisticButton.setStyleName("i-hPadding3 small i-small");
        showStatisticButton.addClickListener(e->{
            Optional<FileFullInfo> selected=resultsGrid.getSelectedItems().stream().findFirst();
            selected.ifPresent(fileFullInfo -> UI.getCurrent().addWindow(new StatisticWindowNew(fileFullInfo)));
        });


        exportDB.addClickListener(e->{
            UI.getCurrent().addWindow(new AdminWindow());
        });



        HorizontalSplitPanel horizontalSplit=new HorizontalSplitPanel(){{
            setSizeFull();
            setFirstComponent(new VerticalLayout(){{
                setSizeFull();
                addComponents(
                        new HorizontalLayout(filterButton,showStatisticButton){{
                            //setWidth("100%");

                            //setComponentAlignment(showStatisticButton,Alignment.MIDDLE_RIGHT);

                        }},
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

