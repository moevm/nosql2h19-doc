package nosql.docdb.doc_parser.object_model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
public class Document{
    String name;
    //Instant creationDate;
    Instant addDate;
    int pageCount;
    long size;
    byte[] binData;
    List<Paragraph> paragraphs;
    List<DocumentObject> documentObjects;

    public String toString(){
        return new GsonBuilder().setPrettyPrinting().create()
                .toJson(new PrintedForm(name,addDate,pageCount,size,paragraphs,documentObjects));
    }

    @Value
    private static class PrintedForm{
        String name;
        Instant addDate;
        int pageCount;
        long size;
        List<Paragraph> paragraphs;
        List<DocumentObject> documentObjects;
    }
}
