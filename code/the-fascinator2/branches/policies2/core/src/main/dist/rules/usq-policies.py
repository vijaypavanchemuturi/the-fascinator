import md5, os, time
from au.edu.usq.fascinator.indexer.rules import AddField, New
from org.dom4j.io import SAXReader

from au.edu.usq.fascinator.common.nco import Contact
from au.edu.usq.fascinator.common.nfo import PaginatedTextDocument
from au.edu.usq.fascinator.common.nie import InformationElement

#
# Available objects:
#    indexer   : Indexer instance
#    rules     : RuleManager instance
#    object    : DigitalObject to index
#    payloadId : Payload identifier
#    storageId : Storage layer identifier
#

def indexPath(name, path, includeLastPart=False):
    parts = path.split("/")
    length = len(parts)
    if includeLastPart:
        length +=1
    for i in range(1, length):
        part = "/".join(parts[:i])
        if part != "":
            if part.startswith("/"):
                part = part[1:]
            rules.add(AddField(name, part))

def indexList(name, values):
    for value in values:
        if name in ["usq_document_policy_type", "usq_document_div_dept_office", "usq_document_responsible_position", "usq_document_strategic_plan"]:
            indexPath(name, value, True)
        elif name in ["usq_document_next_reviewdate", "usq_document_effective_date"]:
            try:
                value = value.replace("/", "-")
                indexPath(name, time.strftime("%Y/%B", time.strptime(value, "%Y-%m-%d")), True)
                rules.add(AddField("date_" + name, value + "T00:00:00Z")) # add field for date querying
                rules.add(AddField("str_" + name, value))
            except:
                pass
        else:
            rules.add(AddField(name, value))

def indexTitleGroup(name, values):
    for value in values:
        firstChar = ord(value.strip().lower()[:1])
        ruleValue = " Others"
        if firstChar in range (97, 102):
            ruleValue = "A-E"
        elif firstChar in range (102, 107):
            ruleValue = "F-J"
        elif firstChar in range (107, 112):
            ruleValue = "K-O"
        elif firstChar in range (112, 117):
            ruleValue = "P-T"
        elif firstChar in range (117, 173):
            ruleValue = "U-Z"
        rules.add(AddField(name, ruleValue))

def getNodeValues (doc, xPath):
    nodes = doc.selectNodes(xPath)
    valueList = []
    if nodes:
        for node in nodes:
            #remove duplicates:
            nodeValue = node.getTextTrim()
            if nodeValue not in valueList:
                valueList.append(nodeValue)
    return valueList 

# start with blank solr document
rules.add(New())

# common fields

path, filename = os.path.split(object.getId())
basename, ext = os.path.splitext(filename)
solrId = basename.lower().replace(" ", "-").replace(".", "")
if isMetadata:
    itemType = "object"
else:
    solrId += "/" + payloadId
    itemType = "datastream"
    rules.add(AddField("identifier", payloadId))

rules.add(AddField("id", solrId))
rules.add(AddField("storage_id", storageId))
rules.add(AddField("item_type", itemType))
rules.add(AddField("last_modified", time.strftime("%Y-%m-%dT%H:%M:%SZ")))
rules.add(AddField("group_access", "guest"))

if isMetadata:
    # only need to index metadata for the main object
    rules.add(AddField("source_name", params["source.name"]))
    rules.add(AddField("source_type", params["source.type"]))
    
    titleList = []
    descriptionList = []
    creatorList = []
    creationDate = []
    contributorList = [] 
    approverList = []  
    formatList = []
    fulltext = []
    relationDict = {}
    
    # check if dc.xml returned from ice is exist or not. if not... process the dc-rdf
    dcPayload = object.getPayload("dc.xml")
    if dcPayload is not None:
        indexer.registerNamespace("dc", "http://purl.org/dc/elements/1.1/")
        dcXml = indexer.getXmlDocument(dcPayload)
        if dcXml is not None:
            #get Title
            titleList = getNodeValues(dcXml, "//dc:title")
            #get abstract/description 
            descriptionList = getNodeValues(dcXml, "//dc:description")
            #get creator
            creatorList = getNodeValues(dcXml, "//dc:creator")
            #get contributor list
            contributorList = getNodeValues(dcXml, "//dc:contributor")
            #get creation date
            creationDate = getNodeValues(dcXml, "//dc:issued")
            #ice metadata stored in dc:relation as key::value
            relationList = getNodeValues(dcXml, "//dc:relation")
            for relation in relationList:
                key, value = relation.split("::")
                key = key.replace("_5f","") #ICE encoding _ as _5f?
                if relationDict.has_key(key):
                    relationDict[key].append(value)
                else:
                    relationDict[key] = [value]
    
    rdfPayload = object.getMetadata()
    if rdfPayload is not None:
        rdfModel = indexer.getRdfModel(rdfPayload)
        
        #Seems like aperture only encode the spaces. Tested against special characters file name
        #and it's working 
        oid = object.getId().replace(" ", "%20")
        if not oid.startswith("/"):
            oid = "/" + oid
        rdfId = "file:%s" % oid
        
        #Set write to False so it won't write to the model
        paginationTextDocument = PaginatedTextDocument(rdfModel, rdfId, False)
        informationElement = InformationElement(rdfModel, rdfId, False)
        
        #1. get title only if no title returned by ICE
        if titleList == []:
            allTitles = informationElement.getAllTitle();
            while (allTitles.hasNext()):
                title = allTitles.next().strip()
                if title != "":
                    titleList.append(title)
        
        #use id/filename if no title
        if titleList == []:
           title = os.path.split(object.getId())[1]
           titleList.append(title)
        
        #2. get creator only if no creator returned by ICE
        if creatorList == []:
            allCreators = paginationTextDocument.getAllCreator();
            while (allCreators.hasNext()):
                thing = allCreators.next()
                contacts = Contact(rdfModel, thing.getResource(), False)
                allFullnames = contacts.getAllFullname()
                while (allFullnames.hasNext()):
                     creatorList.append(allFullnames.next())
        
        #3. getFullText: only aperture has this information
        if informationElement.hasPlainTextContent():
            allPlainTextContents = informationElement.getAllPlainTextContent()
            while(allPlainTextContents.hasNext()):
                fulltextString = allPlainTextContents.next()
                fulltext.append(fulltextString)
                
                #4. description/abstract will not be returned by aperture, so if no description found
                # in dc.xml returned by ICE, put first 100 characters
                if descriptionList == []:
                    descriptionString = fulltextString
                    if len(fulltextString)>100:
                        descriptionString = fulltextString[:100] + "..."
                    descriptionList.append(descriptionString)
        
        #5. mimeType: only aperture has this information
        if informationElement.hasMimeType():
            allMimeTypes = informationElement.getAllMimeType()
            while(allMimeTypes.hasNext()):
                formatList.append(allMimeTypes.next())
    
        #6. contentCreated
        if creationDate == []:
            if informationElement.hasContentCreated():
                creationDate.append(informationElement.getContentCreated().getTime().toString())
    
    indexList("dc_title", titleList)
    indexList("dc_creator", creatorList)  #no dc_author in schema.xml, need to check
    indexList("dc_contributor", contributorList)
    indexList("dc_description", descriptionList)
    indexList("dc_format", formatList)
    indexList("dc_date", creationDate)
    
    for key in relationDict:
        indexList(key, relationDict[key])
    
    indexList("full_text", fulltext)
    indexPath("file_path", object.getId().replace("\\", "/"))
    indexTitleGroup("title_group", titleList)
