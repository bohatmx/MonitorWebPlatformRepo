/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boha.monitor.utilx;

/**
 *
 * @author aubreymalabie
 */
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

public class FileUtility {

    private static final Logger logger = Logger.getLogger(FileUtility.class.getName());

    public static File getFile(String data) {
        Random rand = new Random(System.currentTimeMillis());
        //TODO restore after zip testing
        File dir = MonitorProperties.getTemporaryDir();
        File file = new File(dir, "x" + System.currentTimeMillis() + "_" + rand.nextInt(999999999) + ".txt");
        try {
            FileUtils.writeStringToFile(file, data);
        } catch (IOException ex) {
            Logger.getLogger(FileUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return file;
    }
    
  
}
