package nosql.docdb.web_application;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import nosql.docdb.doc_parser.object_model.DbDocument;
import nosql.docdb.doc_parser.object_model.Picture;
import nosql.docdb.doc_parser.object_model.Table;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public  class FileInfoWindow extends Window {
    public FileInfoWindow(DbDocument record) {
        super("Информация о документе \""+record.getName()+"\"");
        center();
        setWidth("40%");
        setClosable(true);
        setModal(false);
        VerticalLayout layout = new VerticalLayout();
        setContent(layout);
        Label dateOfLoad = new Label("Дата загрузки: "+ DateUtil.formatDate(record.getAddDate()));
        Label imageCount = new Label("Количество картинок: "+record.getDocumentObjects().stream().filter(obj->obj instanceof Picture).count());
        Label tableCount = new Label("Количество таблиц: "+record.getDocumentObjects().stream().filter(obj->obj instanceof Table).count());
        layout.addComponents(dateOfLoad,imageCount,tableCount);
    }
}

