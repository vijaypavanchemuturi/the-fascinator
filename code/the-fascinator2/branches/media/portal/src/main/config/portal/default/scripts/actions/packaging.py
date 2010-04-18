import md5, uuid

from au.edu.usq.fascinator import HarvestClient
from au.edu.usq.fascinator.common import FascinatorHome, JsonConfigHelper

from java.io import File, FileOutputStream, OutputStreamWriter
from java.lang import Exception

from org.apache.commons.io import IOUtils

PACKAGE_NS = "package/selected/"

class PackagingActions:
    
    def __init__(self):
        print "formData=%s" % formData

        result = "{}"
        func = formData.get("func")
        if func == "create-from-selected":
            result = self.__createFromSelected()
        elif func == "update":
            result = self.__update()
        elif func == "clear":
            result = self.__clear()
        
        writer = response.getPrintWriter("application/json; charset=UTF-8")
        writer.println(result)
        writer.close()
    
    def __createFromSelected(self):
        print "Creating package from selected..."

        # create the manifest
        manifest = JsonConfigHelper()
        manifest.set("title", "New package")
        for item in self.__getSelected():
            for id in item.keys():
                hashId = md5.new(id).hexdigest()
                title = item[id]
                manifest.set("manifest/node-%s/id" % hashId, id)
                manifest.set("manifest/node-%s/title" % hashId, title)

        # store the manifest to the know location
        packageDir = FascinatorHome.getPathFile("packages")
        packageDir.mkdirs()

        manifestHash = uuid.uuid4() # random uuid
        manifestFile = File(packageDir, str(manifestHash) + ".tfpackage")
        outStream = FileOutputStream(manifestFile)
        outWriter = OutputStreamWriter(outStream, "UTF-8")
        manifest.store(outWriter, True)
        outWriter.close()

        username = sessionState.get("username")
        if username is None:
            username = "guest" # necessary?
        harvester = None
        try:
            # set up config files if necessary
            workflowsDir = FascinatorHome.getPathFile("workflows")
            configFile = self.__getFile(workflowsDir, "packaging-config.json")
            rulesFile = self.__getFile(workflowsDir, "packaging-rules.py")
            # run the harvest client with our packaging workflow config
            harvester = HarvestClient(configFile, manifestFile, username)
            harvester.start()
            oid = harvester.getUploadOid()
            harvester.shutdown()
        except Exception, ex:
            error = "Packager workflow failed: %s" % str(ex)
            log.error(error, ex)
            if harvester is not None:
                harvester.shutdown()
            return '{ status: "failed" }'

        # clean up
        manifestFile.delete()
        self.__clear()
        
        return '{ status: "ok", url: "%s/workflow/%s" }' % (portalPath, oid)
    
    def __update(self):
        print "Updating package selection..."
        ids = formData.getValues("ids")
        key = PACKAGE_NS + formData.get("page")
        if ids:
            titles = formData.getValues("titles")
            selected = []
            for i in range(len(ids)):
                selected.append({ ids[i]: titles[i] })
            sessionState.set(key, selected)
        else:
            sessionState.remove(key)
        return '{ count: %s }' % self.__getCount()
    
    def __clear(self):
        print "Clearing package selection..."
        for key in self.__getKeys():
            sessionState.remove(key)
        return "{}"
    
    def __getSelected(self):
        selected = []
        for key in self.__getKeys():
            selected.extend(sessionState.get(key))
        return selected
    
    def __getCount(self):
        count = 0
        for key in self.__getKeys():
            count += len(sessionState.get(key))
        return count
    
    def __getFile(self, packageDir, filename):
        file = File(packageDir, filename)
        if not file.exists():
            out = FileOutputStream(file)
            IOUtils.copy(Services.getClass().getResourceAsStream("/workflows/" + filename), out)
            out.close()
        return file
    
    def __getKeys(self):
        return  [k for k in sessionState.keySet() if k.startswith(PACKAGE_NS)]

scriptObject = PackagingActions()
