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
 *   <li> DeletionDate </li>
 *   <li> OriginalLocation </li>
 * </ul>
 *
 * This class was generated by <a href="http://RDFReactor.semweb4j.org">RDFReactor</a> on 15/09/09 11:49 AM
 */
public class DeletedResource extends FileDataObject {

    /** http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#DeletedResource */
    @SuppressWarnings("hiding")
	public static final URI RDFS_CLASS = new URIImpl("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#DeletedResource", false);

    /** http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#deletionDate */
    @SuppressWarnings("hiding")
	public static final URI DELETIONDATE = new URIImpl("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#deletionDate",false);

    /** http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#originalLocation */
    @SuppressWarnings("hiding")
	public static final URI ORIGINALLOCATION = new URIImpl("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#originalLocation",false);

    /** 
     * All property-URIs with this class as domain.
     * All properties of all super-classes are also available. 
     */
    @SuppressWarnings("hiding")
    public static final URI[] MANAGED_URIS = {
      new URIImpl("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#deletionDate",false),
      new URIImpl("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#originalLocation",false) 
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
	protected DeletedResource ( Model model, URI classURI, org.ontoware.rdf2go.model.node.Resource instanceIdentifier, boolean write ) {
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
	public DeletedResource ( Model model, org.ontoware.rdf2go.model.node.Resource instanceIdentifier, boolean write ) {
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
	public DeletedResource ( Model model, String uriString, boolean write) throws ModelRuntimeException {
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
	public DeletedResource ( Model model, BlankNode bnode, boolean write ) {
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
	public DeletedResource ( Model model, boolean write ) {
		super(model, RDFS_CLASS, model.newRandomUniqueURI(), write);
	}

    ///////////////////////////////////////////////////////////////////
    // typing

	/**
	 * Return an existing instance of this class in the model. No statements are written.
	 * @param model an RDF2Go model
	 * @param instanceResource an RDF2Go resource
	 * @return an instance of DeletedResource  or null if none existst
	 *
	 * [Generated from RDFReactor template rule #class0] 
	 */
	public static DeletedResource  getInstance(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getInstance(model, instanceResource, DeletedResource.class);
	}

	/**
	 * Create a new instance of this class in the model. 
	 * That is, create the statement (instanceResource, RDF.type, http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#DeletedResource).
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
	public static ReactorResult<? extends DeletedResource> getAllInstances_as(Model model) {
		return Base.getAllInstances_as(model, RDFS_CLASS, DeletedResource.class );
	}

    /**
	 * Remove rdf:type DeletedResource from this instance. Other triples are not affected.
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
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@14fdb76 has at least one value set 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return true if this property has at least one value
	 *
	 * [Generated from RDFReactor template rule #get0has-static] 
     */
	public static boolean hasDeletionDate(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.has(model, instanceResource, DELETIONDATE);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@14fdb76 has at least one value set 
     * @return true if this property has at least one value
	 *
	 * [Generated from RDFReactor template rule #get0has-dynamic] 
     */
	public boolean hasDeletionDate() {
		return Base.has(this.model, this.getResource(), DELETIONDATE);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@14fdb76 has the given value (maybe among other values).  
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be checked
     * @return true if this property contains (maybe among other) the given value
	 *
	 * [Generated from RDFReactor template rule #get0has-value-static] 
     */
	public static boolean hasDeletionDate(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value ) {
		return Base.hasValue(model, instanceResource, DELETIONDATE);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@14fdb76 has the given value (maybe among other values).  
	 * @param value the value to be checked
     * @return true if this property contains (maybe among other) the given value
	 *
	 * [Generated from RDFReactor template rule #get0has-value-dynamic] 
     */
	public boolean hasDeletionDate( org.ontoware.rdf2go.model.node.Node value ) {
		return Base.hasValue(this.model, this.getResource(), DELETIONDATE);
	}

     /**
     * Get all values of property DeletionDate as an Iterator over RDF2Go nodes 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ClosableIterator of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get7static] 
     */
	public static ClosableIterator<org.ontoware.rdf2go.model.node.Node> getAllDeletionDate_asNode(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_asNode(model, instanceResource, DELETIONDATE);
	}
	
    /**
     * Get all values of property DeletionDate as a ReactorResult of RDF2Go nodes 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a List of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get7static-reactor-result] 
     */
	public static ReactorResult<org.ontoware.rdf2go.model.node.Node> getAllDeletionDate_asNode_(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_as(model, instanceResource, DELETIONDATE, org.ontoware.rdf2go.model.node.Node.class);
	}

    /**
     * Get all values of property DeletionDate as an Iterator over RDF2Go nodes 
     * @return a ClosableIterator of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get8dynamic] 
     */
	public ClosableIterator<org.ontoware.rdf2go.model.node.Node> getAllDeletionDate_asNode() {
		return Base.getAll_asNode(this.model, this.getResource(), DELETIONDATE);
	}

    /**
     * Get all values of property DeletionDate as a ReactorResult of RDF2Go nodes 
     * @return a List of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get8dynamic-reactor-result] 
     */
	public ReactorResult<org.ontoware.rdf2go.model.node.Node> getAllDeletionDate_asNode_() {
		return Base.getAll_as(this.model, this.getResource(), DELETIONDATE, org.ontoware.rdf2go.model.node.Node.class);
	}
     /**
     * Get all values of property DeletionDate     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ClosableIterator of $type
	 *
	 * [Generated from RDFReactor template rule #get11static] 
     */
	public static ClosableIterator<java.util.Calendar> getAllDeletionDate(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll(model, instanceResource, DELETIONDATE, java.util.Calendar.class);
	}
	
    /**
     * Get all values of property DeletionDate as a ReactorResult of java.util.Calendar 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ReactorResult of $type which can conveniently be converted to iterator, list or array
	 *
	 * [Generated from RDFReactor template rule #get11static-reactorresult] 
     */
	public static ReactorResult<java.util.Calendar> getAllDeletionDate_as(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_as(model, instanceResource, DELETIONDATE, java.util.Calendar.class);
	}

    /**
     * Get all values of property DeletionDate     * @return a ClosableIterator of $type
	 *
	 * [Generated from RDFReactor template rule #get12dynamic] 
     */
	public ClosableIterator<java.util.Calendar> getAllDeletionDate() {
		return Base.getAll(this.model, this.getResource(), DELETIONDATE, java.util.Calendar.class);
	}

    /**
     * Get all values of property DeletionDate as a ReactorResult of java.util.Calendar 
     * @return a ReactorResult of $type which can conveniently be converted to iterator, list or array
	 *
	 * [Generated from RDFReactor template rule #get12dynamic-reactorresult] 
     */
	public ReactorResult<java.util.Calendar> getAllDeletionDate_as() {
		return Base.getAll_as(this.model, this.getResource(), DELETIONDATE, java.util.Calendar.class);
	}
 
    /**
     * Adds a value to property DeletionDate as an RDF2Go node 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #add1static] 
     */
	public static void addDeletionDate( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.add(model, instanceResource, DELETIONDATE, value);
	}
	
    /**
     * Adds a value to property DeletionDate as an RDF2Go node 
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #add1dynamic] 
     */
	public void addDeletionDate( org.ontoware.rdf2go.model.node.Node value) {
		Base.add(this.model, this.getResource(), DELETIONDATE, value);
	}
    /**
     * Adds a value to property DeletionDate from an instance of java.util.Calendar 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #add3static] 
     */
	public static void addDeletionDate(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, java.util.Calendar value) {
		Base.add(model, instanceResource, DELETIONDATE, value);
	}
	
    /**
     * Adds a value to property DeletionDate from an instance of java.util.Calendar 
	 *
	 * [Generated from RDFReactor template rule #add4dynamic] 
     */
	public void addDeletionDate(java.util.Calendar value) {
		Base.add(this.model, this.getResource(), DELETIONDATE, value);
	}
  

    /**
     * Sets a value of property DeletionDate from an RDF2Go node.
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be set
	 *
	 * [Generated from RDFReactor template rule #set1static] 
     */
	public static void setDeletionDate( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.set(model, instanceResource, DELETIONDATE, value);
	}
	
    /**
     * Sets a value of property DeletionDate from an RDF2Go node.
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set1dynamic] 
     */
	public void setDeletionDate( org.ontoware.rdf2go.model.node.Node value) {
		Base.set(this.model, this.getResource(), DELETIONDATE, value);
	}
    /**
     * Sets a value of property DeletionDate from an instance of java.util.Calendar 
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set3static] 
     */
	public static void setDeletionDate(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, java.util.Calendar value) {
		Base.set(model, instanceResource, DELETIONDATE, value);
	}
	
    /**
     * Sets a value of property DeletionDate from an instance of java.util.Calendar 
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set4dynamic] 
     */
	public void setDeletionDate(java.util.Calendar value) {
		Base.set(this.model, this.getResource(), DELETIONDATE, value);
	}
  


    /**
     * Removes a value of property DeletionDate as an RDF2Go node 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove1static] 
     */
	public static void removeDeletionDate( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.remove(model, instanceResource, DELETIONDATE, value);
	}
	
    /**
     * Removes a value of property DeletionDate as an RDF2Go node
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove1dynamic] 
     */
	public void removeDeletionDate( org.ontoware.rdf2go.model.node.Node value) {
		Base.remove(this.model, this.getResource(), DELETIONDATE, value);
	}
    /**
     * Removes a value of property DeletionDate given as an instance of java.util.Calendar 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove3static] 
     */
	public static void removeDeletionDate(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, java.util.Calendar value) {
		Base.remove(model, instanceResource, DELETIONDATE, value);
	}
	
    /**
     * Removes a value of property DeletionDate given as an instance of java.util.Calendar 
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove4dynamic] 
     */
	public void removeDeletionDate(java.util.Calendar value) {
		Base.remove(this.model, this.getResource(), DELETIONDATE, value);
	}
  
    /**
     * Removes all values of property DeletionDate     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #removeall1static] 
     */
	public static void removeAllDeletionDate( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		Base.removeAll(model, instanceResource, DELETIONDATE);
	}
	
    /**
     * Removes all values of property DeletionDate	 *
	 * [Generated from RDFReactor template rule #removeall1dynamic] 
     */
	public void removeAllDeletionDate() {
		Base.removeAll(this.model, this.getResource(), DELETIONDATE);
	}
     /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@12a2259 has at least one value set 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return true if this property has at least one value
	 *
	 * [Generated from RDFReactor template rule #get0has-static] 
     */
	public static boolean hasOriginalLocation(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.has(model, instanceResource, ORIGINALLOCATION);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@12a2259 has at least one value set 
     * @return true if this property has at least one value
	 *
	 * [Generated from RDFReactor template rule #get0has-dynamic] 
     */
	public boolean hasOriginalLocation() {
		return Base.has(this.model, this.getResource(), ORIGINALLOCATION);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@12a2259 has the given value (maybe among other values).  
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be checked
     * @return true if this property contains (maybe among other) the given value
	 *
	 * [Generated from RDFReactor template rule #get0has-value-static] 
     */
	public static boolean hasOriginalLocation(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value ) {
		return Base.hasValue(model, instanceResource, ORIGINALLOCATION);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@12a2259 has the given value (maybe among other values).  
	 * @param value the value to be checked
     * @return true if this property contains (maybe among other) the given value
	 *
	 * [Generated from RDFReactor template rule #get0has-value-dynamic] 
     */
	public boolean hasOriginalLocation( org.ontoware.rdf2go.model.node.Node value ) {
		return Base.hasValue(this.model, this.getResource(), ORIGINALLOCATION);
	}

     /**
     * Get all values of property OriginalLocation as an Iterator over RDF2Go nodes 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ClosableIterator of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get7static] 
     */
	public static ClosableIterator<org.ontoware.rdf2go.model.node.Node> getAllOriginalLocation_asNode(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_asNode(model, instanceResource, ORIGINALLOCATION);
	}
	
    /**
     * Get all values of property OriginalLocation as a ReactorResult of RDF2Go nodes 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a List of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get7static-reactor-result] 
     */
	public static ReactorResult<org.ontoware.rdf2go.model.node.Node> getAllOriginalLocation_asNode_(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_as(model, instanceResource, ORIGINALLOCATION, org.ontoware.rdf2go.model.node.Node.class);
	}

    /**
     * Get all values of property OriginalLocation as an Iterator over RDF2Go nodes 
     * @return a ClosableIterator of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get8dynamic] 
     */
	public ClosableIterator<org.ontoware.rdf2go.model.node.Node> getAllOriginalLocation_asNode() {
		return Base.getAll_asNode(this.model, this.getResource(), ORIGINALLOCATION);
	}

    /**
     * Get all values of property OriginalLocation as a ReactorResult of RDF2Go nodes 
     * @return a List of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get8dynamic-reactor-result] 
     */
	public ReactorResult<org.ontoware.rdf2go.model.node.Node> getAllOriginalLocation_asNode_() {
		return Base.getAll_as(this.model, this.getResource(), ORIGINALLOCATION, org.ontoware.rdf2go.model.node.Node.class);
	}
     /**
     * Get all values of property OriginalLocation     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ClosableIterator of $type
	 *
	 * [Generated from RDFReactor template rule #get11static] 
     */
	public static ClosableIterator<java.lang.String> getAllOriginalLocation(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll(model, instanceResource, ORIGINALLOCATION, java.lang.String.class);
	}
	
    /**
     * Get all values of property OriginalLocation as a ReactorResult of java.lang.String 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ReactorResult of $type which can conveniently be converted to iterator, list or array
	 *
	 * [Generated from RDFReactor template rule #get11static-reactorresult] 
     */
	public static ReactorResult<java.lang.String> getAllOriginalLocation_as(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_as(model, instanceResource, ORIGINALLOCATION, java.lang.String.class);
	}

    /**
     * Get all values of property OriginalLocation     * @return a ClosableIterator of $type
	 *
	 * [Generated from RDFReactor template rule #get12dynamic] 
     */
	public ClosableIterator<java.lang.String> getAllOriginalLocation() {
		return Base.getAll(this.model, this.getResource(), ORIGINALLOCATION, java.lang.String.class);
	}

    /**
     * Get all values of property OriginalLocation as a ReactorResult of java.lang.String 
     * @return a ReactorResult of $type which can conveniently be converted to iterator, list or array
	 *
	 * [Generated from RDFReactor template rule #get12dynamic-reactorresult] 
     */
	public ReactorResult<java.lang.String> getAllOriginalLocation_as() {
		return Base.getAll_as(this.model, this.getResource(), ORIGINALLOCATION, java.lang.String.class);
	}
 
    /**
     * Adds a value to property OriginalLocation as an RDF2Go node 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #add1static] 
     */
	public static void addOriginalLocation( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.add(model, instanceResource, ORIGINALLOCATION, value);
	}
	
    /**
     * Adds a value to property OriginalLocation as an RDF2Go node 
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #add1dynamic] 
     */
	public void addOriginalLocation( org.ontoware.rdf2go.model.node.Node value) {
		Base.add(this.model, this.getResource(), ORIGINALLOCATION, value);
	}
    /**
     * Adds a value to property OriginalLocation from an instance of java.lang.String 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #add3static] 
     */
	public static void addOriginalLocation(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, java.lang.String value) {
		Base.add(model, instanceResource, ORIGINALLOCATION, value);
	}
	
    /**
     * Adds a value to property OriginalLocation from an instance of java.lang.String 
	 *
	 * [Generated from RDFReactor template rule #add4dynamic] 
     */
	public void addOriginalLocation(java.lang.String value) {
		Base.add(this.model, this.getResource(), ORIGINALLOCATION, value);
	}
  

    /**
     * Sets a value of property OriginalLocation from an RDF2Go node.
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be set
	 *
	 * [Generated from RDFReactor template rule #set1static] 
     */
	public static void setOriginalLocation( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.set(model, instanceResource, ORIGINALLOCATION, value);
	}
	
    /**
     * Sets a value of property OriginalLocation from an RDF2Go node.
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set1dynamic] 
     */
	public void setOriginalLocation( org.ontoware.rdf2go.model.node.Node value) {
		Base.set(this.model, this.getResource(), ORIGINALLOCATION, value);
	}
    /**
     * Sets a value of property OriginalLocation from an instance of java.lang.String 
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set3static] 
     */
	public static void setOriginalLocation(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, java.lang.String value) {
		Base.set(model, instanceResource, ORIGINALLOCATION, value);
	}
	
    /**
     * Sets a value of property OriginalLocation from an instance of java.lang.String 
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set4dynamic] 
     */
	public void setOriginalLocation(java.lang.String value) {
		Base.set(this.model, this.getResource(), ORIGINALLOCATION, value);
	}
  


    /**
     * Removes a value of property OriginalLocation as an RDF2Go node 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove1static] 
     */
	public static void removeOriginalLocation( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.remove(model, instanceResource, ORIGINALLOCATION, value);
	}
	
    /**
     * Removes a value of property OriginalLocation as an RDF2Go node
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove1dynamic] 
     */
	public void removeOriginalLocation( org.ontoware.rdf2go.model.node.Node value) {
		Base.remove(this.model, this.getResource(), ORIGINALLOCATION, value);
	}
    /**
     * Removes a value of property OriginalLocation given as an instance of java.lang.String 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove3static] 
     */
	public static void removeOriginalLocation(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, java.lang.String value) {
		Base.remove(model, instanceResource, ORIGINALLOCATION, value);
	}
	
    /**
     * Removes a value of property OriginalLocation given as an instance of java.lang.String 
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove4dynamic] 
     */
	public void removeOriginalLocation(java.lang.String value) {
		Base.remove(this.model, this.getResource(), ORIGINALLOCATION, value);
	}
  
    /**
     * Removes all values of property OriginalLocation     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #removeall1static] 
     */
	public static void removeAllOriginalLocation( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		Base.removeAll(model, instanceResource, ORIGINALLOCATION);
	}
	
    /**
     * Removes all values of property OriginalLocation	 *
	 * [Generated from RDFReactor template rule #removeall1dynamic] 
     */
	public void removeAllOriginalLocation() {
		Base.removeAll(this.model, this.getResource(), ORIGINALLOCATION);
	}
 }