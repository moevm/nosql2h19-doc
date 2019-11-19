package nosql.docdb.doc_to_pdf;

import nosql.docdb.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

public class DocToPdfConverter {
    public static byte[] convert(byte[] docFile) throws Exception {
        File in=File.createTempFile(UUID.randomUUID().toString(),".docx");
        in.deleteOnExit();

        try(FileOutputStream fos=new FileOutputStream(in)) {
            fos.write(docFile);
        }

        Process p=Runtime.getRuntime().exec("doc2pdf --stdout "+in.getAbsolutePath());
        byte[] pdf= FileUtils.readAllBytes(p.getInputStream());
        in.delete();
        return pdf;
    }
}
