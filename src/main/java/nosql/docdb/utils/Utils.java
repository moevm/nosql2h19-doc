package nosql.docdb.utils;


public class Utils {
    public static void safe(Action action){
        try {
            action.run();
        }catch (Exception ignored){

        }
    }

    public interface Action{
        void run() throws Exception;
    }
}
