import os
from org.apache.commons.io import IOUtils

class DownloadData:
    def __init__(self):
        print request.getPath()
        storage = Services.storage
        oid = formData.get("id")
        pid = formData.get("payload")
        payload = storage.getPayload(oid, pid)
        filename = os.path.split(pid)[1]
        mimeType = payload.contentType
        if mimeType == "application/octet-stream":
            response.setHeader("Content-Disposition", "attachment; filename=%s" % filename)
        out = response.getOutputStream(payload.contentType)
        IOUtils.copy(payload.inputStream, out)
        out.close()

scriptObject = DownloadData()
