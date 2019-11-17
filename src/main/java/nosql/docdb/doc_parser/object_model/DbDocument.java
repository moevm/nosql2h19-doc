package nosql.docdb.doc_parser.object_model;

import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
public class DbDocument{
    String name;
    //Instant creationDate;
    Instant addDate;
    int pageCount;
    long size;
    String rawFileId;
    List<Paragraph> paragraphs;
    List<DocumentObject> documentObjects;

    public static DbDocument fromParsedDocument(ParsedDocument document, String storedRawBytesId){
        return new DbDocument(
                document.getName(),
                document.getAddDate(),
                document.getPageCount(),
                document.getSize(),
                storedRawBytesId,
                document.getParagraphs(),
                document.getDocumentObjects()
        );
    }
}
