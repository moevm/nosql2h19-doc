package nosql.docdb.doc_to_pdf;

import lombok.SneakyThrows;
import nosql.docdb.file_utils.FileUtills;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class DocToPdfConverter {
    public static byte[] convert(byte[] docFile) throws Exception {
        File in=File.createTempFile(UUID.randomUUID().toString(),".docx");
        in.deleteOnExit();

        try(FileOutputStream fos=new FileOutputStream(in)) {
            fos.write(docFile);
        }

        Process p=Runtime.getRuntime().exec("doc2pdf --stdout "+in.getAbsolutePath());
        byte[] pdf= FileUtills.readAllBytes(p.getInputStream());
        in.delete();
        return pdf;
    }
}
