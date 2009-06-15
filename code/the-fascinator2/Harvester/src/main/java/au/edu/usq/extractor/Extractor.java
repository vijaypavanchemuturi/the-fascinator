/*
 * The Fascinator
 * Copyright (C) 2009  University of Southern Queensland
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
package au.edu.usq.extractor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.Set;
 
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.semanticdesktop.aperture.extractor.Extractor;
import org.semanticdesktop.aperture.extractor.ExtractorFactory;
import org.semanticdesktop.aperture.extractor.ExtractorRegistry;
import org.semanticdesktop.aperture.extractor.impl.DefaultExtractorRegistry;
import org.semanticdesktop.aperture.mime.identifier.MimeTypeIdentifier;
import org.semanticdesktop.aperture.mime.identifier.magic.MagicMimeTypeIdentifier;
import org.semanticdesktop.aperture.rdf.RDFContainer;
import org.semanticdesktop.aperture.rdf.impl.RDFContainerImpl;
import org.semanticdesktop.aperture.util.IOUtil;
import org.semanticdesktop.aperture.vocabulary.NIE;


/*
 * This class is heavily based on the tutorial at
 * http://aperture.wiki.sourceforge.net/Extractors
 */

public class Extractor {
	String filePath = "";

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// check if a commandline argument was specified
		if (args.length == 0) {
			System.err.println("Extractor\nUsage: java Extractor <file>");
			System.exit(-1);
		}
		Extractor extractor = new Extractor(args[0]);
	}

	public Extractor(String filePath) {
		this.filePath = filePath;
	}

	public void extract() {
		// create a stream of the specified file
		File file = new File(filePath);
		String mimeType = getMimeType(file);

		 // create the RDFContainer that will hold the RDF model
        URI uri = new URIImpl(file.toURI().toString());
        Model model = RDF2Go.getModelFactory().createModel();
        model.open();
        RDFContainer container = applyExtractor(model,uri);
    }

	private RDFContainer applyExtractor(Model model, URI uri) {
		RDFContainer container = new RDFContainerImpl(model, uri);
        // determine and apply an Extractor that can handle this MIME type
        Set factories = extractorRegistry.get(mimeType);
        if (factories != null && !factories.isEmpty()) {
            // just fetch the first available Extractor
            ExtractorFactory factory = (ExtractorFactory) factories.iterator().next();
            Extractor extractor = factory.get();
 
            // apply the extractor on the specified file
            // (just open a new stream rather than buffer the previous stream)
            FileInputStream stream = new FileInputStream(file);
            BufferedInputStream buffer = new BufferedInputStream(stream, 8192);
            extractor.extract(uri, buffer, null, mimeType, container);
            stream.close();
        }
        // add the MIME type as an additional statement to the RDF model
        container.add(NIE.mimeType, mimeType);
        // report the output to System.out
        container.getModel().writeTo(new PrintWriter(System.out),Syntax.Ntriples);
	}
	
	private String getMimeType(File file) {
		FileInputStream stream;
		try {
			stream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// create a MimeTypeIdentifier
		MimeTypeIdentifier identifier = new MagicMimeTypeIdentifier();
		// create an ExtractorRegistry containing all available
		// ExtractorFactories
		ExtractorRegistry extractorRegistry = new DefaultExtractorRegistry();
		// read as many bytes of the file as desired by the MIME type identifier
		
		BufferedInputStream buffer = new BufferedInputStream(stream);
		byte[] bytes = IOUtil.readBytes(buffer, identifier.getMinArrayLength());
		stream.close();
		// let the MimeTypeIdentifier determine the MIME type of this file
		String mimeType = identifier.identify(bytes, file.getPath(), null);
		// skip when the MIME type could not be determined
		if (mimeType == null) {
			System.err.println("MIME type could not be established.");
			return;
		}
		return mimeType;
	}

	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return this.filePath;
	}

}
