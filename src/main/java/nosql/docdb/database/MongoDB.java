package nosql.docdb.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import lombok.*;
import nosql.docdb.doc_parser.DocumentConverter;
import nosql.docdb.doc_parser.object_model.*;
import nosql.docdb.file_utils.FileUtills;
import org.bson.*;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;


public class MongoDB{
    private final MongoCollection<org.bson.Document> collection;
    private final GridFSBucket gridFSBucket;
    private static final Gson GSON=new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapterFactory(
                    RuntimeTypeAdapterFactory.of(DocumentObject.class)
                            .registerSubtype(Picture.class)
                            .registerSubtype(Table.class)
            ).registerTypeAdapter(Instant.class, new TypeAdapter<Instant>() {
                @Override
                public void write(JsonWriter out, Instant value) throws IOException {
                    out.jsonValue("{\"$date\": "+value.toEpochMilli()+"}");
                }

                @Override
                public Instant read(JsonReader in) throws IOException {
                    Map<String,Long> date=new Gson().fromJson(in,new TypeToken<Map<String,Long>>(){}.getType());
                    return Instant.ofEpochMilli(date.get("$date"));
                }
            })
            .create();

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

    public List<DbDocument> loadDocuments(Query query){
        List<DbDocument> results=new ArrayList<>();
        collection.find()
                .filter(Filters.and(
                        Filters.regex("name",".*"+query.getNameContains()+".*"),
                        Filters.gte("pageCount",query.getMinPageCount()),
                        Filters.lte("pageCount",query.getMaxPageCount()),
                        Filters.gte("size",query.getMinSize()),
                        Filters.lte("size",query.getMaxSize())
                ))
                .sort(query.getSortDirection().getSortDirection().apply(query.getSortField()))
                .limit(query.getLimit())
                .cursor().forEachRemaining(d->{
                    results.add(GSON.fromJson(d.toJson(),DbDocument.class));
                });
        return results;
    }

    public long getCountOfDocuments(){
        return collection.countDocuments();
    }

    public Optional<DbDocument> getLastDocument(){
        return loadDocuments(
                Query.builder()
                        .limit(1)
                        .sortDirection(Query.SortDirection.DESCENDING)
                        .sortField("addDate")
                        .build()
        ).stream().findFirst();
    }

    @Value
    @Builder
    public static class Query{
        @Builder.Default
        int limit = Integer.MAX_VALUE;
        @Builder.Default
        int minPageCount=0;
        @Builder.Default
        int maxPageCount=Integer.MAX_VALUE;
        @Builder.Default
        long minSize=0;
        @Builder.Default
        long maxSize=Integer.MAX_VALUE;
        @Builder.Default
        DocFormat format=DocFormat.FREE_FORM;
        @Builder.Default
        String sortField="name";
        @Builder.Default
        SortDirection sortDirection=SortDirection.ASCENDING;
        @Builder.Default
        String nameContains="";

        @AllArgsConstructor
        @Getter
        public enum SortDirection{
            DESCENDING(Sorts::descending),
            ASCENDING(Sorts::ascending);

            private Function<String, Bson> sortDirection;
        }

        public enum DocFormat{
            FREE_FORM, WITHOUT_TITLES, EXACT_STRUCTURE
        }
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
        //db.addDocument(document);

        //List<DbDocument> dbDocuments=db.loadDocuments(Query.builder().limit(1).build());
        //dbDocuments.stream()
        //        .forEach(d->System.out.println(d));

        System.out.println(
                db.loadDocuments(
                        Query.builder()
                                .nameContains("dsp")
                                .minSize(2522630)
                                .maxSize(2522635)
                                .build()
                )
                .stream()
                .map(DbDocument::getSize)
                .collect(Collectors.toList())
        );

    }
}
