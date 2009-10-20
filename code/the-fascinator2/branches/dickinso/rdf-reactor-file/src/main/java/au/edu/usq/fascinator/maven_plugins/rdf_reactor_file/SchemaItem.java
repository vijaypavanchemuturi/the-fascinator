package au.edu.usq.fascinator.maven_plugins.rdf_reactor_file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.Reasoning;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdfreactor.generator.CodeGenerator;

/**
 * @author dickinso
 * 
 */
public class SchemaItem {

	private Model model = RDF2Go.getModelFactory().createModel();

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
	 * A list of supporting URLs that are aggregated with the main schema.
	 * 
	 * @parameter
	 */
	private HashSet<URL> schemaUrlLibrary;

	/**
	 * A list of supporting filess that are aggregated with the main schema.
	 * 
	 * @parameter
	 */
	private HashSet<File> schemaFileLibrary;

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
	 * @return
	 */
	public HashSet<File> getSchemaFileLibrary() {
		return schemaFileLibrary;
	}

	/**
	 * @param workingDirectory
	 * @return
	 * @throws IOException
	 */
	private Model loadSchema(File workingDirectory) throws IOException {
		if (schemaUrlLibrary != null) {
			for (URL schema : schemaUrlLibrary) {
				log.info("Schema requested from " + schema.toExternalForm());
				File rdfs = downloadSchema(schema, getSchemaFilePath(
						workingDirectory, schema));
				model.readFrom(new FileReader(rdfs));
			}
		}

		if (schemaFileLibrary != null) {
			for (File schema : schemaFileLibrary) {
				log.info("Schema requested from " + schema.getAbsolutePath());
				model.readFrom(new FileReader(schema));
			}
		}

		return model;
	}

	private String getSchemaFilePath(File workingDirectory, URL url) {
		String rdfsFile = workingDirectory.getPath() + "\\"
				+ schemaName.replaceAll(" ", "_") + "\\"
				+ url.toExternalForm().replaceAll("\\W", "_") + ".rdfs";
		return rdfsFile;
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
		model.open();
		this.log = mavenLogger;
		// Check to make sure that we have a file or a URL
		if (schemaUrlLibrary == null && schemaFileLibrary == null) {
			throw new MojoFailureException(this, "Schema file error",
					"No locations (file or URL given for " + this.schemaName);
		}

		Model workingSchemaModel = null;
		try {
			workingSchemaModel = loadSchema(workingDirectory);
		} catch (IOException e) {
			throw new MojoFailureException(e, "Schema access error",
					"Could not access the requested schema for " + schemaName);
		}
		log.info("Generating code for " + schemaName
				+ ". Classes will be in package " + packageName
				+ " and with method prefix " + methodPrefix
				+ ". skipBuiltins is " + skipBuiltins + ".");

		try {
			CodeGenerator.generate(workingSchemaModel, outputDirectory,
					packageName, Reasoning.rdfs, skipBuiltins, methodPrefix);
		} catch (Exception e) {
			throw new MojoFailureException(e, "RDFS processing error",
					"Could not generate code from the specified RDF schema file.");
		} finally {
			model.close();
		}

	}
}
