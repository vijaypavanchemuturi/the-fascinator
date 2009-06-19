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
package au.edu.usq.fascinator.harvester.extractor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.Set;

import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.semanticdesktop.aperture.extractor.ExtractorException;
import org.semanticdesktop.aperture.extractor.ExtractorFactory;
import org.semanticdesktop.aperture.extractor.ExtractorRegistry;
import org.semanticdesktop.aperture.extractor.impl.DefaultExtractorRegistry;
import org.semanticdesktop.aperture.rdf.RDFContainer;
import org.semanticdesktop.aperture.rdf.impl.RDFContainerImpl;
import org.semanticdesktop.aperture.vocabulary.NIE;

/*
 * This class is heavily based on the tutorial at
 * http://aperture.wiki.sourceforge.net/Extractors
 */

public class Extractor {
	private String filePath = "";

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
		try {
			RDFContainer rdf = extractRDF(args[0]);
			if (rdf != null)
				System.out.println(rdf.getModel().serialize(Syntax.RdfXml));
			else
				System.out.println("Cannot locate file");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("EXCEPTION");
			e.printStackTrace();
		}
	}

	public static RDFContainer extractRDF(String file) throws IOException,
			ExtractorException, URISyntaxException {
		File f = getFile(file);
		if (f != null)
			return extractRDF(f);
		return null;
	}

	/**
	 * 
	 * The following file paths (should) work:
	 * <ul>
	 * <li>/tmp/test\ 1.txt</li>
	 * <li>file:///tmp/test%201.txt</li>
	 * </ul>
	 * 
	 * @param file
	 * @return
	 * @throws URISyntaxException
	 */
	public static File getFile(String file) throws URISyntaxException {
		// We need to see if the file path is a URL
		
		File f = null;
		try {
			URL url = new URL(file);
			if (url.getProtocol().equals("file")) {
				f = new File(url.toURI());
			} 
		} catch (MalformedURLException e) {
			// it may be c:\a\b\c or /a/b/c so it's
			// still legitimate (maybe)
			f = new File(file);
		}
		if (f == null)
			return null;
		if (!f.exists())
			return null;
		return f;
	}

	public static RDFContainer extractRDF(File file) throws IOException,
			ExtractorException {
		String mimeType = MimeType.getMimeType(file);
		return extractRDF(file, mimeType);
	}

	public static RDFContainer extractRDF(File file, String mimeType)
			throws IOException, ExtractorException {
		RDFContainer container = createRDFContainer(file);
		determineExtractor(file, mimeType, container);
		return container;
	}

	private static RDFContainer createRDFContainer(File file) {
		// create the RDFContainer that will hold the RDF model
		URI uri = new URIImpl(file.toURI().toString());
		Model model = RDF2Go.getModelFactory().createModel();
		model.open();
		return new RDFContainerImpl(model, uri);
	}

	private static void determineExtractor(File file, String mimeType,
			RDFContainer container) throws IOException, ExtractorException {
		FileInputStream stream = new FileInputStream(file);
		BufferedInputStream buffer = new BufferedInputStream(stream);
		URI uri = new URIImpl(file.toURI().toString());

		// create an ExtractorRegistry containing all available
		// ExtractorFactories
		ExtractorRegistry extractorRegistry = new DefaultExtractorRegistry();

		// determine and apply an Extractor that can handle this MIME type
		Set factories = extractorRegistry.get(mimeType);
		if (factories != null && !factories.isEmpty()) {
			// just fetch the first available Extractor
			ExtractorFactory factory = (ExtractorFactory) factories.iterator()
					.next();
			org.semanticdesktop.aperture.extractor.Extractor extractor = factory
					.get();

			// apply the extractor on the specified file
			// (just open a new stream rather than buffer the previous stream)
			stream = new FileInputStream(file);
			buffer = new BufferedInputStream(stream, 8192);
			extractor.extract(uri, buffer, null, mimeType, container);
			stream.close();
		}
		// add the MIME type as an additional statement to the RDF model
		container.add(NIE.mimeType, mimeType);

		// container.getModel().writeTo(new PrintWriter(System.out),
		// Syntax.Ntriples);
	}

	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return this.filePath;
	}

}
