import sys
import cPickle
try:
    sys.path.append(parsePath)
    plugin_manifest = __import__(parseLib)
    json = __import__(jsonLib)
except Exception, e:
    print repr(e)

# We want the file path as a python string
pyString = unicode(filePath).encode("utf-8")

# Read the data into memory and unpickle
try:
    FILE = open(pyString, 'rb')
    data = FILE.read()
    iceData = cPickle.loads(data)
    FILE.close()

# Something went awry...
except Exception, e:
    FILE.close()
    iceData = None
    print repr(e)

# Valid ICE data
response.set("guid", None)
if iceData is not None:
    # We only want the top-level manifest
    if iceData.has_key("manifest") and iceData["manifest"] is not None:
        try:
            response.set("guid", iceData["_guid"])
            jsonManifest = iceData["manifest"].asJSON()
            response.set("json", json.write(jsonManifest))
        except Exception, e:
            print repr(e)
