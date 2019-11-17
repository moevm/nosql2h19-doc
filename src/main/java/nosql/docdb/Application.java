package nosql.docdb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Value;
import nosql.docdb.web_application.ServletServer;
import org.bson.Document;

public class Application {
    public static void main(String[] args) {
        ServletServer.startServer();
    }

    @Value
    public static class SimpleDocument{
        String author;
        String title;
        long length;
    }

}
