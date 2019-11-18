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
    byte[] pdfBytes;
    List<Paragraph> paragraphs;
    List<DocumentObject> documentObjects;
}