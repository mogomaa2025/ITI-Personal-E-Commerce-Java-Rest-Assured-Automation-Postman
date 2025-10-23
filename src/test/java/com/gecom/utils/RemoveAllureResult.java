package com.gecom.utils;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;


public class RemoveAllureResult {

    public static void deleteFolder(String folderName) throws IOException {
        FileUtils.deleteDirectory(new File(folderName));
    }


}
