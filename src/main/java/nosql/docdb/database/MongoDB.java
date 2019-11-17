package nosql.docdb.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import lombok.SneakyThrows;
import lombok.val;
import nosql.docdb.doc_parser.DocumentConverter;
import nosql.docdb.doc_parser.object_model.*;
import nosql.docdb.file_utils.FileUtills;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;


public class MongoDB{
    private final MongoCollection<org.bson.Document> collection;
    private final GridFSBucket gridFSBucket;
    private static final Gson GSON=new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapterFactory(
                    RuntimeTypeAdapterFactory.of(DocumentObject.class)
                            .registerSubtype(Picture.class)
                            .registerSubtype(Table.class)
            ).create();

    public MongoDB(){
        MongoClient mongoClient = MongoClients.create("mongodb://localhost");
        MongoDatabase database = mongoClient.getDatabase("mydb");
        collection = database.getCollection("documents");
        gridFSBucket=GridFSBuckets.create(database,"docx");
    }

    public void addDocument(ParsedDocument document){
        String fileId=gridFSBucket.uploadFromStream(document.getName(),new ByteArrayInputStream(document.getRawBytes())).toHexString();
        collection.insertOne(Document.parse(GSON.toJson(DbDocument.fromParsedDocument(document,fileId))));
    }

    public DbDocument loadDocument(){
        return GSON.fromJson(collection.find().first().toJson(),DbDocument.class);
    }

    public byte[] loadRawBytes(DbDocument dbDocument){
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        gridFSBucket.downloadToStream(new ObjectId(dbDocument.getRawFileId()),baos);
        return baos.toByteArray();
    }

    @SneakyThrows
    public static void main(String[] args) {
        ParsedDocument document= DocumentConverter.importFromDoc("dsp.docx", FileUtills.readAllBytes("documents/TsOS_lab_1.docx"));
        MongoDB db=new MongoDB();
        db.addDocument(document);

        DbDocument doc=db.loadDocument();
        System.out.println(doc);
        byte[] raw=db.loadRawBytes(doc);
        try(FileOutputStream fos=new FileOutputStream("from_db.docx")) {
            fos.write(raw);
        }
    }
}
