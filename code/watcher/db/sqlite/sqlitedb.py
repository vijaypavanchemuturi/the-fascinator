import os  
import datetime, time
   
#from pysqlite2 import dbapi2 as sqlite3 # if using python 2.4  
import sqlite3  # if using python 2.5 or greater

#NOTE: sqlite has issue with multithreading... sample error:
#SQLite objects created in a thread can only be used in that same thread.The object was created in thread id 1101532080 and this is thread id 1269304240
#to fix: add check_same_thread=False param


class Database(object):
    def __init__(self, dbFile=None):
        
        if dbFile is None:
            homeDir = os.path.expanduser("~")
            dbFile = os.path.join(homeDir, "queue.db")
        
        if not os.path.exists(dbFile):
            self.__db = sqlite3.connect(dbFile, check_same_thread=False)
            self.__setupDefaultData()    
        else:
            self.__db = sqlite3.connect(dbFile, check_same_thread=False)
            
    def __setupDefaultData(self):
        """Create default Queue Table"""
        sqlStr = """CREATE TABLE queue
                    (   file    TEXT     PRIMARY KEY,
                        time    TIMESTAMP,
                        event   TEXT
                    );"""
        self.__executeScript(sqlStr)
    
    def __execute(self, sqlStr):  
        """ execute any SQL statement but no return value given """
        cursor = self.__db.cursor()    
        cursor.execute(sqlStr)  
        self.__db.commit()  
        cursor.close()  
    
    def __executeScript(self, sqlStr):  
         """Execute sql Command"""
         cursor = self.__db.cursor()    
         cursor.executescript(sqlStr)
         self.__db.commit()  
         cursor.close()  
        
    def getRecordDate(self, dateFrom, dateTo):
        sqlStr = "SELECT * FROM queue WHERE time between '%s' and '%s Z'" % (dateFrom, dateTo)
        cursor = self.__db.cursor()
        cursor.execute(sqlStr)
        records = cursor.fetchall()
        cursor.close()
        return records
    
    def select(self, **kwargs):
        """Select record from queue table"""
        sqlStr = "SELECT * FROM queue "
        whereStr = ""
        for key in kwargs:
            whereStr += "%s='%s'" % (key, kwargs[key])
        if whereStr:
            sqlStr = "%s WHERE %s" % (sqlStr, whereStr)
        cursor = self.__db.cursor()
        cursor.execute(sqlStr)  
        records = cursor.fetchall()  
        cursor.close()  
        return records  
    
    def selectLike(self, dirPath):
        """Select record from queue table"""
        sqlStr = "SELECT * FROM queue WHERE file like '%s/" % dirPath.rstrip("/") 
        sqlStr += "%'"
        cursor = self.__db.cursor()
        cursor.execute(sqlStr)  
        records = cursor.fetchall()  
        cursor.close()  
        return records  
    
    def insertDir(self, dirPath):
        """Insert dir record to queue table if not exist"""
        records = self.select(file="%s/" % dirPath.rstrip("/"))
        if records == []:
            sqlStr = "INSERT INTO queue(file, time, event) VALUES ('%s/', '0', 'dir')" % dirPath.rstrip("/")
            self.__execute(sqlStr)
    
    def delete(self, dirPath, isDir=False):
        #in del dir event, need to update the events of all files under this directory
        """Delete dir record in queue table"""
        if isDir:
            dirPath = "%s/" % dirPath.rstrip("/")
        sqlStr = "DELETE FROM queue WHERE file='%s'" % dirPath
        self.__execute(sqlStr)
        
    def deleteDirectory(self, eventName, dirPath, timeStamp):
        records = self.select(file="%s/" % dirPath.rstrip("/"))
        if records != []:
            self.delete(dirPath, isDir=True)
        #get all the child
        records = self.selectLike(dirPath="%s/" % dirPath.rstrip("/"))
        if records:
            for record in records:
                filePath0, timeStamp0, eventName0 = record
                if eventName0 == 'dir':
                    self.delete(filePath0, isDir=True)
                    #recursively check for the children of the subdirectory
                    self.deleteDirectory(eventName, filePath0, timeStamp)
                else:
                    self.insertFile((eventName, filePath0, timeStamp))
            return True
        return False
    
    def insertFile(self, *args, **kwargs):
        update = True
        sqlStr = ""
        if kwargs:
            if kwargs.has_key('update'):
                update = kwargs['update']
        for arg in args:
            eventName, filePath, timeStamp = arg
            if eventName and filePath and timeStamp:
                records = self.select(file=filePath)
                if records != []:
                    #if update:
                    record = records[0]
                    #need to compare the time found in database and modified time of the file
                    filePath0, timeStamp0, eventName0 = record
                    dateStr = timeStamp0[:timeStamp0.find(".")]
                    dbDate = datetime.datetime.strptime(dateStr, '%Y-%m-%d %H:%M:%S')
                    (mode, ino, dev, nlink, uid, gid, size, atime, mtime, ctime) = os.stat(filePath)
                    modTime = time.ctime(mtime) #local modification
                    fsDate = datetime.datetime.strptime(modTime, '%a %b %d %H:%M:%S %Y')
                    if dbDate<=fsDate or update:                        
                        sqlStr = "UPDATE queue SET time='%s', event='%s' WHERE file='%s'" % (timeStamp, eventName, filePath)
                else:
                    sqlStr = "INSERT INTO queue(file, time, event) VALUES ('%s', '%s', '%s')" % (filePath, timeStamp, eventName)
                if sqlStr:
                    self.__execute(sqlStr)
                
    def processEvent(self, *args, **kwargs):
        if args:
            args = args[0]
        for arg in args:
            eventName, filePath, timeStamp = arg
            if eventName == "dir":
                self.insertDir(filePath)
            elif eventName == "del":
                #check if it's a directory in the queue table
                if not self.deleteDirectory(eventName, filePath, timeStamp):
                    self.delete(filePath)
            else:
                self.insertFile(arg)
