import urllib

from au.edu.usq.fascinator.api.storage import StorageException

class HeadData:
    def __init__(self):
        pass

    def __activate__(self, context):
        self.md = context["metadata"]
        self.log = context["log"]
        self.object = None

        # Per object init
        self.__flvFlag = None

    def debugMetadata(self):
        print repr(self.md)
        return ""

    def hasFlv(self):
        # If we had a value last time, just use it
        if self.__flvFlag is not None:
            return self.__flvFlag

        # Retrieve the object if we haven't previously
        if self.object is None:
            oid = self.md.get("id")
            try:
                self.object = Services.getStorage().getObject(oid)
            except StorageException, e:
                self.log.error("Error retrieving object: '{}'", oid, e)
                self.__flvFlag = False
                return self.__flvFlag

        # Now look through the object payloads
        payloadIdList = self.object.getPayloadIdList()
        for payloadId in payloadIdList:
            try:
                payload = self.object.getPayload(payloadId)
                mimeType = payload.getContentType()
                if mimeType == "video/x-flv":
                    self.__flvFlag = urllib.quote(payloadId)
                    return self.__flvFlag
            except StorageException, e:
                # Not found, just let the False return below
                pass
        self.__flvFlag = False
        return self.__flvFlag
