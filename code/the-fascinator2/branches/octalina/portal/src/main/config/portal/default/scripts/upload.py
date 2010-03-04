class UploadData:

    def __init__(self):
        self.uploader = toolkit.getFileUploader()

    def render_upload_form(self):
        return self.uploader.renderForm()

scriptObject = UploadData()
