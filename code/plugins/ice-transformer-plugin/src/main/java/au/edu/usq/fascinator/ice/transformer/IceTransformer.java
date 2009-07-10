/* 
 * The Fascinator - Transformer Library
 * Copyright (C) 2008 University of Southern Queensland
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package au.edu.usq.fascinator.ice.transformer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.IOUtils;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.transformer.Transformer;
import au.edu.usq.fascinator.api.transformer.TransformerException;
import au.edu.usq.fascinator.common.JsonConfig;

/**
 * Transformer Class will send a file to ice-service to get the renditions of
 * the file NOTE: this class can be use without The Fascinator
 * 
 * @author Linda Octalina
 */

public class IceTransformer implements Transformer {

    private String convertUrl;
    private String outputPath;
    
    public IceTransformer() {
        /**
         * Transformer constructor
         */
        this.convertUrl = "http://ice-service.usq.edu.au/api/convert/";
    }
    
    public IceTransformer(String convertUrl, String outputPath) {
        /**
         * Transformer constructor
         * 
         * @param String convertUrl
         * @param String outputPath
         */
    	this.outputPath = outputPath;
        this.convertUrl = convertUrl;
        if (convertUrl == "" || convertUrl == null)
            this.convertUrl = "http://ice-service.usq.edu.au/api/convert/";
    }

    public boolean getRendition(String fileName) {
        /**
         * getRendition method
         * 
         * @param String fileName
         * @return boolean: true if success false if fail
         */
        File sourceFile = new File(fileName);
        if (sourceFile.exists()) {
            String extResult = getExtension(sourceFile);
            if (!extResult.startsWith("Error")) {
                this.convertUrl = this.convertUrl + extResult;
                String result = getRendition(sourceFile);
                if (!result.startsWith("Error"))
                    return true;
            }
        }
        System.out.println("File: " + fileName + " is not Exist");
        return false;
    }

    public String getRendition(File sourceFile) {
        /**
         * getRendition method
         * 
         * @param File sourceFile
         * @return String: filePath if success error message if fail
         */
        System.out.println("fileName: " + sourceFile.getAbsolutePath() + "outputPath: " + this.outputPath);
        try {
            HttpClient client = new HttpClient();
            PostMethod filePost = new PostMethod(this.convertUrl);
            Part[] parts = { new StringPart("zip", "1"),
                    new StringPart("toc", "1"), new StringPart("pdfLink", "1"),
                    new StringPart("pathext", ""),
                    new StringPart("template", getTemplate()),
                    new FilePart("file", sourceFile) };

            filePost.setRequestEntity(new MultipartRequestEntity(parts,
                    filePost.getParams()));
            client.executeMethod(filePost);

            // Get Response
            InputStream is = filePost.getResponseBodyAsStream();
            String[] filePart = sourceFile.getName().split("\\.");
            System.out.println("outputPath: " + this.outputPath + ", sourceFile: " + sourceFile.getName());
            String outputFilename = this.outputPath + "/" + filePart[0] + ".zip";
            FileOutputStream fos = new FileOutputStream(outputFilename);
            IOUtils.copy(is, fos);

            is.close();
            fos.close();
            return outputFilename; // Returning the location of the file
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getClass());
            // e.printStackTrace();
            return "Error " + e.getClass();
        }
    }

    private String getExtension(File fileObject) {
        /**
         * getExtension method
         * 
         * @param File fileObject
         * @return String
         */
        String[] parts = fileObject.getName().split("\\.");
        String ext = "";
        if (parts.length == 2) {
            ext = parts[1];
            return ext;
        }
        return "Error: Unable to detect file extension properly!";
    }

    private String getTemplate() {
        /**
         * getTemplate method
         * 
         * @return String
         */
        String template = "<html>"
                + "<head>"
                + "<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>"
                + "<title>Default Template</title>" + "<style type='text/css'>"
                + ".rendition-links { text-align: right; }"
                + ".body table td { vertical-align: top; }" + "</style>"
                + "<style class='sub style-css' type='text/css'></style>"
                + "</head>" + "<body>" + "<div class='rendition-links'>"
                + "<span class='ins source-link'></span>"
                + "<span class='ins slide-link'></span>"
                + "<span class='ins pdf-rendition-link'></span>" + "</div>"
                + "<h1 class='ins title'></h1>"
                + "<div class='ins page-toc'></div>"
                + "<div class='ins body'></div>" + "</body>" + "</html>";
        return template;
    }

    @Override
    public File transform(File in) throws TransformerException {

        String zipPath = getRendition(in);
        return new File(zipPath);

    }

    @Override
    public String getId() {
        return "ice-transformer";
    }

    @Override
    public String getName() {
        return "ICE Transformer";
    }

    @Override
    public void init(File jsonFile) throws PluginException {
        try {
            JsonConfig config = new JsonConfig(jsonFile);
            this.outputPath = config.get("transformer/ice/outputPath", "/tmp");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() throws PluginException {
    }
}