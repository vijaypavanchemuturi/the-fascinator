
class DetailData:
    def __init__(self):
        self.__storage = Services.storage

    def getObject(self, id):
        item = self.__storage.getObject(id)
        print item
        return item


scriptObject = DetailData()
