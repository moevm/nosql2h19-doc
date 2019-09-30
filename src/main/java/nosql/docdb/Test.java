package nosql.docdb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Value;
import org.bson.Document;

public class Test {
    public static void main(String[] args) {
        Gson gson=new GsonBuilder().setPrettyPrinting().create();
        MongoClient mongoClient = MongoClients.create("mongodb://localhost");
        MongoDatabase database = mongoClient.getDatabase("mydb");
        MongoCollection<Document> collection = database.getCollection("test");

        SimpleDocument document=new SimpleDocument("Вася","Дневник",100);


        collection.insertOne(Document.parse(gson.toJson(document)));

        SimpleDocument documentInBase=gson.fromJson(collection.find().first().toJson(),SimpleDocument.class);

        System.out.println(documentInBase);
    }

    @Value
    public static class SimpleDocument{
        String author;
        String title;
        long length;
    }
}
