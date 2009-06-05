import os  
   
#from pysqlite2 import dbapi2 as sqlite3 # if using python 2.4  
import sqlite3  # if using python 2.5 or greater  

class Database(object):
    def __init__(self, dbFile=None):
        if dbFile is None:
            homeDir = os.path.expanduser("~")
            dbFile = os.path.join(homeDir, "queue.db")
        
        if not os.path.exists(dbFile):
            self.__db = sqlite3.connect(dbFile)
            self.__setupDefaultData()    
        else:
            self.__db = sqlite3.connect(dbFile)
            
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
    
    def insertDir(self, dirPath):
        """Insert dir record to queue table if not exist"""
        records = self.select(file="%s/" % dirPath.rstrip("/"))
        if records == []:
            sqlStr = "INSERT INTO queue(file, time, event) VALUES ('%s/', '0', 'dir')" % dirPath.rstrip("/")
            self.__execute(sqlStr)
    
    def delete(self, dirPath, isDir=False):
        """Delete dir record in queue table"""
        if isDir:
            dirPath = "%s/" % dirPath.rstrip("/")
        sqlStr = "DELETE FROM queue WHERE file='%s'" % dirPath
        self.__execute(sqlStr)
        
    def insertFile(self, filePath, event, timeStamp):
        records = self.select(file=filePath)
        if records != []:
            record = records[0]
            sqlStr = "UPDATE queue SET time='%s', event='%s' WHERE file='%s'" % (timeStamp, event, filePath)
        else:
            sqlStr = "INSERT INTO queue(file, time, event) VALUES ('%s', '%s', '%s')" % (filePath, timeStamp, event)
        self.__execute(sqlStr)
