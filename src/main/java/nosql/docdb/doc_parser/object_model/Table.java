package nosql.docdb.doc_parser.object_model;

import lombok.Value;

@Value
public class Table implements DocumentObject{
    String name;
    int rows;
    int columns;
}
