/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU Affero General Public License for more details.  
 *  
 * You should have received a copy of the GNU Affero General Public License  
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.server.viewers;

import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FileConverter {

    private final static String ooHome;
    private final static int ooPort;

    private static final String PROPERTIES_FILE = "/com/docdoku/server/viewers/conf.properties";
    private static final String OO_HOME_KEY = "com.docdoku.server.viewers.ooHome";
    private static final String OO_PORT_KEY = "com.docdoku.server.viewers.ooPort";

    static {
        try {
            Properties properties = new Properties();
            properties.load(FileConverter.class.getResourceAsStream(PROPERTIES_FILE));
            ooHome = properties.getProperty(OO_HOME_KEY);
            ooPort = Integer.parseInt(properties.getProperty(OO_PORT_KEY));
        } catch (IOException e) {
            throw new RuntimeException("Can't read conf.properties file for documents conversion", e);
        }
    }

    public static InputStream convertToPDF(String sourceName, final InputStream streamToConvert) throws IOException {
        File tmpDir = com.google.common.io.Files.createTempDir();
        File fileToConvert = new File(tmpDir, sourceName);

        Files.copy(new InputSupplier<InputStream>() {
            @Override
            public InputStream getInput() throws IOException {
                return streamToConvert;
            }
        }, fileToConvert);

        File pdfFile = convertToPDF(fileToConvert);

        //clean-up
        tmpDir.deleteOnExit();

        return new FileInputStream(pdfFile);
    }

    private static File convertToPDF(File fileToConvert) {
        File pdfFile = new File(fileToConvert.getParentFile(), "converted.pdf");

        OfficeManager officeManager = new DefaultOfficeManagerConfiguration()
                .setOfficeHome(new File(ooHome))
                .setPortNumber(ooPort)
                .buildOfficeManager();
        officeManager.start();
        OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
        converter.convert(fileToConvert, pdfFile);
        officeManager.stop();

        return pdfFile;
    }

}
