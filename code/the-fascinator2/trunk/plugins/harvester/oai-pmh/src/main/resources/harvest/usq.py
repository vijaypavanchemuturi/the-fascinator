import time

class IndexData:
    def __init__(self):
        pass

    def __activate__(self, context):
        # Prepare variables
        self.index = context["fields"]
        self.object = context["object"]
        self.payload = context["payload"]
        self.params = context["params"]
        self.utils = context["pyUtils"]

        # Common data
        self.__newDoc()

        # Real metadata
        if self.itemType == "object":
            self.__previews()
            self.__basicData()
            self.__metadata()

        # Make sure security comes after workflows
        self.__security()

    def __newDoc(self):
        self.oid = self.object.getId()
        self.pid = self.payload.getId()
        metadataPid = self.params.getProperty("metaPid", "DC")

        if self.pid == metadataPid:
            self.itemType = "object"
        else:
            self.oid += "/" + self.pid
            self.itemType = "datastream"
            self.index.put("identifier", self.pid)

        self.index.put("id", self.oid)
        self.index.put("storage_id", self.oid)
        self.index.put("item_type", self.itemType)
        self.index.put("last_modified", time.strftime("%Y-%m-%dT%H:%M:%SZ"))
        self.index.put("harvest_config", self.params.getProperty("jsonConfigOid"))
        self.index.put("harvest_rules",  self.params.getProperty("rulesOid"))
        self.index.put("display_type", "OaiPmhDC")

    def __basicData(self):
        self.index.put("repository_name", self.params["repository.name"])
        self.index.put("repository_type", self.params["repository.type"])

    def __previews(self):
        self.previewPid = None
        for payloadId in self.object.getPayloadIdList():
            try:
                payload = self.object.getPayload(payloadId)
                if str(payload.getType())=="Thumbnail":
                    self.index.put("thumbnail", payload.getId())
                elif str(payload.getType())=="Preview":
                    self.previewPid = payload.getId()
                    self.index.put("preview", self.previewPid)
                elif str(payload.getType())=="AltPreview":
                    self.index.put("altpreview", payload.getId())
            except Exception, e:
                pass

    def __security(self):
        roles = self.utils.getRolesWithAccess(self.oid)
        if roles is not None:
            for role in roles:
                self.index.put("security_filter", role)
        else:
            # Default to guest access if Null object returned
            schema = self.utils.getAccessSchema("derby");
            schema.setRecordId(self.oid)
            schema.set("role", "guest")
            self.utils.setAccessSchema(schema, "derby")
            self.index.put("security_filter", "guest")

    def __metadata(self):
        self.utils.registerNamespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/")
        self.utils.registerNamespace("dc", "http://purl.org/dc/elements/1.1/")

        dcPayload = self.object.getPayload(self.object.getSourceId())
        dc = self.utils.getXmlDocument(dcPayload.open())
        dcPayload.close()
        nodes = dc.selectNodes("//dc:*")
        for node in nodes:
            name = "dc_" + node.getName()
            text = node.getTextTrim()
            self.index.put(name, text)
            # Make sure we get the title and description just for the Fascanator
            if name == "dc_title":
                self.index.put("title", text)
            if name == "dc_description":
                self.index.put("description", text)
