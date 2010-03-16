class UploadedData:

    def __init__(self):
        self.file_name = formData.get("upload-file-file")
        if self.file_name is None:
            self.has_upload = False
        else:
            self.has_upload = True
            self.file_details = sessionState.get(self.file_name)

    def get_error(self):
        if self.file_details.get("error") is None:
            return ""
        else:
            return self.file_details.get("error")

    def get_oid(self):
        if self.file_details.get("oid") is None:
            return ""
        else:
            return self.file_details.get("oid")

    def get_workflow(self):
        return formData.get("upload-file-workflow")

    def has_error(self):
        if self.file_details.get("error") is None:
            return False
        else:
            return True

    def is_valid(self):
        return self.has_upload

    def is_pending(self):
        object = self.getObject()
        metaProps = object.getMetadata()
        status = metaProps.get("render-pending")
        if status is None or status == "false":
            return False
        else:
            return True

    def upload_details(self):
        return self.file_details

scriptObject = UploadedData()
