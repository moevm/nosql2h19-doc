package nosql.docdb.web_application;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public  class FileInfoWindow extends Window {
    public FileInfoWindow(MainView.FileFullInfo record) {
        super("Дополнительная информация о документе \""+record.getName()+"\""); // Set window caption
        center();
        setClosable(true);
        setModal(false);
        VerticalLayout layout = new VerticalLayout();
        setContent(layout);
        Label dateOfcreate = new Label("Дата создания: "+record.getOfDateCreateString());
        Label dateOfLoad = new Label("Дата загрузки: "+record.getDateOfLoadString());
        Label imageCount = new Label("Количество картинок: "+record.getCountOfImages());
        Label tableCount = new Label("Количество таблиц: "+record.getCountOfTables());
        layout.addComponents(dateOfcreate,dateOfLoad,imageCount,tableCount);
        //layout.setExpandRatio(dateOfcreate, 1);

    }
}

