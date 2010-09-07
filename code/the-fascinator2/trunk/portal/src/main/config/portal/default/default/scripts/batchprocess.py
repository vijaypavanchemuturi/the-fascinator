from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.common import JsonConfigHelper
from java.io import ByteArrayInputStream, ByteArrayOutputStream

import os

class BatchProcess:

    def __init__(self):
        self.formRenderer = toolkit.getFormRenderer()
        func = formData.get("func")
        if func == "num-modified":
            writer = response.getPrintWriter("text/plain")
            writer.println(self.numberOfModifiedRecord())
            writer.close()

    def renderUpdateForm(self):
        return self.__createBatchForm("update")
    
    def renderExportForm(self):
        return self.__createBatchForm("export")
    
    def __createBatchForm(self, processName):
        form = "<form id='%s-form' method='post'>\n" \
                "<fieldset class='login'>\n" \
                "<legend>Batch %s script file</legend>\n" % (processName, processName)
        form += self.formRenderer.ajaxFluidErrorHolder("%s-script-file" % processName) + "\n"
        
        if self.__scriptList(processName) != {}:
            form += self.formRenderer.renderFormSelect("%s-script-file" % processName, \
                    "Batch %s script:" % processName, self.__scriptList(processName))
            form += "<div><br/>"
            form += self.formRenderer.renderFormElement("%s-upload" % processName, "button", "", "Batch %s" % processName)
            form += self.formRenderer.renderFormElement("%s-cancel" % processName, "button", "", "Cancel")
            form += self.formRenderer.ajaxProgressLoader("%s-script-file" % processName)
            form += "</div>"
        else:
            form += "<div>There is not script available to do batch processing.</div>"
        form += "</fieldset></form>\n"
        return form
    
    def __scriptList(self, processName):
        scriptDir = "%s/batch-process/%s" % (os.environ.get("TF_HOME"), processName)
        scriptDic = {}
        if os.path.isdir(scriptDir):
            scriptFiles = os.listdir(scriptDir)
            if scriptFiles:
                for script in scriptFiles:
                    scriptFilePath = "%s/%s" % (scriptDir, script)
                    scriptDic[scriptFilePath] = script
        return scriptDic
    
    def numberOfModifiedRecord(self):
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
        
        req = SearchRequest("modified:true")
        req.setParam("fq", 'item_type:"object"')
        if portalQuery:
            req.addParam("fq", portalQuery)
        if portalSearchQuery:
            req.addParam("fq", portalSearchQuery)
        req.addParam("fq", "")
        req.setParam("rows", "0")
        if not page.authentication.is_admin():
            req.addParam("fq", security_query)
        out = ByteArrayOutputStream()
        indexer.search(req, out)
        
        self.__result = JsonConfigHelper(ByteArrayInputStream(out.toByteArray()))
        return self.__result.get("response/numFound")
    
    #http://localhost:9997/solr/fascinator/select?start=0&fq=item_type%3A%22object%22&rows=25&q=modified:true

scriptObject = BatchProcess()
