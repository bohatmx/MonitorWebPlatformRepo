/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boha.monitor.utilx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to expose system properties on file
 *
 * @author Aubrey Malabie
 */
public class MonitorProperties {

    public static File getTemporaryDir() {
        getProperties();
        File d = new File(props.getProperty("tempDir"));
        if (!d.exists()) {
            d.mkdir();
        }
        return d;
    }

    public static File getTemporaryZippedDir() {
        getProperties();
        File d = new File(props.getProperty("zipped"));
        if (!d.exists()) {
            d.mkdir();
        }
        return d;
    }

    public static File getDocumentDir() {
        getProperties();
        File d = new File(props.getProperty("documents"));
        if (!d.exists()) {
            d.mkdir();
        }
        return d;
    }

    public static File getLogDir() {
        getProperties();
        File d = new File(props.getProperty("logs"));
        if (!d.exists()) {
            d.mkdir();
        }
        return d;
    }

    private static void getProperties() {
        if (props != null) {
            return;
        }
        props = new Properties();
        try {
            File f = null;
            f = new File("/work/properties/monitor.properties");
            if (!f.exists()) {
                f = new File("/opt/properties/monitor.properties");
            }
            if (!f.exists()) {
                logger.log(Level.SEVERE, "Monitor Properties File not found");
            } else {
                logger.log(Level.INFO, "Monitor Properties: {0}\n\n\n", f.getAbsolutePath());
                props.load(new FileInputStream(f));
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Properties file monitor.properties not found or corrupted");
        }

    }
    private static final Logger logger = Logger.getLogger(MonitorProperties.class.getName());
    private static Properties props;
}
