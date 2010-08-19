from java.lang import Exception

class DeleteData:

    def __init__(self):
        self.writer = response.getPrintWriter("text/html; charset=UTF-8")

        if page.authentication.is_logged_in() and page.authentication.is_admin():
            self.process()
        else:
            self.throw_error("Only administrative users can access this feature")

    def process(self):
        record = formData.get("record")
        try:
            Services.storage.removeObject(record)
            Services.indexer.remove(record)
            Services.indexer.annotateRemove(record)
            self.writer.println(record)
            self.writer.close()
        except Exception, e:
            self.throw_error("Error deleting object: " + e.getMessage())

    def throw_error(self, message):
        response.setStatus(500)
        self.writer.println("Error: " + message)
        self.writer.close()

scriptObject = DeleteData()
