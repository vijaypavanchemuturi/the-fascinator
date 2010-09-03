print "** Export to Fedora2 (draft)"

#Delete the payload that is supposed to be deleted
props = object.getMetadata()
e = props.propertyNames()
while (e.hasMoreElements()):
    key = str(e.nextElement())
    if key.startswith("payloadToDelete"):
        #Notify fedora to delete this datastream
        
        #Notify fascinator to delete the payload
        try:
            pass
            #object.removePayload(props.getProperty(key))
        except:
            print "fail to delete payload"
        props.remove(key)
    if key.startswith("payloadToExport"):
        #Notify fedora to add this datastream
        
        props.remove(key)

#reset modified property after export
resetModifiedProperty = props.getProperty("resetModifiedProperty", "false")
if resetModifiedProperty == "true":
    props.setProperty("modified", "false")

object.close()
