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
 *   <li> HasMediaFileListEntry </li>
 * </ul>
 *
 * This class was generated by <a href="http://RDFReactor.semweb4j.org">RDFReactor</a> on 15/09/09 11:49 AM
 */
public class MediaList extends InformationElement {

    /** http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#MediaList */
    @SuppressWarnings("hiding")
	public static final URI RDFS_CLASS = new URIImpl("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#MediaList", false);

    /** http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#hasMediaFileListEntry */
    @SuppressWarnings("hiding")
	public static final URI HASMEDIAFILELISTENTRY = new URIImpl("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#hasMediaFileListEntry",false);

    /** 
     * All property-URIs with this class as domain.
     * All properties of all super-classes are also available. 
     */
    @SuppressWarnings("hiding")
    public static final URI[] MANAGED_URIS = {
      new URIImpl("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#hasMediaFileListEntry",false) 
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
	protected MediaList ( Model model, URI classURI, org.ontoware.rdf2go.model.node.Resource instanceIdentifier, boolean write ) {
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
	public MediaList ( Model model, org.ontoware.rdf2go.model.node.Resource instanceIdentifier, boolean write ) {
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
	public MediaList ( Model model, String uriString, boolean write) throws ModelRuntimeException {
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
	public MediaList ( Model model, BlankNode bnode, boolean write ) {
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
	public MediaList ( Model model, boolean write ) {
		super(model, RDFS_CLASS, model.newRandomUniqueURI(), write);
	}

    ///////////////////////////////////////////////////////////////////
    // typing

	/**
	 * Return an existing instance of this class in the model. No statements are written.
	 * @param model an RDF2Go model
	 * @param instanceResource an RDF2Go resource
	 * @return an instance of MediaList  or null if none existst
	 *
	 * [Generated from RDFReactor template rule #class0] 
	 */
	public static MediaList  getInstance(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getInstance(model, instanceResource, MediaList.class);
	}

	/**
	 * Create a new instance of this class in the model. 
	 * That is, create the statement (instanceResource, RDF.type, http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#MediaList).
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
	public static ReactorResult<? extends MediaList> getAllInstances_as(Model model) {
		return Base.getAllInstances_as(model, RDFS_CLASS, MediaList.class );
	}

    /**
	 * Remove rdf:type MediaList from this instance. Other triples are not affected.
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
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@196649c has at least one value set 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return true if this property has at least one value
	 *
	 * [Generated from RDFReactor template rule #get0has-static] 
     */
	public static boolean hasHasMediaFileListEntry(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.has(model, instanceResource, HASMEDIAFILELISTENTRY);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@196649c has at least one value set 
     * @return true if this property has at least one value
	 *
	 * [Generated from RDFReactor template rule #get0has-dynamic] 
     */
	public boolean hasHasMediaFileListEntry() {
		return Base.has(this.model, this.getResource(), HASMEDIAFILELISTENTRY);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@196649c has the given value (maybe among other values).  
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be checked
     * @return true if this property contains (maybe among other) the given value
	 *
	 * [Generated from RDFReactor template rule #get0has-value-static] 
     */
	public static boolean hasHasMediaFileListEntry(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value ) {
		return Base.hasValue(model, instanceResource, HASMEDIAFILELISTENTRY);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@196649c has the given value (maybe among other values).  
	 * @param value the value to be checked
     * @return true if this property contains (maybe among other) the given value
	 *
	 * [Generated from RDFReactor template rule #get0has-value-dynamic] 
     */
	public boolean hasHasMediaFileListEntry( org.ontoware.rdf2go.model.node.Node value ) {
		return Base.hasValue(this.model, this.getResource(), HASMEDIAFILELISTENTRY);
	}

     /**
     * Get all values of property HasMediaFileListEntry as an Iterator over RDF2Go nodes 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ClosableIterator of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get7static] 
     */
	public static ClosableIterator<org.ontoware.rdf2go.model.node.Node> getAllHasMediaFileListEntry_asNode(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_asNode(model, instanceResource, HASMEDIAFILELISTENTRY);
	}
	
    /**
     * Get all values of property HasMediaFileListEntry as a ReactorResult of RDF2Go nodes 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a List of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get7static-reactor-result] 
     */
	public static ReactorResult<org.ontoware.rdf2go.model.node.Node> getAllHasMediaFileListEntry_asNode_(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_as(model, instanceResource, HASMEDIAFILELISTENTRY, org.ontoware.rdf2go.model.node.Node.class);
	}

    /**
     * Get all values of property HasMediaFileListEntry as an Iterator over RDF2Go nodes 
     * @return a ClosableIterator of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get8dynamic] 
     */
	public ClosableIterator<org.ontoware.rdf2go.model.node.Node> getAllHasMediaFileListEntry_asNode() {
		return Base.getAll_asNode(this.model, this.getResource(), HASMEDIAFILELISTENTRY);
	}

    /**
     * Get all values of property HasMediaFileListEntry as a ReactorResult of RDF2Go nodes 
     * @return a List of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get8dynamic-reactor-result] 
     */
	public ReactorResult<org.ontoware.rdf2go.model.node.Node> getAllHasMediaFileListEntry_asNode_() {
		return Base.getAll_as(this.model, this.getResource(), HASMEDIAFILELISTENTRY, org.ontoware.rdf2go.model.node.Node.class);
	}
     /**
     * Get all values of property HasMediaFileListEntry     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ClosableIterator of $type
	 *
	 * [Generated from RDFReactor template rule #get11static] 
     */
	public static ClosableIterator<MediaFileListEntry> getAllHasMediaFileListEntry(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll(model, instanceResource, HASMEDIAFILELISTENTRY, MediaFileListEntry.class);
	}
	
    /**
     * Get all values of property HasMediaFileListEntry as a ReactorResult of MediaFileListEntry 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ReactorResult of $type which can conveniently be converted to iterator, list or array
	 *
	 * [Generated from RDFReactor template rule #get11static-reactorresult] 
     */
	public static ReactorResult<MediaFileListEntry> getAllHasMediaFileListEntry_as(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_as(model, instanceResource, HASMEDIAFILELISTENTRY, MediaFileListEntry.class);
	}

    /**
     * Get all values of property HasMediaFileListEntry     * @return a ClosableIterator of $type
	 *
	 * [Generated from RDFReactor template rule #get12dynamic] 
     */
	public ClosableIterator<MediaFileListEntry> getAllHasMediaFileListEntry() {
		return Base.getAll(this.model, this.getResource(), HASMEDIAFILELISTENTRY, MediaFileListEntry.class);
	}

    /**
     * Get all values of property HasMediaFileListEntry as a ReactorResult of MediaFileListEntry 
     * @return a ReactorResult of $type which can conveniently be converted to iterator, list or array
	 *
	 * [Generated from RDFReactor template rule #get12dynamic-reactorresult] 
     */
	public ReactorResult<MediaFileListEntry> getAllHasMediaFileListEntry_as() {
		return Base.getAll_as(this.model, this.getResource(), HASMEDIAFILELISTENTRY, MediaFileListEntry.class);
	}
 
    /**
     * Adds a value to property HasMediaFileListEntry as an RDF2Go node 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #add1static] 
     */
	public static void addHasMediaFileListEntry( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.add(model, instanceResource, HASMEDIAFILELISTENTRY, value);
	}
	
    /**
     * Adds a value to property HasMediaFileListEntry as an RDF2Go node 
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #add1dynamic] 
     */
	public void addHasMediaFileListEntry( org.ontoware.rdf2go.model.node.Node value) {
		Base.add(this.model, this.getResource(), HASMEDIAFILELISTENTRY, value);
	}
    /**
     * Adds a value to property HasMediaFileListEntry from an instance of MediaFileListEntry 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #add3static] 
     */
	public static void addHasMediaFileListEntry(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, MediaFileListEntry value) {
		Base.add(model, instanceResource, HASMEDIAFILELISTENTRY, value);
	}
	
    /**
     * Adds a value to property HasMediaFileListEntry from an instance of MediaFileListEntry 
	 *
	 * [Generated from RDFReactor template rule #add4dynamic] 
     */
	public void addHasMediaFileListEntry(MediaFileListEntry value) {
		Base.add(this.model, this.getResource(), HASMEDIAFILELISTENTRY, value);
	}
  

    /**
     * Sets a value of property HasMediaFileListEntry from an RDF2Go node.
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be set
	 *
	 * [Generated from RDFReactor template rule #set1static] 
     */
	public static void setHasMediaFileListEntry( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.set(model, instanceResource, HASMEDIAFILELISTENTRY, value);
	}
	
    /**
     * Sets a value of property HasMediaFileListEntry from an RDF2Go node.
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set1dynamic] 
     */
	public void setHasMediaFileListEntry( org.ontoware.rdf2go.model.node.Node value) {
		Base.set(this.model, this.getResource(), HASMEDIAFILELISTENTRY, value);
	}
    /**
     * Sets a value of property HasMediaFileListEntry from an instance of MediaFileListEntry 
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set3static] 
     */
	public static void setHasMediaFileListEntry(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, MediaFileListEntry value) {
		Base.set(model, instanceResource, HASMEDIAFILELISTENTRY, value);
	}
	
    /**
     * Sets a value of property HasMediaFileListEntry from an instance of MediaFileListEntry 
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set4dynamic] 
     */
	public void setHasMediaFileListEntry(MediaFileListEntry value) {
		Base.set(this.model, this.getResource(), HASMEDIAFILELISTENTRY, value);
	}
  


    /**
     * Removes a value of property HasMediaFileListEntry as an RDF2Go node 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove1static] 
     */
	public static void removeHasMediaFileListEntry( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.remove(model, instanceResource, HASMEDIAFILELISTENTRY, value);
	}
	
    /**
     * Removes a value of property HasMediaFileListEntry as an RDF2Go node
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove1dynamic] 
     */
	public void removeHasMediaFileListEntry( org.ontoware.rdf2go.model.node.Node value) {
		Base.remove(this.model, this.getResource(), HASMEDIAFILELISTENTRY, value);
	}
    /**
     * Removes a value of property HasMediaFileListEntry given as an instance of MediaFileListEntry 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove3static] 
     */
	public static void removeHasMediaFileListEntry(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, MediaFileListEntry value) {
		Base.remove(model, instanceResource, HASMEDIAFILELISTENTRY, value);
	}
	
    /**
     * Removes a value of property HasMediaFileListEntry given as an instance of MediaFileListEntry 
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove4dynamic] 
     */
	public void removeHasMediaFileListEntry(MediaFileListEntry value) {
		Base.remove(this.model, this.getResource(), HASMEDIAFILELISTENTRY, value);
	}
  
    /**
     * Removes all values of property HasMediaFileListEntry     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #removeall1static] 
     */
	public static void removeAllHasMediaFileListEntry( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		Base.removeAll(model, instanceResource, HASMEDIAFILELISTENTRY);
	}
	
    /**
     * Removes all values of property HasMediaFileListEntry	 *
	 * [Generated from RDFReactor template rule #removeall1dynamic] 
     */
	public void removeAllHasMediaFileListEntry() {
		Base.removeAll(this.model, this.getResource(), HASMEDIAFILELISTENTRY);
	}
 }