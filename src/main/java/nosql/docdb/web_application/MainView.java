package nosql.docdb.web_application;

import com.vaadin.contextmenu.GridContextMenu;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import lombok.SneakyThrows;
import nosql.docdb.database.MongoDB;
import nosql.docdb.doc_parser.object_model.DbDocument;
import nosql.docdb.web_application.statistic_window.StatisticWindowNew;

import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Optional;

public class MainView extends VerticalLayout {
    private final BrowserFrame previewFrame;
    private final Grid<DbDocument> resultsGrid;
    private final TextField queryField;
    private final Button searchButton;
    private final Button exportDB;
    private final Button showStatisticButton;
    private final CheckBox searchInTextCheckBox;

    private final Label countOfDocumentLabel;
    private final Button filterButton;
    private final FilterWindow filterWindow;
    private final MongoDB mongoDB=new MongoDB();

    @SneakyThrows
    public MainView(){
        setSizeFull();
        setMargin(false);
        previewFrame=new BrowserFrame(){{
            setSizeFull();
        }};
        filterWindow=new FilterWindow(e->{},mongoDB);
        UI.getCurrent().addWindow(filterWindow);
        resultsGrid=new Grid<DbDocument>(){{
            setSizeFull();
            addColumn(DbDocument::getName).setCaption("Документ");
            addColumn(DbDocument::getPageCount).setCaption("Количество страниц");
            addColumn(d->{
                NumberFormat formatter = new DecimalFormat("#0.00");
                return formatter.format(d.getSize()/1024.0/1024);
            }).setCaption("Объем документа, Мб");

            setItems(mongoDB.loadDocuments(MongoDB.Query.builder().limit(1000).build()));

            addSelectionListener(e->{
                e.getFirstSelectedItem()
                        .ifPresent(doc->{
                            ByteArrayInputStream pdfStream=new ByteArrayInputStream(mongoDB.loadRawBytes(doc).getRight());
                            previewFrame.setSource(new StreamResource(()->pdfStream,doc.getName()+".pdf"));
                        });
            });

            new GridContextMenu<DbDocument>(this){{
                addGridFooterContextMenuListener(e->removeItems());
                addGridHeaderContextMenuListener(e->removeItems());
                addGridBodyContextMenuListener(e->{
                    removeItems();
                    if(!getSelectedItems().contains(e.getItem()))return;
                    addItem("Дополнительно",ee->{
                        UI.getCurrent().addWindow(new FileInfoWindow(e.getItem()));
                    });
                    DbDocument fileFullInfo =e.getItem();

                });
            }};
        }};

        queryField=new TextField(){{
            setWidth("100%");
        }};

        filterButton = new Button("Фильтры");
        filterButton.addClickListener(e->{
            filterWindow.setVisible(true);
        });
        filterButton.setStyleName("i-hPadding3 small i-small");

        searchInTextCheckBox = new CheckBox("Искать в тексте");
        countOfDocumentLabel =new Label("Количество документов: "+mongoDB.getCountOfDocuments());
        exportDB = new Button("Страница администратора");
        exportDB.setStyleName("i-hPadding3 small i-small");
        showStatisticButton = new Button( "Статистика");
        showStatisticButton.setStyleName("i-hPadding3 small i-small");
        showStatisticButton.addClickListener(e->{
            UI.getCurrent().addWindow(new StatisticWindowNew(mongoDB,readQuery()));
        });


        exportDB.addClickListener(e->{
            UI.getCurrent().addWindow(new AdminWindow(mongoDB));
        });


        searchButton=new Button("Поиск",e->{
            MongoDB.Query query= readQuery();
            System.out.println(query);
            countOfDocumentLabel.setValue("Количество документов: "+mongoDB.getCountOfDocuments());
            resultsGrid.setItems(mongoDB.loadDocuments(query));
        });

        HorizontalSplitPanel horizontalSplit=new HorizontalSplitPanel(){{
            setSizeFull();
            setFirstComponent(new VerticalLayout(){{
                setSizeFull();
                addComponents(
                        new HorizontalLayout(filterButton,showStatisticButton){{ }},
                        new HorizontalLayout(queryField,searchButton,searchInTextCheckBox){{
                            setWidth("100%");
                            setComponentAlignment(searchInTextCheckBox,Alignment.MIDDLE_CENTER);
                            setExpandRatio(queryField,1);
                        }},
                        resultsGrid,
                        new HorizontalLayout(countOfDocumentLabel,exportDB) {{
                            setMargin(false);
                            setWidth("100%");
                            setComponentAlignment(countOfDocumentLabel,Alignment.MIDDLE_LEFT);
                            setComponentAlignment(exportDB,Alignment.MIDDLE_RIGHT);
                        }}
                );
                setExpandRatio(resultsGrid,1);
            }});
            setSecondComponent(previewFrame);
        }};


        addComponent(horizontalSplit);
    }

    private MongoDB.Query readQuery(){
        FilterWindow.FilterWindowParams filters=filterWindow.getValue();
        return MongoDB.Query.builder()
                .limit(1000)
                .minPageCount(filters.getPageCount().getLeft())
                .maxPageCount(filters.getPageCount().getRight())
                .minSize(filters.getSize().getLeft()*1024)
                .maxSize(filters.getSize().getRight()*1024)
                .format(filters.getDocFormat())
                .findMode(searchInTextCheckBox.getValue()? MongoDB.Query.FindMode.EVERYWHERE: MongoDB.Query.FindMode.IN_FILE_NAME)
                .findString(queryField.getValue())
                .build();
    }
}

