package nosql.docdb.doc_parser.object_model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
public class ParsedDocument {
    String name;
    //Instant creationDate;
    Instant addDate;
    int pageCount;
    long size;
    byte[] rawBytes;
    List<Paragraph> paragraphs;
    List<DocumentObject> documentObjects;
}

class Test{
    public static void main(String[] args) {
        Table t=new Table("table with name",15,2);
        Picture p=new Picture("pic name", "jpg");

        List<DocumentObject> documentObjects= Stream.of(t,p).collect(Collectors.toList());

        ParsedDocument d=new ParsedDocument("",null,0,0, null,null,documentObjects);


    }
}