package nosql.docdb.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.mongodb.client.*;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.model.*;
import lombok.*;
import nosql.docdb.doc_parser.object_model.*;
import nosql.docdb.utils.FileUtils;
import nosql.docdb.utils.Utils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.bson.*;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class MongoDB{
    private static final Bson ALWAYS_TRUE_FILTER=Filters.exists("_id");
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
        String pdfFileId=gridFSBucket.uploadFromStream(document.getName(),new ByteArrayInputStream(document.getPdfBytes())).toHexString();
        collection.insertOne(Document.parse(GSON.toJson(DbDocument.fromParsedDocument(document,fileId, pdfFileId))));
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

    private Bson queryToFilter(Query query){
        return Filters.and(
                query.getFindMode().getFilter().apply(query.getFindString()),
                Filters.gte("pageCount",query.getMinPageCount()),
                Filters.lte("pageCount",query.getMaxPageCount()),
                Filters.gte("size",query.getMinSize()),
                Filters.lte("size",query.getMaxSize()),
                query.getFormat().getFilter()
        );
    }

    public FindIterable<Document> toMongoQuery(Query query){
        return collection.find()
                .filter(queryToFilter(query))
                .projection(Projections.metaTextScore("score"))
                .sort(query.getSortDirection().getSortDirection().apply(query.getSortField()))
                .limit(query.getLimit());
    }

    public List<DbDocument> loadDocuments(Query query){
        List<DbDocument> results=new ArrayList<>();
        toMongoQuery(query)
                .cursor().forEachRemaining(d->{
                    results.add(GSON.fromJson(d.toJson(),DbDocument.class));
                });
        return results;
    }

    public Set<String> getTermsFromTextSearchQuery(Query query){
        Map explain=GSON.fromJson(
                toMongoQuery(query)
                        .modifiers(new Document("$explain", true))
                        .first()
                        .toJson(),
                Map.class
        );

        return MongoDB.<Pair<String,Object>>flatify(
                Pair.of("root",explain),
                p->{
                    if(!(p.getRight() instanceof Map))return Stream.empty();
                    Map<String,Object> childs=(Map<String, Object>) p.getRight();
                    return childs.entrySet().stream().map(e->Pair.of(e.getKey(),e.getValue()));
                }
        ).filter(p->p.getLeft().equals("terms"))
        .map(Pair::getRight)
        .filter(r->r instanceof List)
        .map(r->(List)r)
        .flatMap((Function<List, Stream<String>>)  s->s.stream().map(Object::toString))
        .collect(Collectors.toSet());
    }

    public static <T>Stream<T> flatify(T root, Function<T,Stream<T>> childExtractor){
        return Stream.concat(
                Stream.of(root),
                childExtractor.apply(root).flatMap(c->flatify(c,childExtractor))
        );
    }

    public Pair<List<DbDocument>,Set<String>> topNFulltextSearch(String queryText, int limit){
        Query query=Query.builder()
                .findString(queryText)
                .findMode(Query.FindMode.EVERYWHERE)
                .limit(limit)
                .sortField("score")
                .sortDirection(Query.SortDirection.TEXT_SCORE)
                .build();

        List<DbDocument> documents=loadDocuments(query);
        Set<String> terms=getTermsFromTextSearchQuery(query);
        return Pair.of(documents,terms);
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

    public Future<?> exportToZip(OutputStream os) {
        ExecutorService es= Executors.newFixedThreadPool(1);
        Future<?> task=es.submit(()->{
            MongoDB db=new MongoDB();
            List<String> ids=db.loadIds();
            try(ZipOutputStream zos=new ZipOutputStream(os)) {
                ids.stream()
                        .map(id->Pair.of(id,db.loadById(id)))
                        .filter(p->p.getRight().isPresent())
                        .map(p->Pair.of(p.getLeft(),p.getRight().get()))
                        .map(p-> Triple.of(p.getLeft(),p.getRight(),db.loadRawBytes(p.getRight())))
                        .forEach(p->{
                            Utils.safe(()->{
                                zos.putNextEntry(new ZipEntry(p.getMiddle().getRawFileId()));
                                zos.write(p.getRight().getLeft());
                                zos.closeEntry();
                                zos.putNextEntry(new ZipEntry(p.getMiddle().getPdfFileId()));
                                zos.write(p.getRight().getRight());
                                zos.closeEntry();
                                zos.putNextEntry(new ZipEntry(p.getLeft()));
                                zos.write(GSON.toJson(p.getMiddle()).getBytes());
                                zos.closeEntry();
                            });
                        });
                zos.putNextEntry(new ZipEntry("content"));
                zos.write(GSON.toJson(ids).getBytes());
                zos.closeEntry();
                return null;
            }
        });
        es.shutdown();
        return task;
    }

    public void importFromZip(String filePath) throws IOException {
        try(ZipFile zf=new ZipFile(filePath)) {
            InputStream is=zf.getInputStream(new ZipEntry("content"));
            List<String> ids=GSON.fromJson(new InputStreamReader(is),new TypeToken<List<String>>(){}.getType());
            ids.stream().forEach(id->{
                Utils.safe(()->{
                    DbDocument document=GSON.fromJson(new InputStreamReader(zf.getInputStream(new ZipEntry(id))),DbDocument.class);
                    byte[] bytes= FileUtils.readAllBytes(zf.getInputStream(new ZipEntry(document.getRawFileId())));
                    byte[] pdfBytes= FileUtils.readAllBytes(zf.getInputStream(new ZipEntry(document.getPdfFileId())));
                    addDocument(document.toParsedDocument(bytes,pdfBytes));
                });
            });
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
        String findString ="";
        @Builder.Default
        FindMode findMode=FindMode.IN_FILE_NAME;

        @AllArgsConstructor
        @Getter
        public enum FindMode{
            IN_FILE_NAME(s->Filters.regex("name",".*"+s+".*")),
            EVERYWHERE(Filters::text);
            private Function<String, Bson> filter;
        }

        @AllArgsConstructor
        @Getter
        public enum SortDirection{
            DESCENDING(Sorts::descending),
            ASCENDING(Sorts::ascending),
            TEXT_SCORE(Sorts::metaTextScore);

            private final Function<String, Bson> sortDirection;
        }

        @AllArgsConstructor
        @Getter
        public enum DocFormat{
            FREE_FORM(ALWAYS_TRUE_FILTER,"Без ограничений"),
            WITHOUT_TITLES(Filters.size("paragraphs",1),"Без заголовков"),
            EXACT_STRUCTURE(
                    Filters.and(
                            Filters.elemMatch("paragraphs",Filters.regex("name",".*Цель работы.*")),
                            Filters.elemMatch("paragraphs",Filters.regex("name",".*Выводы.*"))
                    ),
                    "С заданной структурой"
            );

            private final Bson filter;
            private final String description;
        }
    }

    public Pair<byte[],byte[]> loadRawBytes(DbDocument dbDocument){
        ByteArrayOutputStream doc=new ByteArrayOutputStream();
        gridFSBucket.downloadToStream(new ObjectId(dbDocument.getRawFileId()),doc);
        ByteArrayOutputStream pdf=new ByteArrayOutputStream();
        gridFSBucket.downloadToStream(new ObjectId(dbDocument.getPdfFileId()),pdf);
        return Pair.of(doc.toByteArray(),pdf.toByteArray());
    }

    @Value
    private static class AggregationResult{
        double _id;
        double objects;
    }

    @SneakyThrows
    public List<Pair<Integer,Double>> getImagesAndPages(Query query){
        List<AggregationResult> rawResults=new ArrayList<>();
        collection.aggregate(
                Arrays.asList(
                        Aggregates.match(queryToFilter(query)),
                        Aggregates.project(
                                Projections.fields(
                                        Projections.include("pageCount"),
                                        Projections.computed("documentObjects", Document.parse("{ \n" +
                                                "            $filter: { input: \"$documentObjects\",  as: 'd',  cond: {$eq: [\"$$d.type\", \"Picture\"]} } \n" +
                                                "        } "))
                                )
                        ),
                        Aggregates.project(
                                Projections.fields(
                                        Projections.include("pageCount"),
                                        Projections.computed("pictures",new Document("$size", "$documentObjects" ))
                                )
                        ),
                        Aggregates.group("$pageCount", Accumulators.avg("objects","$pictures"))
                )
        ).forEach((Consumer<Document>) doc -> rawResults.add(GSON.fromJson(doc.toJson(),AggregationResult.class)));

        return rawResults.stream()
                .map(r->Pair.of((int)r.get_id(),r.getObjects()))
                .sorted(Comparator.comparing(Pair::getKey))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public List<Pair<Integer,Double>> getTablesAndPages(Query query){
        List<AggregationResult> rawResults=new ArrayList<>();
        collection.aggregate(
                Arrays.asList(
                        Aggregates.match(queryToFilter(query)),
                        Aggregates.project(
                                Projections.fields(
                                        Projections.include("pageCount"),
                                        Projections.computed("documentObjects", Document.parse("{ \n" +
                                                "            $filter: { input: \"$documentObjects\",  as: 'd',  cond: {$eq: [\"$$d.type\", \"Table\"]} } \n" +
                                                "        } "))
                                )
                        ),
                        Aggregates.project(
                                Projections.fields(
                                        Projections.include("pageCount"),
                                        Projections.computed("tables",new Document("$size", "$documentObjects" ))
                                )
                        ),
                        Aggregates.group("$pageCount", Accumulators.avg("objects","$tables"))
                )
        ).forEach((Consumer<Document>) doc -> rawResults.add(GSON.fromJson(doc.toJson(),AggregationResult.class)));

        return rawResults.stream()
                .map(r->Pair.of((int)r.get_id(),r.getObjects()))
                .sorted(Comparator.comparing(Pair::getKey))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public static void main(String[] args) {
        //ParsedDocument document= DocumentConverter.importFromDoc("dsp.docx", FileUtills.readAllBytes("documents/TsOS_lab_1.docx"));
        MongoDB db=new MongoDB();

        db.topNFulltextSearch("large",2).getLeft()
                .forEach(d-> System.out.println(d.getName()));

        db.topNFulltextSearch("алгоритм",2).getRight()
                .forEach(t-> System.out.println(t));

        //System.out.println(db.getImagesAndPages(Query.builder().build()));
        //System.out.println(db.getTablesAndPages(Query.builder().build()));
        //db.addDocument(document);

        //List<DbDocument> dbDocuments=db.loadDocuments(Query.builder().limit(1).build());
        //dbDocuments.stream()
        //        .forEach(d->System.out.println(d));

        /*System.out.println(
                db.loadDocuments(
                        Query.builder()
                                .findString("лист")
                                .findMode(Query.FindMode.EVERYWHERE)
                                .minSize(2522630)
                                .maxSize(2522635)
                                .format(Query.DocFormat.EXACT_STRUCTURE)
                                .build()
                )
                .stream()
                .map(DbDocument::getParagraphs)
                .map(p->p.stream().map(Paragraph::getName).collect(Collectors.toList()))
                .collect(Collectors.toList())
        );*/

        //db.importFromZip("export.zip");
    }
}