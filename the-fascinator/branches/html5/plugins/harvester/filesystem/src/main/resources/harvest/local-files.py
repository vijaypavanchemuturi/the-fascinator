import md5, os, time

from au.edu.usq.fascinator.api.storage import StorageException
from au.edu.usq.fascinator.indexer.rules import AddField, New

#
# Available objects:
#    indexer    : Indexer instance
#    log        : Logger instance
#    jsonConfig : JsonConfigHelper of our harvest config file
#    rules      : RuleManager instance
#    object     : DigitalObject to index
#    payload    : Payload to index
#    params     : Metadata Properties object
#    pyUtils    : Utility object for accessing app logic
#

#
# Index a path separated value as multiple field values
#
def indexPath(name, path, includeLastPart=True):
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
        rules.add(AddField(name, value))

def getNodeValues (doc, xPath):
    nodes = doc.selectNodes(xPath)
    valueList = []
    if nodes:
        for node in nodes:
            #remove duplicates:
            nodeValue = node.getText()
            if nodeValue not in valueList:
                valueList.append(node.getText())
    return valueList 

#start with blank solr document
rules.add(New())

#common fields
oid = object.getId()
pid = payload.getId()
metaPid = params.getProperty("metaPid", "DC")
if pid == metaPid:
    itemType = "object"
else:
    oid += "/" + pid
    itemType = "datastream"
    rules.add(AddField("identifier", pid))

rules.add(AddField("id", oid))
rules.add(AddField("storage_id", oid))
rules.add(AddField("item_type", itemType))
rules.add(AddField("last_modified", time.strftime("%Y-%m-%dT%H:%M:%SZ")))
rules.add(AddField("harvest_config", params.getProperty("jsonConfigOid")))
rules.add(AddField("harvest_rules",  params.getProperty("rulesOid")))

# Security
roles = pyUtils.getRolesWithAccess(oid)
if roles is not None:
    for role in roles:
        rules.add(AddField("security_filter", role))
else:
    # Default to guest access if Null object returned
    schema = pyUtils.getAccessSchema("derby");
    schema.setRecordId(oid)
    schema.set("role", "guest")
    pyUtils.setAccessSchema(schema, "derby")
    rules.add(AddField("security_filter", "guest"))

if pid == metaPid:
    for payloadId in object.getPayloadIdList():
        try:
            payload = object.getPayload(payloadId)
            if str(payload.getType())=="Thumbnail":
                rules.add(AddField("thumbnail", payload.getId()))
            elif str(payload.getType())=="Preview":
                rules.add(AddField("preview", payload.getId()))
            elif str(payload.getType())=="AltPreview":
                rules.add(AddField("altpreview", payload.getId()))
        except Exception, e:
            pass
    #only need to index metadata for the main object
    rules.add(AddField("repository_name", params["repository.name"]))
    rules.add(AddField("repository_type", params["repository.type"]))
    
    titleList = []
    descriptionList = []
    creatorList = []
    creationDate = []
    contributorList = [] 
    approverList = []  
    formatList = []
    fulltext = []
    relationDict = {}
    
    ### Check if dc.xml returned from ice is exist or not. if not... process the dc-rdf
    try:
        dcPayload = object.getPayload("dc.xml")
        pyUtils.registerNamespace("dc", "http://purl.org/dc/elements/1.1/")
        dcXml = pyUtils.getXmlDocument(dcPayload)
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
                value = value.strip()
                key = key.replace("_5f","") #ICE encoding _ as _5f?
                if relationDict.has_key(key):
                    relationDict[key].append(value)
                else:
                    relationDict[key] = [value]
    except StorageException, e:
        #print "Failed to index ICE dublin core data (%s)" % str(e)
        pass
    
    # Extract from aperture.rdf if exist
    try:
        from au.edu.usq.fascinator.common.nco import Contact
        from au.edu.usq.fascinator.common.nfo import PaginatedTextDocument
        from au.edu.usq.fascinator.common.nid3 import ID3Audio
        from au.edu.usq.fascinator.common.nie import InformationElement
        
        rdfPayload = object.getPayload("aperture.rdf")
        rdfModel = pyUtils.getRdfModel(rdfPayload)
        
        #Seems like aperture only encode the spaces. Tested against special characters file name
        #and it's working 
        safeOid = oid.replace(" ", "%20")
        #under windows we need to add a slash
        if not safeOid.startswith("/"):
            safeOid = "/" + safeOid
        rdfId = "file:%s" % safeOid
        
        #Set write to False so it won't write to the model
        paginationTextDocument = PaginatedTextDocument(rdfModel, rdfId, False)
        informationElement = InformationElement(rdfModel, rdfId, False)
        id3Audio = ID3Audio(rdfModel, rdfId, False)
        
        #1. get title only if no title returned by ICE
        if titleList == []:
            allTitles = informationElement.getAllTitle();
            while (allTitles.hasNext()):
                title = allTitles.next().strip()
                if title != "":
                    titleList.append(title)
            allTitles = id3Audio.getAllTitle()
            while (allTitles.hasNext()):
                title = allTitles.next().strip()
                if title != "":
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
        
        if id3Audio.hasAlbumTitle():
            albumTitle = id3Audio.getAllAlbumTitle().next().strip()
            descriptionList.append("Album: " + albumTitle)
        
        #5. mimeType: only aperture has this information
        if informationElement.hasMimeType():
            allMimeTypes = informationElement.getAllMimeType()
            while(allMimeTypes.hasNext()):
                formatList.append(allMimeTypes.next())
    
        #6. contentCreated
        if creationDate == []:
            if informationElement.hasContentCreated():
                creationDate.append(informationElement.getContentCreated().getTime().toString())
    except StorageException, e:
        #print "Failed to index aperture data (%s)" % str(e)
        pass

    ### Check if ffmpeg.info exists or not
    try:
        ffmpegPayload = object.getPayload("ffmpeg.info")
        ffmpeg = pyUtils.getJsonObject(ffmpegPayload.open())
        ffmpegPayload.close()
        if ffmpeg is not None:
            # Dimensions
            width = ffmpeg.get("video/width")
            height = ffmpeg.get("video/height")
            if width is not None and height is not None:
                rules.add(AddField("dc_size", width + " x " + height))

            # Duration
            duration = ffmpeg.get("duration")
            if duration is not None and int(duration) > 0:
                if int(duration) > 59:
                    secs = int(duration) % 60
                    mins = (int(duration) - secs) / 60
                    rules.add(AddField("dc_duration", "%dm %ds" % (mins, secs)))
                else:
                    rules.add(AddField("dc_duration", duration + " second(s)"))

            # Format
            media = ffmpeg.get("format/label")
            if media is not None:
                rules.add(AddField("dc_media_format", media))

            # Video
            codec = ffmpeg.get("video/codec/simple")
            label = ffmpeg.get("video/codec/label")
            if codec is not None and label is not None:
                rules.add(AddField("video_codec_simple", codec))
                rules.add(AddField("video_codec_label", label))
                rules.add(AddField("meta_video_codec", label + " (" + codec + ")"))
            else:
                if codec is not None:
                    rules.add(AddField("video_codec_simple", codec))
                    rules.add(AddField("meta_video_codec", codec))
                if label is not None:
                    rules.add(AddField("video_codec_label", label))
                    rules.add(AddField("meta_video_codec", label))
            pixel_format = ffmpeg.get("video/pixel_format")
            if pixel_format is not None:
                rules.add(AddField("meta_video_pixel_format", pixel_format))

            # Audio
            codec = ffmpeg.get("audio/codec/simple")
            label = ffmpeg.get("audio/codec/label")
            if codec is not None and label is not None:
                rules.add(AddField("audio_codec_simple", codec))
                rules.add(AddField("audio_codec_label", label))
                rules.add(AddField("meta_audio_codec", label + " (" + codec + ")"))
            else:
                if codec is not None:
                    rules.add(AddField("audio_codec_simple", codec))
                    rules.add(AddField("meta_audio_codec", codec))
                if label is not None:
                    rules.add(AddField("audio_codec_label", label))
                    rules.add(AddField("meta_audio_codec", label))
            sample_rate = ffmpeg.get("audio/sample_rate")
            if sample_rate is not None:
                sample_rate = "%.1f KHz" % (int(sample_rate) / 1000)
            channels = ffmpeg.get("audio/channels")
            if channels is not None:
                channels += " Channel(s)"
            if sample_rate is not None and channels is not None:
                rules.add(AddField("meta_audio_details", sample_rate + ", " + channels))
            else:
                if sample_rate is not None:
                    rules.add(AddField("meta_audio_details", sample_rate))
                if channels is not None:
                    rules.add(AddField("meta_audio_details", channels))
    except StorageException, e:
        #print "Failed to index FFmpeg metadata (%s)" % str(e)
        pass

    # some defaults if the above failed
    if titleList == []:
       #use object's source id (i.e. most likely a filename)
       titleList.append(object.getSourceId())
    
    if formatList == []:
        payload = object.getPayload(object.getSourceId())
        if payload.getContentType():
            formatList.append(payload.getContentType())
        else:
            formatList.append("Unknown")
    
    indexList("dc_title", titleList)
    indexList("dc_creator", creatorList)  #no dc_author in schema.xml, need to check
    indexList("dc_contributor", contributorList)
    indexList("dc_description", descriptionList)
    indexList("dc_format", formatList)
    indexList("dc_date", creationDate)
    
    for key in relationDict:
        indexList(key, relationDict[key])
    
    indexList("full_text", fulltext)
    baseFilePath = params["base.file.path"]
    filePath = object.getMetadata().getProperty("file.path")
    if baseFilePath:
        #NOTE: need to change again if the json file accept forward slash in window
        #get the base folder
        baseFilePath = baseFilePath.replace("\\", "/")
        if not baseFilePath.endswith("/"):
           baseFilePath = "%s/" % baseFilePath
        baseDir = baseFilePath[baseFilePath.rstrip("/").rfind("/")+1:]
        filePath = filePath.replace("\\", "/").replace(baseFilePath, baseDir)
    indexPath("file_path", filePath, includeLastPart=False)
    
    