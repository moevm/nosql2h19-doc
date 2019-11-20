package nosql.docdb.web_application;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import nosql.docdb.database.MongoDB;
import nosql.docdb.doc_parser.DocumentConverter;
import nosql.docdb.doc_parser.object_model.DbDocument;
import nosql.docdb.utils.FileUtils;
import org.apache.commons.compress.utils.IOUtils;
import server.droporchoose.UploadComponent;

import java.io.*;
import java.nio.file.Path;
import java.util.UUID;

public class AdminWindow extends Window {
    private final Label overAllDocsLabel;
    private final Label lastLoadTimeLabel;
    private final Button exportDBDialog;
    private final MultiFileUpload importDBDialog;
    private final MultiFileUpload addToDBDialog;
    private final MongoDB mongoDB;

    private void uploadReceived(String fileName, Path file) {
    }

    private void uploadStarted(String fileName) {
    }

    private void uploadProgress(String fileName, long readBytes, long contentLength) {

    }

    private void uploadFailed(String fileName, Path file) {
    }

    public AdminWindow(MongoDB mongoDB) {
        super("Страница администратора"); // Set window caption
        this.mongoDB=mongoDB;
        center();
        setClosable(true);
        setModal(false);
        VerticalLayout verticalLayout = new VerticalLayout();
        overAllDocsLabel = new Label();
        lastLoadTimeLabel = new Label();
        verticalLayout.addComponent(overAllDocsLabel);
        verticalLayout.addComponent(lastLoadTimeLabel);

        UploadComponent uploadComponent = new UploadComponent(this::uploadReceived);
        uploadComponent.setStartedCallback(this::uploadStarted);
        uploadComponent.setProgressCallback(this::uploadProgress);
        uploadComponent.setFailedCallback(this::uploadFailed);
        uploadComponent.setWidth(500, Unit.PIXELS);
        uploadComponent.setHeight(100, Unit.PIXELS);
        uploadComponent.setCaption("Импортировать БД");
        uploadComponent.setStyleName("i-hPadding3 small i-small");

        exportDBDialog = new Button("Экспортировать БД");

        new FileDownloader(new StreamResource(() -> {
            try {
                PipedOutputStream out=new PipedOutputStream();
                PipedInputStream in=new PipedInputStream(out);
                mongoDB.exportToZip(out);
                return in;
            }catch (Exception e){
                e.printStackTrace();
                Notification.show("Ошибка");
                return new ByteArrayInputStream(new byte[0]);
            }
        }, "export.zip")).extend(exportDBDialog);

        UI ui=UI.getCurrent();
        importDBDialog = new MultiFileUpload((inputStream, name, type, length, n) -> {
            File tmp=null;
            try {
                tmp=File.createTempFile(UUID.randomUUID().toString(),".zip");
                tmp.deleteOnExit();
                try(FileOutputStream fos=new FileOutputStream(tmp)){
                    IOUtils.copy(inputStream,fos);
                }
            }catch (Exception e){
                e.printStackTrace();
                Notification.show("Ошибка");
            }
            File tmpFinal=tmp;
            ui.access(()->{
                try {
                    mongoDB.importFromZip(tmpFinal.getAbsolutePath());
                    update();
                    System.out.println("Import Done");
                }catch (Exception e){
                    e.printStackTrace();
                    Notification.show("Ошибка");
                }finally {
                    if(tmpFinal!=null)tmpFinal.delete();
                }
            });
        }, new UploadStateWindow());

        importDBDialog.setUploadButtonCaptions("Импортировать БД","Импортировать БД");
        addToDBDialog = new MultiFileUpload((inputStream, name, type, length, n) -> {
            try{
                byte[] d= FileUtils.readAllBytes(inputStream);
                ui.access(()->{
                    try {
                        mongoDB.addDocument(DocumentConverter.importFromDoc(name,d));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Notification.show("Ошибка");
                    }
                    update();
                });
            }catch (Exception e){
                e.printStackTrace();
                Notification.show("Ошибка");
            }
        }, new UploadStateWindow());
        addToDBDialog.setUploadButtonCaptions("Добавить файл в БД","Добавить файл в БД");

        verticalLayout.addComponent(exportDBDialog);
        verticalLayout.addComponent (importDBDialog);
        verticalLayout.addComponent (addToDBDialog);

        setContent(verticalLayout);

        update();
    }

    private void update(){
        overAllDocsLabel.setValue("Всего документов: "+mongoDB.getCountOfDocuments());
        lastLoadTimeLabel.setValue("Последняя загрузка: "+
                mongoDB.getLastDocument()
                        .map(DbDocument::getAddDate)
                        .map(DateUtil::formatDate)
                        .orElse("-"));
    }
}
