package au.edu.usq.fascinator.maven_plugins.rdf_reactor_file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.ontoware.rdf2go.Reasoning;
import org.ontoware.rdfreactor.generator.CodeGenerator;

/**
 * @author dickinso
 * 
 */
public class SchemaItem {

	/**
	 * Just a logger
	 */
	private Log log = null;

	/**
	 * 
	 * 
	 * @parameter
	 * @required
	 */
	private String schemaName;

	/**
	 * The URL of the RDF Schema.
	 * 
	 * @parameter
	 */
	private URL url;

	/**
	 * A local file containing the RDF Schema
	 * 
	 * @parameter
	 */
	private File file;

	/**
	 * A list of supporting URLs that are aggregated with the main schema.
	 * 
	 * @parameter
	 */
	private HashSet<URL> schemaUrlLibrary;

	/**
	 * 
	 * 
	 * @parameter
	 * @required
	 */
	private String packageName;

	/**
	 * 
	 * 
	 * @parameter
	 */
	private String methodPrefix;

	/**
	 * @return
	 */
	public String getSchemaName() {
		return schemaName;
	}

	/**
	 * @return
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * @return
	 */
	public File getFile() {
		return this.file;
	}

	/**
	 * @return
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return
	 */
	public String getMethodPrefix() {
		return methodPrefix;
	}

	/**
	 * @return
	 */
	public HashSet<URL> getSchemaUrlLibrary() {
		return schemaUrlLibrary;
	}

	/**
	 * @param workingDirectory
	 * @return
	 * @throws IOException
	 */
	private File loadSchema(File workingDirectory) throws IOException {
		if (file != null) {
			log.info("Schema requested from " + file.getAbsolutePath());
			return file;
		} else if (url != null) {
			
			String rdfsFile = workingDirectory.getPath() + "\\"
					+ schemaName.replaceAll(" ", "_") + "\\"
					+ url.toExternalForm().replaceAll("\\W", "_") 
					+ ".rdfs";
			log.info("Schema requested from " + url.toExternalForm()
					+ " will be downloaded to " + rdfsFile);

			return downloadSchema(url, rdfsFile);

		}
		return null;
	}

	private File downloadSchema(URL url, String outputFileName)
			throws IOException {
		String inputLine;
		BufferedReader in = null;
		BufferedWriter out = null;
		URLConnection connection = null;

		File schemaFile = new File(outputFileName);
		schemaFile.getParentFile().mkdirs();

		try {
			log
					.info("Accessing " + url.toExternalForm() + " for "
							+ schemaName);
			connection = url.openConnection();
			connection.addRequestProperty("accept", "application/rdf+xml");
			connection.connect();
		} catch (IOException e) {
			throw new IOException("Failed to access requested URL ("
					+ url.toExternalForm() + ") for " + schemaName, e);
		}
		try {
			try {

				in = new BufferedReader(new InputStreamReader(connection
						.getInputStream()));
			} catch (IOException e) {
				throw new IOException("Failed to read from the requested URL ("
						+ url.toExternalForm() + ") for " + schemaName, e);
			}

			try {
				out = new BufferedWriter(new FileWriter(outputFileName));
			} catch (IOException e) {
				throw new IOException("Failed to create the output file "
						+ outputFileName + " for the requested URL ("
						+ url.toExternalForm() + ") for " + schemaName, e);
			}

			try {
				while ((inputLine = in.readLine()) != null) {
					out.append(inputLine + "\n");
				}
			} catch (IOException e) {
				throw new IOException("Failed to create a local version of ("
						+ url.toExternalForm() + ") for " + schemaName, e);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		}
		return schemaFile;
	}

	/**
	 * @param skipBuiltins
	 * @param workingDirectory
	 * @param outputDirectory
	 * @param rdfReactorLogfile
	 * @param mavenLogger
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	protected void generateCode(boolean skipBuiltins, File workingDirectory,
			File outputDirectory, File rdfReactorLogfile, Log mavenLogger)
			throws MojoExecutionException, MojoFailureException {

		this.log = mavenLogger;
		// Check to make sure that we have a file or a URL
		if (url == null && file == null) {
			throw new MojoFailureException(this, "Schema file error",
					"No location (file or URL given for " + this.schemaName);
		}
		if (url != null && file != null) {
			log.info("A file and a URL was provided for " + this.schemaName
					+ " so I'll use the file - only one should be provided");
		}

		File workingSchemaFile = null;
		try {
			workingSchemaFile = loadSchema(workingDirectory);
		} catch (IOException e) {
			throw new MojoFailureException(e, "Schema access error",
					"Could not access the requested schema for " + schemaName);
		}
		log.info("Generating code for " + schemaName + " from RDF schema file "
				+ workingSchemaFile + " into dir " + outputDirectory
				+ ". Classes will be in package " + packageName
				+ " and with method prefix " + methodPrefix
				+ ". skipBuiltins is " + skipBuiltins + ".");

		try {
			CodeGenerator.generate(workingSchemaFile.getAbsolutePath(),
					outputDirectory.getAbsolutePath(), packageName,
					Reasoning.rdfs, skipBuiltins, methodPrefix);
		} catch (Exception e) {
			throw new MojoFailureException(e, "RDFS processing error",
					"Could not generate code from the specified RDF schema file.");
		}

	}
}
