package au.edu.usq.fascinator.maven_plugins.rdf_reactor_file;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.SimpleLayout;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Does some stuff
 * 
 * Note: curl  -H "Accept: application/rdf+xml" http://www.semanticdesktop.org/ontologies/nie/
 * 
 * @goal rdfs
 * @phase generate-sources
 */
public class RDFSGenerator extends AbstractMojo {

	/**
	 * Path to the file or directory containing the RDF Schemas to process.
	 * 
	 * @parameter
	 * @required
	 */
	private List schemaList;

	/**
	 * The directory where generated java code shall be written to.
	 * 
	 * @parameter 
	 *            expression="${project.build.directory}/generated-sources/rdfs-classes"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Specifies whether implicitly existing RDF classes (contained in RDF or
	 * RDFS) shall be generated as well (false) or not (true).
	 * 
	 * @parameter default-value="true"
	 */
	private boolean skipBuiltins;
	

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
		
		

		// add generated code to list of files to be compiled
		if (project != null) {
			project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
		}
	}
}
