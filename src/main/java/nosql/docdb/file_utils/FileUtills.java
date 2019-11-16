package nosql.docdb.file_utils;

import lombok.SneakyThrows;

import java.io.*;
import java.util.Arrays;

public class FileUtills {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

    @SneakyThrows
    public static void copyFiles(String from, String to){
        try(FileInputStream fis=new FileInputStream(from); FileOutputStream fos=new FileOutputStream(to)) {
            fos.write(readAllBytes(fis));
        }
    }

    public static byte[] readAllBytes(String path) throws IOException {
        try(FileInputStream fis=new FileInputStream(path)) {
            return readAllBytes(fis);
        }
    }

    // java 9 source
    public static byte[] readAllBytes(InputStream is) throws IOException {
        byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
        int capacity = buf.length;
        int nread = 0;
        int n;
        for (;;) {
            // read to EOF which may read more or less than initial buffer size
            while ((n = is.read(buf, nread, capacity - nread)) > 0)
                nread += n;

            // if the last call to read returned -1, then we're done
            if (n < 0)
                break;

            // need to allocate a larger buffer
            if (capacity <= MAX_BUFFER_SIZE - capacity) {
                capacity = capacity << 1;
            } else {
                if (capacity == MAX_BUFFER_SIZE)
                    throw new OutOfMemoryError("Required array size too large");
                capacity = MAX_BUFFER_SIZE;
            }
            buf = Arrays.copyOf(buf, capacity);
        }
        return (capacity == nread) ? buf : Arrays.copyOf(buf, nread);
    }
}