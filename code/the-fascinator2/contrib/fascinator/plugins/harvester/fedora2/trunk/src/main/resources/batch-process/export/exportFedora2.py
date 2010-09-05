from fedora.client import FedoraClient
from org.apache.commons.io import IOUtils
from au.edu.usq.fascinator.harvester.fedora.restclient import FedoraRestClient
from java.io import File

print "** Export to Fedora2 (draft)"

client = FedoraClient("http://localhost:8001/fedora/", "fedoraAdmin", "fedoraAdmin")
apim = client.getAPIM()
restClient = FedoraRestClient("http://localhost:8001")

#Delete the payload that is supposed to be deleted
props = object.getMetadata()
e = props.propertyNames()
while (e.hasMoreElements()):
    key = str(e.nextElement())
    value = props.getProperty(key)
    if key.startswith("payloadToDelete."):
        #Notify fedora to delete this datastream
        print " ** Deleting datastream: ", value
        apim.purgeDatastream(object.getId(), value, None, None, "Deleting from fascinator", False)
        
        #Notify fascinator to delete the payload
        try:
            pass
            #object.removePayload(props.getProperty(key))
        except:
            print "fail to delete payload"
        props.remove(key)
    if key.startswith("payloadToExport."):
        #Notify fedora to add this datastream
        print " ** Deleting datastream: ", value
        
        payload = object.getPayload(value)
        #Check if object exist
        objectResult = restClient.findObjects(object.getId(), 1)
        print "result is empty: ", objectResult.getObjectFields().isEmpty()
        if not objectResult.getObjectFields().isEmpty():
            try:
                mimeType = payload.getContentType()
                formatURI = None
                dsLocation = None
                controlGroup = "M"  # get original instead of setting
                dsState = "A"  # get original instead of setting
                #Check if datastream exist
                datastream = apim.getDatastream(object.getId(), value, None)
                if datastream:
                    byteArrayOfDSContent=IOUtils.toByteArray(payload.open())
                    apim.modifyDatastreamByValue(object.getId(), value, None, value, mimeType, \
                                                 formatURI, byteArrayOfDSContent, None, None, \
                                                 "Updating datastream from Fascinator", False)
                else:
                    f = File.createTempFile("fedora-", ".tmp")
                    fOut = FileOutputStream()
                    IOUtils.copy(payload.open(), fOut)
                    payload.close()
                    fOut.close()
                    dsLocation = client.uploadFile(f)
                    #add new datastream
                    apim.addDatastream(object.getId(), value, None, mimeType, True, mimeType, \
                                       formatURI, dsLocation, controlGroup, dsState, None, None, \
                                       "Creating new datastream")
            except:
                #Remote exception
                pass
        else:
            formatURI = None
            dsLocation = None
            controlGroup = "M"  # get original instead of setting
            dsState = "A"  # get original instead of setting
            # Create the object and the datastream
            xmlContent = IOUtils.toByteArray(client.getClass().getResourceAsStream("foxml11_template.xml"))
            apim.ingest(xmlContent, "info:fedora/fedora-system:FOXML-1.1", "Creating new object")
            #adding each datastream
            for payload in object.getPayloadList():
                mimeType = payload.getContentType()
                f = File.createTempFile("fedora-", ".tmp")
                fOut = FileOutputStream()
                IOUtils.copy(payload.open(), fOut)
                payload.close()
                fOut.close()
                dsLocation = client.uploadFile(f)
                apim.addDatastream(object.getId(), value, None, mimeType, True, mimeType, \
                                       formatURI, dsLocation, controlGroup, dsState, None, None, \
                                       "Creating new dataastream")
            
            apim.ingest()
        
        props.remove(key)

#reset modified property after export
resetModifiedProperty = props.getProperty("resetModifiedProperty", "false")
if resetModifiedProperty == "true":
    props.setProperty("modified", "false")

object.close()
