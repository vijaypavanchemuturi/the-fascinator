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
 *   <li> Containsbookmark </li>
 *   <li> Containsfolder </li>
 * </ul>
 *
 * This class was generated by <a href="http://RDFReactor.semweb4j.org">RDFReactor</a> on 15/09/09 11:49 AM
 */
public class BookmarkFolder extends InformationElement {

    /** http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#BookmarkFolder */
    @SuppressWarnings("hiding")
	public static final URI RDFS_CLASS = new URIImpl("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#BookmarkFolder", false);

    /** http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#containsBookmark */
    @SuppressWarnings("hiding")
	public static final URI CONTAINSBOOKMARK = new URIImpl("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#containsBookmark",false);

    /** http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#containsBookmarkFolder */
    @SuppressWarnings("hiding")
	public static final URI CONTAINSFOLDER = new URIImpl("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#containsBookmarkFolder",false);

    /** 
     * All property-URIs with this class as domain.
     * All properties of all super-classes are also available. 
     */
    @SuppressWarnings("hiding")
    public static final URI[] MANAGED_URIS = {
      new URIImpl("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#containsBookmark",false),
      new URIImpl("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#containsBookmarkFolder",false) 
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
	protected BookmarkFolder ( Model model, URI classURI, org.ontoware.rdf2go.model.node.Resource instanceIdentifier, boolean write ) {
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
	public BookmarkFolder ( Model model, org.ontoware.rdf2go.model.node.Resource instanceIdentifier, boolean write ) {
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
	public BookmarkFolder ( Model model, String uriString, boolean write) throws ModelRuntimeException {
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
	public BookmarkFolder ( Model model, BlankNode bnode, boolean write ) {
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
	public BookmarkFolder ( Model model, boolean write ) {
		super(model, RDFS_CLASS, model.newRandomUniqueURI(), write);
	}

    ///////////////////////////////////////////////////////////////////
    // typing

	/**
	 * Return an existing instance of this class in the model. No statements are written.
	 * @param model an RDF2Go model
	 * @param instanceResource an RDF2Go resource
	 * @return an instance of BookmarkFolder  or null if none existst
	 *
	 * [Generated from RDFReactor template rule #class0] 
	 */
	public static BookmarkFolder  getInstance(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getInstance(model, instanceResource, BookmarkFolder.class);
	}

	/**
	 * Create a new instance of this class in the model. 
	 * That is, create the statement (instanceResource, RDF.type, http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#BookmarkFolder).
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
	public static ReactorResult<? extends BookmarkFolder> getAllInstances_as(Model model) {
		return Base.getAllInstances_as(model, RDFS_CLASS, BookmarkFolder.class );
	}

    /**
	 * Remove rdf:type BookmarkFolder from this instance. Other triples are not affected.
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
	 * @param model an RDF2Go model
	 * @param objectValue
	 * @return all A's as RDF resources, that have a relation 'Containsfolder' to this BookmarkFolder instance
	 *
	 * [Generated from RDFReactor template rule #getallinverse1static] 
	 */
	public static ClosableIterator<org.ontoware.rdf2go.model.node.Resource> getAllContainsfolder_Inverse( Model model, Object objectValue) {
		return Base.getAll_Inverse(model, BookmarkFolder.CONTAINSFOLDER, objectValue);
	}

	/**
	 * @return all A's as RDF resources, that have a relation 'Containsfolder' to this BookmarkFolder instance
	 *
	 * [Generated from RDFReactor template rule #getallinverse1dynamic] 
	 */
	public ClosableIterator<org.ontoware.rdf2go.model.node.Resource> getAllContainsfolder_Inverse() {
		return Base.getAll_Inverse(this.model, BookmarkFolder.CONTAINSFOLDER, this.getResource() );
	}

	/**
	 * @param model an RDF2Go model
	 * @param objectValue
	 * @return all A's as a ReactorResult, that have a relation 'Containsfolder' to this BookmarkFolder instance
	 *
	 * [Generated from RDFReactor template rule #getallinverse-as1static] 
	 */
	public static ReactorResult<org.ontoware.rdf2go.model.node.Resource> getAllContainsfolder_Inverse_as( Model model, Object objectValue) {
		return Base.getAll_Inverse_as(model, BookmarkFolder.CONTAINSFOLDER, objectValue, org.ontoware.rdf2go.model.node.Resource.class);
	}



    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@db4c8d has at least one value set 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return true if this property has at least one value
	 *
	 * [Generated from RDFReactor template rule #get0has-static] 
     */
	public static boolean hasContainsbookmark(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.has(model, instanceResource, CONTAINSBOOKMARK);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@db4c8d has at least one value set 
     * @return true if this property has at least one value
	 *
	 * [Generated from RDFReactor template rule #get0has-dynamic] 
     */
	public boolean hasContainsbookmark() {
		return Base.has(this.model, this.getResource(), CONTAINSBOOKMARK);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@db4c8d has the given value (maybe among other values).  
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be checked
     * @return true if this property contains (maybe among other) the given value
	 *
	 * [Generated from RDFReactor template rule #get0has-value-static] 
     */
	public static boolean hasContainsbookmark(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value ) {
		return Base.hasValue(model, instanceResource, CONTAINSBOOKMARK);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@db4c8d has the given value (maybe among other values).  
	 * @param value the value to be checked
     * @return true if this property contains (maybe among other) the given value
	 *
	 * [Generated from RDFReactor template rule #get0has-value-dynamic] 
     */
	public boolean hasContainsbookmark( org.ontoware.rdf2go.model.node.Node value ) {
		return Base.hasValue(this.model, this.getResource(), CONTAINSBOOKMARK);
	}

     /**
     * Get all values of property Containsbookmark as an Iterator over RDF2Go nodes 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ClosableIterator of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get7static] 
     */
	public static ClosableIterator<org.ontoware.rdf2go.model.node.Node> getAllContainsbookmark_asNode(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_asNode(model, instanceResource, CONTAINSBOOKMARK);
	}
	
    /**
     * Get all values of property Containsbookmark as a ReactorResult of RDF2Go nodes 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a List of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get7static-reactor-result] 
     */
	public static ReactorResult<org.ontoware.rdf2go.model.node.Node> getAllContainsbookmark_asNode_(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_as(model, instanceResource, CONTAINSBOOKMARK, org.ontoware.rdf2go.model.node.Node.class);
	}

    /**
     * Get all values of property Containsbookmark as an Iterator over RDF2Go nodes 
     * @return a ClosableIterator of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get8dynamic] 
     */
	public ClosableIterator<org.ontoware.rdf2go.model.node.Node> getAllContainsbookmark_asNode() {
		return Base.getAll_asNode(this.model, this.getResource(), CONTAINSBOOKMARK);
	}

    /**
     * Get all values of property Containsbookmark as a ReactorResult of RDF2Go nodes 
     * @return a List of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get8dynamic-reactor-result] 
     */
	public ReactorResult<org.ontoware.rdf2go.model.node.Node> getAllContainsbookmark_asNode_() {
		return Base.getAll_as(this.model, this.getResource(), CONTAINSBOOKMARK, org.ontoware.rdf2go.model.node.Node.class);
	}
     /**
     * Get all values of property Containsbookmark     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ClosableIterator of $type
	 *
	 * [Generated from RDFReactor template rule #get11static] 
     */
	public static ClosableIterator<Bookmark> getAllContainsbookmark(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll(model, instanceResource, CONTAINSBOOKMARK, Bookmark.class);
	}
	
    /**
     * Get all values of property Containsbookmark as a ReactorResult of Bookmark 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ReactorResult of $type which can conveniently be converted to iterator, list or array
	 *
	 * [Generated from RDFReactor template rule #get11static-reactorresult] 
     */
	public static ReactorResult<Bookmark> getAllContainsbookmark_as(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_as(model, instanceResource, CONTAINSBOOKMARK, Bookmark.class);
	}

    /**
     * Get all values of property Containsbookmark     * @return a ClosableIterator of $type
	 *
	 * [Generated from RDFReactor template rule #get12dynamic] 
     */
	public ClosableIterator<Bookmark> getAllContainsbookmark() {
		return Base.getAll(this.model, this.getResource(), CONTAINSBOOKMARK, Bookmark.class);
	}

    /**
     * Get all values of property Containsbookmark as a ReactorResult of Bookmark 
     * @return a ReactorResult of $type which can conveniently be converted to iterator, list or array
	 *
	 * [Generated from RDFReactor template rule #get12dynamic-reactorresult] 
     */
	public ReactorResult<Bookmark> getAllContainsbookmark_as() {
		return Base.getAll_as(this.model, this.getResource(), CONTAINSBOOKMARK, Bookmark.class);
	}
 
    /**
     * Adds a value to property Containsbookmark as an RDF2Go node 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #add1static] 
     */
	public static void addContainsbookmark( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.add(model, instanceResource, CONTAINSBOOKMARK, value);
	}
	
    /**
     * Adds a value to property Containsbookmark as an RDF2Go node 
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #add1dynamic] 
     */
	public void addContainsbookmark( org.ontoware.rdf2go.model.node.Node value) {
		Base.add(this.model, this.getResource(), CONTAINSBOOKMARK, value);
	}
    /**
     * Adds a value to property Containsbookmark from an instance of Bookmark 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #add3static] 
     */
	public static void addContainsbookmark(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, Bookmark value) {
		Base.add(model, instanceResource, CONTAINSBOOKMARK, value);
	}
	
    /**
     * Adds a value to property Containsbookmark from an instance of Bookmark 
	 *
	 * [Generated from RDFReactor template rule #add4dynamic] 
     */
	public void addContainsbookmark(Bookmark value) {
		Base.add(this.model, this.getResource(), CONTAINSBOOKMARK, value);
	}
  

    /**
     * Sets a value of property Containsbookmark from an RDF2Go node.
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be set
	 *
	 * [Generated from RDFReactor template rule #set1static] 
     */
	public static void setContainsbookmark( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.set(model, instanceResource, CONTAINSBOOKMARK, value);
	}
	
    /**
     * Sets a value of property Containsbookmark from an RDF2Go node.
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set1dynamic] 
     */
	public void setContainsbookmark( org.ontoware.rdf2go.model.node.Node value) {
		Base.set(this.model, this.getResource(), CONTAINSBOOKMARK, value);
	}
    /**
     * Sets a value of property Containsbookmark from an instance of Bookmark 
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set3static] 
     */
	public static void setContainsbookmark(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, Bookmark value) {
		Base.set(model, instanceResource, CONTAINSBOOKMARK, value);
	}
	
    /**
     * Sets a value of property Containsbookmark from an instance of Bookmark 
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set4dynamic] 
     */
	public void setContainsbookmark(Bookmark value) {
		Base.set(this.model, this.getResource(), CONTAINSBOOKMARK, value);
	}
  


    /**
     * Removes a value of property Containsbookmark as an RDF2Go node 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove1static] 
     */
	public static void removeContainsbookmark( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.remove(model, instanceResource, CONTAINSBOOKMARK, value);
	}
	
    /**
     * Removes a value of property Containsbookmark as an RDF2Go node
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove1dynamic] 
     */
	public void removeContainsbookmark( org.ontoware.rdf2go.model.node.Node value) {
		Base.remove(this.model, this.getResource(), CONTAINSBOOKMARK, value);
	}
    /**
     * Removes a value of property Containsbookmark given as an instance of Bookmark 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove3static] 
     */
	public static void removeContainsbookmark(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, Bookmark value) {
		Base.remove(model, instanceResource, CONTAINSBOOKMARK, value);
	}
	
    /**
     * Removes a value of property Containsbookmark given as an instance of Bookmark 
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove4dynamic] 
     */
	public void removeContainsbookmark(Bookmark value) {
		Base.remove(this.model, this.getResource(), CONTAINSBOOKMARK, value);
	}
  
    /**
     * Removes all values of property Containsbookmark     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #removeall1static] 
     */
	public static void removeAllContainsbookmark( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		Base.removeAll(model, instanceResource, CONTAINSBOOKMARK);
	}
	
    /**
     * Removes all values of property Containsbookmark	 *
	 * [Generated from RDFReactor template rule #removeall1dynamic] 
     */
	public void removeAllContainsbookmark() {
		Base.removeAll(this.model, this.getResource(), CONTAINSBOOKMARK);
	}
     /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@2026f3 has at least one value set 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return true if this property has at least one value
	 *
	 * [Generated from RDFReactor template rule #get0has-static] 
     */
	public static boolean hasContainsfolder(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.has(model, instanceResource, CONTAINSFOLDER);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@2026f3 has at least one value set 
     * @return true if this property has at least one value
	 *
	 * [Generated from RDFReactor template rule #get0has-dynamic] 
     */
	public boolean hasContainsfolder() {
		return Base.has(this.model, this.getResource(), CONTAINSFOLDER);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@2026f3 has the given value (maybe among other values).  
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be checked
     * @return true if this property contains (maybe among other) the given value
	 *
	 * [Generated from RDFReactor template rule #get0has-value-static] 
     */
	public static boolean hasContainsfolder(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value ) {
		return Base.hasValue(model, instanceResource, CONTAINSFOLDER);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@2026f3 has the given value (maybe among other values).  
	 * @param value the value to be checked
     * @return true if this property contains (maybe among other) the given value
	 *
	 * [Generated from RDFReactor template rule #get0has-value-dynamic] 
     */
	public boolean hasContainsfolder( org.ontoware.rdf2go.model.node.Node value ) {
		return Base.hasValue(this.model, this.getResource(), CONTAINSFOLDER);
	}

     /**
     * Get all values of property Containsfolder as an Iterator over RDF2Go nodes 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ClosableIterator of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get7static] 
     */
	public static ClosableIterator<org.ontoware.rdf2go.model.node.Node> getAllContainsfolder_asNode(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_asNode(model, instanceResource, CONTAINSFOLDER);
	}
	
    /**
     * Get all values of property Containsfolder as a ReactorResult of RDF2Go nodes 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a List of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get7static-reactor-result] 
     */
	public static ReactorResult<org.ontoware.rdf2go.model.node.Node> getAllContainsfolder_asNode_(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_as(model, instanceResource, CONTAINSFOLDER, org.ontoware.rdf2go.model.node.Node.class);
	}

    /**
     * Get all values of property Containsfolder as an Iterator over RDF2Go nodes 
     * @return a ClosableIterator of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get8dynamic] 
     */
	public ClosableIterator<org.ontoware.rdf2go.model.node.Node> getAllContainsfolder_asNode() {
		return Base.getAll_asNode(this.model, this.getResource(), CONTAINSFOLDER);
	}

    /**
     * Get all values of property Containsfolder as a ReactorResult of RDF2Go nodes 
     * @return a List of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get8dynamic-reactor-result] 
     */
	public ReactorResult<org.ontoware.rdf2go.model.node.Node> getAllContainsfolder_asNode_() {
		return Base.getAll_as(this.model, this.getResource(), CONTAINSFOLDER, org.ontoware.rdf2go.model.node.Node.class);
	}
     /**
     * Get all values of property Containsfolder     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ClosableIterator of $type
	 *
	 * [Generated from RDFReactor template rule #get11static] 
     */
	public static ClosableIterator<BookmarkFolder> getAllContainsfolder(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll(model, instanceResource, CONTAINSFOLDER, BookmarkFolder.class);
	}
	
    /**
     * Get all values of property Containsfolder as a ReactorResult of BookmarkFolder 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ReactorResult of $type which can conveniently be converted to iterator, list or array
	 *
	 * [Generated from RDFReactor template rule #get11static-reactorresult] 
     */
	public static ReactorResult<BookmarkFolder> getAllContainsfolder_as(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_as(model, instanceResource, CONTAINSFOLDER, BookmarkFolder.class);
	}

    /**
     * Get all values of property Containsfolder     * @return a ClosableIterator of $type
	 *
	 * [Generated from RDFReactor template rule #get12dynamic] 
     */
	public ClosableIterator<BookmarkFolder> getAllContainsfolder() {
		return Base.getAll(this.model, this.getResource(), CONTAINSFOLDER, BookmarkFolder.class);
	}

    /**
     * Get all values of property Containsfolder as a ReactorResult of BookmarkFolder 
     * @return a ReactorResult of $type which can conveniently be converted to iterator, list or array
	 *
	 * [Generated from RDFReactor template rule #get12dynamic-reactorresult] 
     */
	public ReactorResult<BookmarkFolder> getAllContainsfolder_as() {
		return Base.getAll_as(this.model, this.getResource(), CONTAINSFOLDER, BookmarkFolder.class);
	}
 
    /**
     * Adds a value to property Containsfolder as an RDF2Go node 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #add1static] 
     */
	public static void addContainsfolder( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.add(model, instanceResource, CONTAINSFOLDER, value);
	}
	
    /**
     * Adds a value to property Containsfolder as an RDF2Go node 
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #add1dynamic] 
     */
	public void addContainsfolder( org.ontoware.rdf2go.model.node.Node value) {
		Base.add(this.model, this.getResource(), CONTAINSFOLDER, value);
	}
    /**
     * Adds a value to property Containsfolder from an instance of BookmarkFolder 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #add3static] 
     */
	public static void addContainsfolder(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, BookmarkFolder value) {
		Base.add(model, instanceResource, CONTAINSFOLDER, value);
	}
	
    /**
     * Adds a value to property Containsfolder from an instance of BookmarkFolder 
	 *
	 * [Generated from RDFReactor template rule #add4dynamic] 
     */
	public void addContainsfolder(BookmarkFolder value) {
		Base.add(this.model, this.getResource(), CONTAINSFOLDER, value);
	}
  

    /**
     * Sets a value of property Containsfolder from an RDF2Go node.
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be set
	 *
	 * [Generated from RDFReactor template rule #set1static] 
     */
	public static void setContainsfolder( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.set(model, instanceResource, CONTAINSFOLDER, value);
	}
	
    /**
     * Sets a value of property Containsfolder from an RDF2Go node.
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set1dynamic] 
     */
	public void setContainsfolder( org.ontoware.rdf2go.model.node.Node value) {
		Base.set(this.model, this.getResource(), CONTAINSFOLDER, value);
	}
    /**
     * Sets a value of property Containsfolder from an instance of BookmarkFolder 
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set3static] 
     */
	public static void setContainsfolder(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, BookmarkFolder value) {
		Base.set(model, instanceResource, CONTAINSFOLDER, value);
	}
	
    /**
     * Sets a value of property Containsfolder from an instance of BookmarkFolder 
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set4dynamic] 
     */
	public void setContainsfolder(BookmarkFolder value) {
		Base.set(this.model, this.getResource(), CONTAINSFOLDER, value);
	}
  


    /**
     * Removes a value of property Containsfolder as an RDF2Go node 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove1static] 
     */
	public static void removeContainsfolder( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.remove(model, instanceResource, CONTAINSFOLDER, value);
	}
	
    /**
     * Removes a value of property Containsfolder as an RDF2Go node
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove1dynamic] 
     */
	public void removeContainsfolder( org.ontoware.rdf2go.model.node.Node value) {
		Base.remove(this.model, this.getResource(), CONTAINSFOLDER, value);
	}
    /**
     * Removes a value of property Containsfolder given as an instance of BookmarkFolder 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove3static] 
     */
	public static void removeContainsfolder(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, BookmarkFolder value) {
		Base.remove(model, instanceResource, CONTAINSFOLDER, value);
	}
	
    /**
     * Removes a value of property Containsfolder given as an instance of BookmarkFolder 
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove4dynamic] 
     */
	public void removeContainsfolder(BookmarkFolder value) {
		Base.remove(this.model, this.getResource(), CONTAINSFOLDER, value);
	}
  
    /**
     * Removes all values of property Containsfolder     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #removeall1static] 
     */
	public static void removeAllContainsfolder( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		Base.removeAll(model, instanceResource, CONTAINSFOLDER);
	}
	
    /**
     * Removes all values of property Containsfolder	 *
	 * [Generated from RDFReactor template rule #removeall1dynamic] 
     */
	public void removeAllContainsfolder() {
		Base.removeAll(this.model, this.getResource(), CONTAINSFOLDER);
	}
 }