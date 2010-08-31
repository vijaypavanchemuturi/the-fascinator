
class BatchProcess:

    def __init__(self):
        self.formRenderer = toolkit.getFormRenderer()

    def render_upload_form(self):
        uploadForm = "<form id='general-form' method='post' action='batchprocess'>\n" \
                "<fieldset class='login'>\n" \
                "<legend>Batch Update configuration  File</legend>\n"
        uploadForm += self.formRenderer.ajaxFluidErrorHolder("upload-file") + "<p>\n"
        uploadForm += self.formRenderer.renderFormElement("config-file", "text", "Location of the configuration file:") + "</p>\n"
        
        uploadForm += self.formRenderer.renderFormElement("upload", "button", "", "Batch Update")
        uploadForm += self.formRenderer.renderFormElement("cancel", "button", "", "Cancel")
        uploadForm += self.formRenderer.ajaxProgressLoader("upload-file")
        uploadForm += "</div></fieldset></form>\n"
        
        return uploadForm

scriptObject = BatchProcess()
