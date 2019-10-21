package nosql.docdb.web_application;

import com.vaadin.ui.*;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import server.droporchoose.UploadComponent;

import java.io.InputStream;
import java.nio.file.Path;

public class AdminWindow extends Window {
    private MultiFileUpload exportDBDialog;
    private MultiFileUpload importDBDialog;
    private MultiFileUpload addToDBDialog;

    private void uploadReceived(String fileName, Path file) {
    }

    private void uploadStarted(String fileName) {
    }

    private void uploadProgress(String fileName, long readBytes, long contentLength) {

    }

    private void uploadFailed(String fileName, Path file) {
    }

    public AdminWindow() {
        super("Страница администратора"); // Set window caption
        center();
        setClosable(true);
        setModal(false);
        VerticalLayout verticalLayout = new VerticalLayout();
        Label overAllDocs = new Label("Всего документов: 4");
        Label lastLoadTime = new Label("Последняя загрузка: 15.10.19");
        verticalLayout.addComponent(overAllDocs);
        verticalLayout.addComponent(lastLoadTime);

        UploadComponent uploadComponent = new UploadComponent(this::uploadReceived);
        uploadComponent.setStartedCallback(this::uploadStarted);
        uploadComponent.setProgressCallback(this::uploadProgress);
        uploadComponent.setFailedCallback(this::uploadFailed);
        uploadComponent.setWidth(500, Unit.PIXELS);
        uploadComponent.setHeight(100, Unit.PIXELS);
        uploadComponent.setCaption("Импортировать БД");
        uploadComponent.setStyleName("i-hPadding3 small i-small");
        //verticalLayout.addComponent(uploadComponent);
        exportDBDialog = new MultiFileUpload(new UploadFinishedHandler() {
            @Override
            public void handleFile(InputStream inputStream, String s, String s1, long l, int i) {

            }
        }, new UploadStateWindow());
        exportDBDialog.setUploadButtonCaptions("Экспортировать БД","Экспортировать БД");
        importDBDialog = new MultiFileUpload(new UploadFinishedHandler() {
            @Override
            public void handleFile(InputStream inputStream, String s, String s1, long l, int i) {

            }
        }, new UploadStateWindow());
        importDBDialog.setUploadButtonCaptions("Импортировать БД","Импортировать БД");
        addToDBDialog = new MultiFileUpload(new UploadFinishedHandler() {
            @Override
            public void handleFile(InputStream inputStream, String s, String s1, long l, int i) {

            }
        }, new UploadStateWindow());
        addToDBDialog.setUploadButtonCaptions("Добавить файл в БД","Добавить файл в БД");
        verticalLayout.addComponent(exportDBDialog);
        verticalLayout.addComponent (importDBDialog);
        verticalLayout.addComponent (addToDBDialog);

        setContent(verticalLayout);

        //layout.setExpandRatio(dateOfcreate, 1);

    }
}
