



class Config(object):
    # Constructor:
    #   Config(configFileSearchPaths=["."], configFileName="config.json", fileSystem=None)
    # Properties:
    #   settings            (default None dictionary)
    #   configFilePath      (the configuration file that is being used)
    # Methods:
    #   reload()
    #   addReloadWatcher(callback)      ( callback method notify(config) )
    #   removeReloadWatcher(callback)
    #
    class DefaultDict(dict):
        def __missing__(self, key):
            return None

    def __init__(self, configFileSearchPaths=["."], configFileName="config.json",
            fileSystem=None):
        if fileSystem is None:
            fileSystem = FileSystem
        self.__fileSystem = fileSystem
        self.__settings = Config.DefaultDict()
        self.__configFileSearchPaths = configFileSearchPaths
        self.__configFileName = configFileName
        self.__configFilePath = None
        self.__data = None
        self.__watchers = []
        self.reload()

    @property
    def settings(self):
        return self.__settings

    @property
    def configFilePath(self):
        return self.__configFilePath
    
    @property
    def platform(self):
        if self.__settings.has_key("os"):
            return self.__settings["os"]
        return None
    
    @property
    def db(self):
        if self.__settings.has_key("db"):
            return self.__settings["db"]
        return None

    @property
    def watchDirs(self):
        if self.__settings.has_key("watchDirs"):
            return self.__settings["watchDirs"]
        return []

    def reload(self):
        if self.__configFilePath is None:
            self.__configFilePath = self.__getConfigFile()
        #need to do if no config file, create one
        data = self.__fileSystem.readFile(self.__configFilePath)
        if data is None:
            self.__configFilePath = self.__getConfigFile()
            data = self.__fileSystem.readFile(self.__configFilePath)
        if data==self.__data:
            return
        self.__data = data
        try:
            d = eval(data)
            self.__settings.clear()
            self.__settings.update(d)
        except Exception, e:
            msg = "Error loading configFile '%s' - '%s'" % (self.__configFilePath, str(e))
            raise Exception(msg)
        for watcher in self.__watchers:
            try:
                watcher(self)
            except: pass

    def addReloadWatcher(self, callback):
        self.__watchers.append(callback)

    def removeReloadWatcher(self, callback):
        try:
            self.__watchers.remove(callback)
        except: pass

    def __getConfigFile(self):
        self.__configFilePath = None
        for path in self.__configFileSearchPaths:
            file = self.__fileSystem.join(path, self.__configFileName)
            if self.__fileSystem.isFile(file):
                return self.__fileSystem.absPath(file)
        return None


class FileSystem(object):
    # Methods
    #   join(*args)     -> string
    #   absPath(path)   -> string
    #   isFile(file)    -> True|False
    #   readFile(file)  -> data|None

    import os

    @staticmethod
    def join(*args):
        return FileSystem.os.path.join(*args)

    @staticmethod
    def absPath(path):
        return FileSystem.os.path.absPath(path)

    @staticmethod
    def isFile(file):
        return FileSystem.os.path.isfile(file)

    @staticmethod
    def readFile(file):
        if file is None:
            return None
        data = None
        try:
            f = open(file, "rb")
            data = f.read()
            f.close()
        except:
            pass
        return data


