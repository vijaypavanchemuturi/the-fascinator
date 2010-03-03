from authentication import Authentication

class DeleteData:

    def __init__(self):
        self.authentication = Authentication()
        self.authentication.session_init()

        self.writer = response.getPrintWriter("text/html")

        if self.authentication.is_logged_in() and self.authentication.is_admin():
            self.process()
        else:
            self.throw_error("Only administrative users can access this feature")

    def process(self):
        record = formData.get("record")
        Services.storage.removeObject(record)
        Services.indexer.remove(record)

        self.writer.println(record)
        self.writer.close()

    def throw_error(self, message):
        response.setStatus(500)
        self.writer.println("Error: " + message)
        self.writer.close()

scriptObject = DeleteData()
