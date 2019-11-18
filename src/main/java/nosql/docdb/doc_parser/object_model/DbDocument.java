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
    String pdfFileId;
    List<Paragraph> paragraphs;
    List<DocumentObject> documentObjects;

    public static DbDocument fromParsedDocument(ParsedDocument document, String storedRawBytesId, String storedPdfBytesId){
        return new DbDocument(
                document.getName(),
                document.getAddDate(),
                document.getPageCount(),
                document.getSize(),
                storedRawBytesId,
                storedPdfBytesId,
                document.getParagraphs(),
                document.getDocumentObjects()
        );
    }

    public ParsedDocument toParsedDocument(byte[] bytes, byte[] pdfBytes){
        return new ParsedDocument(
                getName(),
                getAddDate(),
                getPageCount(),
                getSize(),
                bytes,
                pdfBytes,
                getParagraphs(),
                getDocumentObjects()
        );
    }
}
