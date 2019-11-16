package nosql.docdb.doc_parser.object_model;

import lombok.Value;

@Value
public class Picture implements DocumentObject{
    String name;
    String fileExtension;
}
