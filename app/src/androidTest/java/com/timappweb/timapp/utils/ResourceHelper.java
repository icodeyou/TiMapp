package com.timappweb.timapp.utils;

import com.timappweb.timapp.ResizePictureTest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Stephane on 23/09/2016.
 */
public class ResourceHelper {

    public static <T> File getFile(T test, String path) throws IOException {
        InputStream in = test.getClass().getClassLoader().getResourceAsStream(path);
        return IOUtil.toFile(in, "tmp", "tmp");
    }

}
