package com.timappweb.timapp.utils;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Stephane on 23/09/2016.
 */
public class IOUtil {

    public static File toFile(InputStream stream, String filename, String ext) throws IOException {
        File file = File.createTempFile(filename, ext);
        file.deleteOnExit();
        copy(stream, file);
        return file;
    }

    private static void copy(InputStream input, File file) throws IOException {
        try {
            OutputStream output = new FileOutputStream(file);
            try {
                try {
                    byte[] buffer = new byte[4 * 1024]; // or other buffer size
                    int read;

                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                    output.flush();
                } finally {
                    output.close();
                }
            } catch (Exception e) {
                e.printStackTrace(); // handle exception, define IOException and others
            }
        } finally {
            input.close();
        }
    }


}
