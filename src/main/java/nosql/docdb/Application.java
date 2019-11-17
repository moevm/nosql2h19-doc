package nosql.docdb;

import lombok.Value;
import nosql.docdb.web_application.ServletServer;

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
