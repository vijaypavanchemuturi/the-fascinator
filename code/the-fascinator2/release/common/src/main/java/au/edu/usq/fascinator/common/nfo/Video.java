/**
 * generated by http://RDFReactor.semweb4j.org ($Id: CodeGenerator.java 1535 2008-09-09 15:44:46Z max.at.xam.de $) on 15/09/09 11:49 AM
 */
package au.edu.usq.fascinator.common.nfo;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.node.BlankNode;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.ontoware.rdfreactor.runtime.Base;
import org.ontoware.rdfreactor.runtime.ReactorResult;

/**
 * This class manages access to these properties:
 * <ul>
 *   <li> FrameCount </li>
 *   <li> FrameRate </li>
 * </ul>
 *
 * This class was generated by <a href="http://RDFReactor.semweb4j.org">RDFReactor</a> on 15/09/09 11:49 AM
 */
public class Video extends Visual {

    /** http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#Video */
    @SuppressWarnings("hiding")
	public static final URI RDFS_CLASS = new URIImpl("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#Video", false);

    /** http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#frameCount */
    @SuppressWarnings("hiding")
	public static final URI FRAMECOUNT = new URIImpl("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#frameCount",false);

    /** http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#frameRate */
    @SuppressWarnings("hiding")
	public static final URI FRAMERATE = new URIImpl("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#frameRate",false);

    /** 
     * All property-URIs with this class as domain.
     * All properties of all super-classes are also available. 
     */
    @SuppressWarnings("hiding")
    public static final URI[] MANAGED_URIS = {
      new URIImpl("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#frameCount",false),
      new URIImpl("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#frameRate",false) 
    };


	// protected constructors needed for inheritance
	
	/**
	 * Returns a Java wrapper over an RDF object, identified by URI.
	 * Creating two wrappers for the same instanceURI is legal.
	 * @param model RDF2GO Model implementation, see http://rdf2go.semweb4j.org
	 * @param classURI URI of RDFS class
	 * @param instanceIdentifier Resource that identifies this instance
	 * @param write if true, the statement (this, rdf:type, TYPE) is written to the model
	 *
	 * [Generated from RDFReactor template rule #c1] 
	 */
	protected Video ( Model model, URI classURI, org.ontoware.rdf2go.model.node.Resource instanceIdentifier, boolean write ) {
		super(model, classURI, instanceIdentifier, write);
	}

	// public constructors

	/**
	 * Returns a Java wrapper over an RDF object, identified by URI.
	 * Creating two wrappers for the same instanceURI is legal.
	 * @param model RDF2GO Model implementation, see http://rdf2go.ontoware.org
	 * @param instanceIdentifier an RDF2Go Resource identifying this instance
	 * @param write if true, the statement (this, rdf:type, TYPE) is written to the model
	 *
	 * [Generated from RDFReactor template rule #c2] 
	 */
	public Video ( Model model, org.ontoware.rdf2go.model.node.Resource instanceIdentifier, boolean write ) {
		super(model, RDFS_CLASS, instanceIdentifier, write);
	}


	/**
	 * Returns a Java wrapper over an RDF object, identified by a URI, given as a String.
	 * Creating two wrappers for the same URI is legal.
	 * @param model RDF2GO Model implementation, see http://rdf2go.ontoware.org
	 * @param uriString a URI given as a String
	 * @param write if true, the statement (this, rdf:type, TYPE) is written to the model
	 * @throws ModelRuntimeException if URI syntax is wrong
	 *
	 * [Generated from RDFReactor template rule #c7] 
	 */
	public Video ( Model model, String uriString, boolean write) throws ModelRuntimeException {
		super(model, RDFS_CLASS, new URIImpl(uriString,false), write);
	}

	/**
	 * Returns a Java wrapper over an RDF object, identified by a blank node.
	 * Creating two wrappers for the same blank node is legal.
	 * @param model RDF2GO Model implementation, see http://rdf2go.ontoware.org
	 * @param bnode BlankNode of this instance
	 * @param write if true, the statement (this, rdf:type, TYPE) is written to the model
	 *
	 * [Generated from RDFReactor template rule #c8] 
	 */
	public Video ( Model model, BlankNode bnode, boolean write ) {
		super(model, RDFS_CLASS, bnode, write);
	}

	/**
	 * Returns a Java wrapper over an RDF object, identified by 
	 * a randomly generated URI.
	 * Creating two wrappers results in different URIs.
	 * @param model RDF2GO Model implementation, see http://rdf2go.ontoware.org
	 * @param write if true, the statement (this, rdf:type, TYPE) is written to the model
	 *
	 * [Generated from RDFReactor template rule #c9] 
	 */
	public Video ( Model model, boolean write ) {
		super(model, RDFS_CLASS, model.newRandomUniqueURI(), write);
	}

    ///////////////////////////////////////////////////////////////////
    // typing

	/**
	 * Return an existing instance of this class in the model. No statements are written.
	 * @param model an RDF2Go model
	 * @param instanceResource an RDF2Go resource
	 * @return an instance of Video  or null if none existst
	 *
	 * [Generated from RDFReactor template rule #class0] 
	 */
	public static Video  getInstance(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getInstance(model, instanceResource, Video.class);
	}

	/**
	 * Create a new instance of this class in the model. 
	 * That is, create the statement (instanceResource, RDF.type, http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#Video).
	 * @param model an RDF2Go model
	 * @param instanceResource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #class1] 
	 */
	public static void createInstance(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		Base.createInstance(model, RDFS_CLASS, instanceResource);
	}

	/**
	 * @param model an RDF2Go model
	 * @param instanceResource an RDF2Go resource
	 * @return true if instanceResource is an instance of this class in the model
	 *
	 * [Generated from RDFReactor template rule #class2] 
	 */
	public static boolean hasInstance(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.hasInstance(model, RDFS_CLASS, instanceResource);
	}

	/**
	 * @param model an RDF2Go model
	 * @return all instances of this class in Model 'model' as RDF resources
	 *
	 * [Generated from RDFReactor template rule #class3] 
	 */
	public static ClosableIterator<org.ontoware.rdf2go.model.node.Resource> getAllInstances(Model model) {
		return Base.getAllInstances(model, RDFS_CLASS, org.ontoware.rdf2go.model.node.Resource.class);
	}

	/**
	 * @param model an RDF2Go model
	 * @return all instances of this class in Model 'model' as a ReactorResult,
	 * which can conveniently be converted to iterator, list or array.
	 *
	 * [Generated from RDFReactor template rule #class3-as] 
	 */
	public static ReactorResult<? extends Video> getAllInstances_as(Model model) {
		return Base.getAllInstances_as(model, RDFS_CLASS, Video.class );
	}

    /**
	 * Remove rdf:type Video from this instance. Other triples are not affected.
	 * To delete more, use deleteAllProperties
	 * @param model an RDF2Go model
	 * @param instanceResource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #class4] 
	 */
	public static void deleteInstance(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		Base.deleteInstance(model, RDFS_CLASS, instanceResource);
	}

	/**
	 * Delete all (this, *, *), i.e. including rdf:type
	 * @param model an RDF2Go model
	 * @param resource
	 */
	public static void deleteAllProperties(Model model,	org.ontoware.rdf2go.model.node.Resource instanceResource) {
		Base.deleteAllProperties(model, instanceResource);
	}

    ///////////////////////////////////////////////////////////////////
    // property access methods


    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@94c5e7 has at least one value set 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return true if this property has at least one value
	 *
	 * [Generated from RDFReactor template rule #get0has-static] 
     */
	public static boolean hasFrameCount(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.has(model, instanceResource, FRAMECOUNT);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@94c5e7 has at least one value set 
     * @return true if this property has at least one value
	 *
	 * [Generated from RDFReactor template rule #get0has-dynamic] 
     */
	public boolean hasFrameCount() {
		return Base.has(this.model, this.getResource(), FRAMECOUNT);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@94c5e7 has the given value (maybe among other values).  
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be checked
     * @return true if this property contains (maybe among other) the given value
	 *
	 * [Generated from RDFReactor template rule #get0has-value-static] 
     */
	public static boolean hasFrameCount(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value ) {
		return Base.hasValue(model, instanceResource, FRAMECOUNT);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@94c5e7 has the given value (maybe among other values).  
	 * @param value the value to be checked
     * @return true if this property contains (maybe among other) the given value
	 *
	 * [Generated from RDFReactor template rule #get0has-value-dynamic] 
     */
	public boolean hasFrameCount( org.ontoware.rdf2go.model.node.Node value ) {
		return Base.hasValue(this.model, this.getResource(), FRAMECOUNT);
	}

     /**
     * Get all values of property FrameCount as an Iterator over RDF2Go nodes 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ClosableIterator of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get7static] 
     */
	public static ClosableIterator<org.ontoware.rdf2go.model.node.Node> getAllFrameCount_asNode(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_asNode(model, instanceResource, FRAMECOUNT);
	}
	
    /**
     * Get all values of property FrameCount as a ReactorResult of RDF2Go nodes 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a List of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get7static-reactor-result] 
     */
	public static ReactorResult<org.ontoware.rdf2go.model.node.Node> getAllFrameCount_asNode_(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_as(model, instanceResource, FRAMECOUNT, org.ontoware.rdf2go.model.node.Node.class);
	}

    /**
     * Get all values of property FrameCount as an Iterator over RDF2Go nodes 
     * @return a ClosableIterator of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get8dynamic] 
     */
	public ClosableIterator<org.ontoware.rdf2go.model.node.Node> getAllFrameCount_asNode() {
		return Base.getAll_asNode(this.model, this.getResource(), FRAMECOUNT);
	}

    /**
     * Get all values of property FrameCount as a ReactorResult of RDF2Go nodes 
     * @return a List of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get8dynamic-reactor-result] 
     */
	public ReactorResult<org.ontoware.rdf2go.model.node.Node> getAllFrameCount_asNode_() {
		return Base.getAll_as(this.model, this.getResource(), FRAMECOUNT, org.ontoware.rdf2go.model.node.Node.class);
	}
     /**
     * Get all values of property FrameCount     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ClosableIterator of $type
	 *
	 * [Generated from RDFReactor template rule #get11static] 
     */
	public static ClosableIterator<java.lang.Integer> getAllFrameCount(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll(model, instanceResource, FRAMECOUNT, java.lang.Integer.class);
	}
	
    /**
     * Get all values of property FrameCount as a ReactorResult of java.lang.Integer 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ReactorResult of $type which can conveniently be converted to iterator, list or array
	 *
	 * [Generated from RDFReactor template rule #get11static-reactorresult] 
     */
	public static ReactorResult<java.lang.Integer> getAllFrameCount_as(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_as(model, instanceResource, FRAMECOUNT, java.lang.Integer.class);
	}

    /**
     * Get all values of property FrameCount     * @return a ClosableIterator of $type
	 *
	 * [Generated from RDFReactor template rule #get12dynamic] 
     */
	public ClosableIterator<java.lang.Integer> getAllFrameCount() {
		return Base.getAll(this.model, this.getResource(), FRAMECOUNT, java.lang.Integer.class);
	}

    /**
     * Get all values of property FrameCount as a ReactorResult of java.lang.Integer 
     * @return a ReactorResult of $type which can conveniently be converted to iterator, list or array
	 *
	 * [Generated from RDFReactor template rule #get12dynamic-reactorresult] 
     */
	public ReactorResult<java.lang.Integer> getAllFrameCount_as() {
		return Base.getAll_as(this.model, this.getResource(), FRAMECOUNT, java.lang.Integer.class);
	}
 
    /**
     * Adds a value to property FrameCount as an RDF2Go node 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #add1static] 
     */
	public static void addFrameCount( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.add(model, instanceResource, FRAMECOUNT, value);
	}
	
    /**
     * Adds a value to property FrameCount as an RDF2Go node 
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #add1dynamic] 
     */
	public void addFrameCount( org.ontoware.rdf2go.model.node.Node value) {
		Base.add(this.model, this.getResource(), FRAMECOUNT, value);
	}
    /**
     * Adds a value to property FrameCount from an instance of java.lang.Integer 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #add3static] 
     */
	public static void addFrameCount(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, java.lang.Integer value) {
		Base.add(model, instanceResource, FRAMECOUNT, value);
	}
	
    /**
     * Adds a value to property FrameCount from an instance of java.lang.Integer 
	 *
	 * [Generated from RDFReactor template rule #add4dynamic] 
     */
	public void addFrameCount(java.lang.Integer value) {
		Base.add(this.model, this.getResource(), FRAMECOUNT, value);
	}
  

    /**
     * Sets a value of property FrameCount from an RDF2Go node.
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be set
	 *
	 * [Generated from RDFReactor template rule #set1static] 
     */
	public static void setFrameCount( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.set(model, instanceResource, FRAMECOUNT, value);
	}
	
    /**
     * Sets a value of property FrameCount from an RDF2Go node.
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set1dynamic] 
     */
	public void setFrameCount( org.ontoware.rdf2go.model.node.Node value) {
		Base.set(this.model, this.getResource(), FRAMECOUNT, value);
	}
    /**
     * Sets a value of property FrameCount from an instance of java.lang.Integer 
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set3static] 
     */
	public static void setFrameCount(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, java.lang.Integer value) {
		Base.set(model, instanceResource, FRAMECOUNT, value);
	}
	
    /**
     * Sets a value of property FrameCount from an instance of java.lang.Integer 
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set4dynamic] 
     */
	public void setFrameCount(java.lang.Integer value) {
		Base.set(this.model, this.getResource(), FRAMECOUNT, value);
	}
  


    /**
     * Removes a value of property FrameCount as an RDF2Go node 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove1static] 
     */
	public static void removeFrameCount( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.remove(model, instanceResource, FRAMECOUNT, value);
	}
	
    /**
     * Removes a value of property FrameCount as an RDF2Go node
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove1dynamic] 
     */
	public void removeFrameCount( org.ontoware.rdf2go.model.node.Node value) {
		Base.remove(this.model, this.getResource(), FRAMECOUNT, value);
	}
    /**
     * Removes a value of property FrameCount given as an instance of java.lang.Integer 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove3static] 
     */
	public static void removeFrameCount(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, java.lang.Integer value) {
		Base.remove(model, instanceResource, FRAMECOUNT, value);
	}
	
    /**
     * Removes a value of property FrameCount given as an instance of java.lang.Integer 
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove4dynamic] 
     */
	public void removeFrameCount(java.lang.Integer value) {
		Base.remove(this.model, this.getResource(), FRAMECOUNT, value);
	}
  
    /**
     * Removes all values of property FrameCount     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #removeall1static] 
     */
	public static void removeAllFrameCount( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		Base.removeAll(model, instanceResource, FRAMECOUNT);
	}
	
    /**
     * Removes all values of property FrameCount	 *
	 * [Generated from RDFReactor template rule #removeall1dynamic] 
     */
	public void removeAllFrameCount() {
		Base.removeAll(this.model, this.getResource(), FRAMECOUNT);
	}
     /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@1cbcc56 has at least one value set 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return true if this property has at least one value
	 *
	 * [Generated from RDFReactor template rule #get0has-static] 
     */
	public static boolean hasFrameRate(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.has(model, instanceResource, FRAMERATE);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@1cbcc56 has at least one value set 
     * @return true if this property has at least one value
	 *
	 * [Generated from RDFReactor template rule #get0has-dynamic] 
     */
	public boolean hasFrameRate() {
		return Base.has(this.model, this.getResource(), FRAMERATE);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@1cbcc56 has the given value (maybe among other values).  
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be checked
     * @return true if this property contains (maybe among other) the given value
	 *
	 * [Generated from RDFReactor template rule #get0has-value-static] 
     */
	public static boolean hasFrameRate(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value ) {
		return Base.hasValue(model, instanceResource, FRAMERATE);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@1cbcc56 has the given value (maybe among other values).  
	 * @param value the value to be checked
     * @return true if this property contains (maybe among other) the given value
	 *
	 * [Generated from RDFReactor template rule #get0has-value-dynamic] 
     */
	public boolean hasFrameRate( org.ontoware.rdf2go.model.node.Node value ) {
		return Base.hasValue(this.model, this.getResource(), FRAMERATE);
	}

     /**
     * Get all values of property FrameRate as an Iterator over RDF2Go nodes 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ClosableIterator of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get7static] 
     */
	public static ClosableIterator<org.ontoware.rdf2go.model.node.Node> getAllFrameRate_asNode(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_asNode(model, instanceResource, FRAMERATE);
	}
	
    /**
     * Get all values of property FrameRate as a ReactorResult of RDF2Go nodes 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a List of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get7static-reactor-result] 
     */
	public static ReactorResult<org.ontoware.rdf2go.model.node.Node> getAllFrameRate_asNode_(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_as(model, instanceResource, FRAMERATE, org.ontoware.rdf2go.model.node.Node.class);
	}

    /**
     * Get all values of property FrameRate as an Iterator over RDF2Go nodes 
     * @return a ClosableIterator of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get8dynamic] 
     */
	public ClosableIterator<org.ontoware.rdf2go.model.node.Node> getAllFrameRate_asNode() {
		return Base.getAll_asNode(this.model, this.getResource(), FRAMERATE);
	}

    /**
     * Get all values of property FrameRate as a ReactorResult of RDF2Go nodes 
     * @return a List of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get8dynamic-reactor-result] 
     */
	public ReactorResult<org.ontoware.rdf2go.model.node.Node> getAllFrameRate_asNode_() {
		return Base.getAll_as(this.model, this.getResource(), FRAMERATE, org.ontoware.rdf2go.model.node.Node.class);
	}
     /**
     * Get all values of property FrameRate     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ClosableIterator of $type
	 *
	 * [Generated from RDFReactor template rule #get11static] 
     */
	public static ClosableIterator<java.lang.Float> getAllFrameRate(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll(model, instanceResource, FRAMERATE, java.lang.Float.class);
	}
	
    /**
     * Get all values of property FrameRate as a ReactorResult of java.lang.Float 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ReactorResult of $type which can conveniently be converted to iterator, list or array
	 *
	 * [Generated from RDFReactor template rule #get11static-reactorresult] 
     */
	public static ReactorResult<java.lang.Float> getAllFrameRate_as(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_as(model, instanceResource, FRAMERATE, java.lang.Float.class);
	}

    /**
     * Get all values of property FrameRate     * @return a ClosableIterator of $type
	 *
	 * [Generated from RDFReactor template rule #get12dynamic] 
     */
	public ClosableIterator<java.lang.Float> getAllFrameRate() {
		return Base.getAll(this.model, this.getResource(), FRAMERATE, java.lang.Float.class);
	}

    /**
     * Get all values of property FrameRate as a ReactorResult of java.lang.Float 
     * @return a ReactorResult of $type which can conveniently be converted to iterator, list or array
	 *
	 * [Generated from RDFReactor template rule #get12dynamic-reactorresult] 
     */
	public ReactorResult<java.lang.Float> getAllFrameRate_as() {
		return Base.getAll_as(this.model, this.getResource(), FRAMERATE, java.lang.Float.class);
	}
 
    /**
     * Adds a value to property FrameRate as an RDF2Go node 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #add1static] 
     */
	public static void addFrameRate( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.add(model, instanceResource, FRAMERATE, value);
	}
	
    /**
     * Adds a value to property FrameRate as an RDF2Go node 
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #add1dynamic] 
     */
	public void addFrameRate( org.ontoware.rdf2go.model.node.Node value) {
		Base.add(this.model, this.getResource(), FRAMERATE, value);
	}
    /**
     * Adds a value to property FrameRate from an instance of java.lang.Float 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #add3static] 
     */
	public static void addFrameRate(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, java.lang.Float value) {
		Base.add(model, instanceResource, FRAMERATE, value);
	}
	
    /**
     * Adds a value to property FrameRate from an instance of java.lang.Float 
	 *
	 * [Generated from RDFReactor template rule #add4dynamic] 
     */
	public void addFrameRate(java.lang.Float value) {
		Base.add(this.model, this.getResource(), FRAMERATE, value);
	}
  

    /**
     * Sets a value of property FrameRate from an RDF2Go node.
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be set
	 *
	 * [Generated from RDFReactor template rule #set1static] 
     */
	public static void setFrameRate( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.set(model, instanceResource, FRAMERATE, value);
	}
	
    /**
     * Sets a value of property FrameRate from an RDF2Go node.
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set1dynamic] 
     */
	public void setFrameRate( org.ontoware.rdf2go.model.node.Node value) {
		Base.set(this.model, this.getResource(), FRAMERATE, value);
	}
    /**
     * Sets a value of property FrameRate from an instance of java.lang.Float 
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set3static] 
     */
	public static void setFrameRate(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, java.lang.Float value) {
		Base.set(model, instanceResource, FRAMERATE, value);
	}
	
    /**
     * Sets a value of property FrameRate from an instance of java.lang.Float 
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set4dynamic] 
     */
	public void setFrameRate(java.lang.Float value) {
		Base.set(this.model, this.getResource(), FRAMERATE, value);
	}
  


    /**
     * Removes a value of property FrameRate as an RDF2Go node 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove1static] 
     */
	public static void removeFrameRate( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.remove(model, instanceResource, FRAMERATE, value);
	}
	
    /**
     * Removes a value of property FrameRate as an RDF2Go node
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove1dynamic] 
     */
	public void removeFrameRate( org.ontoware.rdf2go.model.node.Node value) {
		Base.remove(this.model, this.getResource(), FRAMERATE, value);
	}
    /**
     * Removes a value of property FrameRate given as an instance of java.lang.Float 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove3static] 
     */
	public static void removeFrameRate(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, java.lang.Float value) {
		Base.remove(model, instanceResource, FRAMERATE, value);
	}
	
    /**
     * Removes a value of property FrameRate given as an instance of java.lang.Float 
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove4dynamic] 
     */
	public void removeFrameRate(java.lang.Float value) {
		Base.remove(this.model, this.getResource(), FRAMERATE, value);
	}
  
    /**
     * Removes all values of property FrameRate     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #removeall1static] 
     */
	public static void removeAllFrameRate( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		Base.removeAll(model, instanceResource, FRAMERATE);
	}
	
    /**
     * Removes all values of property FrameRate	 *
	 * [Generated from RDFReactor template rule #removeall1dynamic] 
     */
	public void removeAllFrameRate() {
		Base.removeAll(this.model, this.getResource(), FRAMERATE);
	}
 }