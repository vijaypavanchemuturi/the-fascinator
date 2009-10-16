package au.edu.usq.fascinator.maven_plugins.rdf_reactor_file;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.SimpleLayout;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.ontoware.rdf2go.Reasoning;
import org.ontoware.rdfreactor.generator.CodeGenerator;

/**
 * Does some stuff
 * 
 * Note: curl  -H "Accept: application/rdf+xml" http://www.semanticdesktop.org/ontologies/nie/
 * 
 * @goal rdfs
 * @phase generate-sources
 */
//public class RDFSGeneratorOld extends AbstractMojo {
public class RDFSGeneratorOld extends AbstractMojo {
	/**
	 * Path to the file or directory containing the RDF Schemas to process.
	 * 
	 * @parameter
	 * @required
	 */
	private File schemaFile;

	/**
	 * The directory where generated java code shall be written to.
	 * 
	 * @parameter 
	 *            expression="${project.build.directory}/generated-sources/rdfs-classes"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Package that the generated classes shall belong to.
	 * 
	 * @parameter expression="${project.groupId}"
	 * @required
	 */
	private String packageName;

	/**
	 * Specifies whether implicitly existing RDF classes (contained in RDF or
	 * RDFS) shall be generated as well (false) or not (true).
	 * 
	 * @parameter default-value="true"
	 */
	private boolean skipBuiltins;
	
	/**
	 * Notes
	 * 
	 * @parameter default-value="true"
	 */
	private boolean mergeFiles;
	
	/**
	 * Notes
	 * 
	 * @parameter default-value="false"
	 */
	private boolean singlePackage;

	/**
	 * A prefix that will be included in all method names. This setting is
	 * useful to avoid surprising name collisions with pre-defined methods which
	 * would render the generated code incompatible. A prefix is preferably
	 * short and starts with a capital latter.
	 * 
	 * @parameter default-value=""
	 */
	private String methodPrefix;

	/**
	 * The Maven Project Object
	 * 
	 * @parameter expression="${project}"
	 * @required
	 */
	private MavenProject project;

	/**
	 * Log file location of RDFReactor
	 * 
	 * @parameter default-value="target/rdfreactor.log"
	 * @readonly
	 */
	private File rdfReactorLogfile;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			// make sure that directory for log file exists.
			rdfReactorLogfile.getParentFile().mkdirs();

			// configure logging infrastructure for RDFReactor
			FileAppender logFileAppender = new FileAppender(new SimpleLayout(),
					rdfReactorLogfile.getAbsolutePath());
			BasicConfigurator.configure(logFileAppender);

		} catch (IOException ioe) {
			throw new MojoExecutionException(
					"Cannot open log file for writing RDFReactor log messages",
					ioe);
		}
		
		if (!schemaFile.exists())
			getLog().error(
					"Schema file (" + schemaFile.getAbsolutePath()
							+ ") not found. Aborting.");
		getLog().info("Requested schemaFile: " + schemaFile.getAbsolutePath());
		// schemaFile may be a file or a directory
		if (schemaFile.isDirectory()) {
			getLog().info("Request is to process schemas in a directory");
			for (File schema : schemaFile.listFiles()) {
				generateCode(schema);
			}
		} else {
			getLog().info("Request is to process a specific schema");
			generateCode(schemaFile);
		}

		// add generated code to list of files to be compiled
		if (project != null) {
			project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
		}
	}

	private void generateCode(File schema) throws MojoExecutionException,
			MojoFailureException {

		String schemaPackage = packageName;
		if (!singlePackage) {
			//Use the filename as the package name
			schemaPackage = schemaPackage.concat(schema.getName().substring(0, schema.getName().indexOf(".")));
		}
		
		getLog().info(
				"Generating code from RDF schema file " + schema + " into dir "
						+ outputDirectory + ". Classes will be in package "
						+ schemaPackage + " and with method prefix "
						+ methodPrefix + ". skipBuiltins is " + skipBuiltins
						+ ".");
		getLog()
				.info(
						"RDFReactor's log messages are written to "
								+ rdfReactorLogfile);

		try {
			CodeGenerator.generate(schema.getAbsolutePath(), outputDirectory
					.getAbsolutePath(), schemaPackage, Reasoning.rdfs,
					skipBuiltins, methodPrefix);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MojoFailureException(e, "RDFS processing error",
					"Could not generate code from the specified RDF schema file.");
		}
	}

}
