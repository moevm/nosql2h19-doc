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
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import lombok.*;
import nosql.docdb.doc_parser.DocumentConverter;
import nosql.docdb.doc_parser.object_model.*;
import nosql.docdb.file_utils.FileUtills;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.bson.*;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


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

    public List<String> loadIds(){
        List<String> ids=new ArrayList<>();
        collection.find().projection(Projections.include("_id")).cursor().forEachRemaining(id->{
            ids.add(id.getObjectId("_id").toHexString());
        });
        return ids;
    }

    public Optional<DbDocument> loadById(String id){
        return Optional.ofNullable(collection.find().filter(Filters.eq("_id",new ObjectId(id))).first())
                .map(d->GSON.fromJson(d.toJson(),DbDocument.class));
    }

    public List<DbDocument> loadDocuments(Query query){
        List<DbDocument> results=new ArrayList<>();
        collection.find()
                .filter(Filters.and(
                        Filters.regex("name",".*"+query.getNameContains()+".*"),
                        Filters.gte("pageCount",query.getMinPageCount()),
                        Filters.lte("pageCount",query.getMaxPageCount()),
                        Filters.gte("size",query.getMinSize()),
                        Filters.lte("size",query.getMaxSize()),
                        query.getFormat().getFilter()
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

    public void exportToZip(OutputStream os) throws IOException {
        List<String> ids=loadIds();
        try(ZipOutputStream zos=new ZipOutputStream(os)) {
            ids.stream()
                    .map(id->Pair.of(id,loadById(id)))
                    .filter(p->p.getRight().isPresent())
                    .map(p->Pair.of(p.getLeft(),p.getRight().get()))
                    .map(p-> Triple.of(p.getLeft(),p.getRight(),loadRawBytes(p.getRight())))
                    .forEach(p->{
                        safe(()->{
                            zos.putNextEntry(new ZipEntry(p.getMiddle().getRawFileId()));
                            zos.write(p.getRight());
                            zos.closeEntry();
                            zos.putNextEntry(new ZipEntry(p.getLeft()));
                            zos.write(GSON.toJson(p.getMiddle()).getBytes());
                            zos.closeEntry();
                            return null;
                        });
                    });
            zos.putNextEntry(new ZipEntry("content"));
            zos.write(GSON.toJson(ids).getBytes());
            zos.closeEntry();
        }
    }

    public void importFromZip(String filePath) throws IOException {
        try(ZipFile zf=new ZipFile(filePath)) {
            InputStream is=zf.getInputStream(new ZipEntry("content"));
            List<String> ids=GSON.fromJson(new InputStreamReader(is),new TypeToken<List<String>>(){}.getType());
            ids.stream().forEach(id->{
                safe(()->{
                    DbDocument document=GSON.fromJson(new InputStreamReader(zf.getInputStream(new ZipEntry(id))),DbDocument.class);
                    byte[] bytes=FileUtills.readAllBytes(zf.getInputStream(new ZipEntry(document.getRawFileId())));
                    addDocument(document.toParsedDocument(bytes));
                    return null;
                });
            });
        }
    }

    private static <T> void safe(Callable<T> action){
        try {
            action.call();
        }catch (Exception ignored){

        }
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

        @AllArgsConstructor
        @Getter
        public enum DocFormat{
            FREE_FORM(Filters.exists("_id")),
            WITHOUT_TITLES(Filters.size("paragraphs",1)),
            EXACT_STRUCTURE(
                    Filters.and(
                            Filters.elemMatch("paragraphs",Filters.regex("name",".*Цель работы.*")),
                            Filters.elemMatch("paragraphs",Filters.regex("name",".*Выводы.*"))
                    )
            );

            private Bson filter;
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
                                .format(Query.DocFormat.EXACT_STRUCTURE)
                                .build()
                )
                .stream()
                .map(DbDocument::getParagraphs)
                .map(p->p.stream().map(Paragraph::getName).collect(Collectors.toList()))
                .collect(Collectors.toList())
        );

        //db.importFromZip("export.zip");
    }
}