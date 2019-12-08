package nosql.docdb;

import lombok.SneakyThrows;
import nosql.docdb.database.MongoDB;
import nosql.docdb.web_application.ServletServer;


public class Application {
    @SneakyThrows
    public static void main(String[] args) {
        if(args.length==2&&args[0].equals("import")){
            new MongoDB().importFromZip(args[1]);
        }else {
            ServletServer.startServer();
        }
    }
}
