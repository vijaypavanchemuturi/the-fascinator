class UploadedData:

    def __init__(self):
        pass

    def get_plugin(self):
        return formData.get("upload-file-plugin")

    def upload_details(self):
        file_name = formData.get("upload-file-file")
        file_details = sessionState.get(file_name)
        return file_details

scriptObject = UploadedData()
