from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.api.storage import StorageException
from au.edu.usq.fascinator import HarvestClient
from au.edu.usq.fascinator.common import JsonConfigHelper
from java.io import File, ByteArrayInputStream, ByteArrayOutputStream

from org.apache.commons.lang import StringUtils;

class BatchProcess:
    def __init__(self):
        self.writer = response.getPrintWriter("text/html; charset=UTF-8")
        print " *** action batchProcess.py: formData=%s" % formData
        self.storage = Services.getStorage()
        self.__process()
        
    def __process(self):
        func = formData.get("func")
        result = {}
        
        if func == "batch-update":
            updateScriptFile = formData.get("update-script-file")
            self.__checkIfScriptFileIsValid(updateScriptFile)
                
            objectIdList = self.__search("*:*")
            try:
                self.__harvester = HarvestClient()
                for oid in objectIdList:
                    self.__processObject(oid, updateScriptFile, "false")
            except Exception, ex:
                error = "Batch update failed: %s" % str(ex)
                log.error(error, ex)
                return '{ status: "failed" }'
        elif func == "batch-export":
            exportScriptFile = formData.get("export-script-file")
            self.__checkIfScriptFileIsValid(exportScriptFile)
            
            objectIdList = self.__search("modified:true")
            try:
                self.__harvester = HarvestClient()
                for oid in objectIdList:
                    self.__processObject(oid, exportScriptFile, "true")
            except Exception, ex:
                error = "Batch export failed: %s" % str(ex)
                log.error(error, ex)
                return '{ status: "failed" }'
            
        self.writer.println(result)
        self.writer.close()
        
    def __checkIfScriptFileIsValid(self, scriptFile):
        if scriptFile == "":
            result = "Invalid script file"
            self.throw_error("Invalid script file")
        script = File(scriptFile)
        if not script.exists():
            result = "Script file is not exist"
            self.throw_error("Script file is not exist")
    
    def __processObject(self, oid, scriptFile, resetModifiedProperty):
        try:
            print "** processObject: ", oid
            # temporarily update the object properties for transforming
            obj = self.storage.getObject(oid)
            props = obj.getMetadata()
            
            indexOnHarvest = self.__setProperty(props, "indexOnHarvest", "false")
            harvestQueue = self.__setProperty(props, "harvestQueue", "")
            renderQueue = self.__setProperty(props, "renderQueue", "jython")
            props.setProperty("resetModifiedProperty", resetModifiedProperty)
            jythonScript = self.__setProperty(props, "jythonScript", scriptFile)
            
            obj.close()
            # signal a reharvest
            self.__harvester.reharvest(oid);
        except StorageException, se:
            print se
    
    def __setProperty(self, props, key, newValue=None):
        oldValue = props.get(key)
        if oldValue:
            props.setProperty("copyOf_" + key, oldValue)
        if newValue:
            props.setProperty(key, newValue)
        else:
            props.remove(key)
        return oldValue
    
    def __search(self, searchField):
        indexer = Services.getIndexer()
        portalQuery = Services.getPortalManager().get(portalId).getQuery()
        portalSearchQuery = Services.getPortalManager().get(portalId).getSearchQuery()
        
        # Security prep work
        current_user = page.authentication.get_username()
        security_roles = page.authentication.get_roles_list()
        security_filter = 'security_filter:("' + '" OR "'.join(security_roles) + '")'
        security_exceptions = 'security_exception:"' + current_user + '"'
        owner_query = 'owner:"' + current_user + '"'
        security_query = "(" + security_filter + ") OR (" + security_exceptions + ") OR (" + owner_query + ")"
        
        startRow = 0
        numPerPage = 25
        numFound = 0
        
        req = SearchRequest(searchField)
        if portalQuery:
            req.addParam("fq", portalQuery)
        if portalSearchQuery:
            req.addParam("fq", portalSearchQuery)
        if not page.authentication.is_admin():
            req.addParam("fq", security_query)
        
        objectIdList = []
        while True:
            req.addParam("fq", 'item_type:"object"')
            req.addParam("rows", str(numPerPage))
            req.addParam("start", str(startRow))
            
            out = ByteArrayOutputStream()
            indexer.search(req, out)
            result = JsonConfigHelper(ByteArrayInputStream(out.toByteArray()))
            
            docs = result.getList("response/docs/storage_id")
            
            objectIdList.extend(docs)
            
            startRow += numPerPage
            numFound = int(result.get("response/numFound"))
            
            if (startRow > numFound):
                break

        return objectIdList
    
    def throw_error(self, message):
        response.setStatus(500)
        self.writer.println("Error: " + message)
        self.writer.close()
        
scriptObject = BatchProcess()
