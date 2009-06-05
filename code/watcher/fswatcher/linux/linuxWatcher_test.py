#!/usr/bin/env python

import sys, datetime, os
import unittest
from time import sleep
from unittest import TestCase

sys.path.append("../../common")
from filesystem import FileSystem
from linuxWatcher import *

watchedPath = "../watchDir"

class WatcherTest(TestCase):
    def setup(self):
        pass

    def tearDown(self):
        pass
    
    def testStartWatcherSingleDir(self):
        watchPath = "watchDir1"
        fs = FileSystem(".")
        linuxWatcher = EventWatcherClass(watchPath, fs)
        
    def testStartWatcherMultipleDir(self):
        watchPath = ["watchDir1", "watchDir2"]
        fs = FileSystem(".")
        linuxWatcher = EventWatcherClass(watchPath, fs)
        
    
    def testCreateNewFile(self):
        watchedPath = "watchDir1"
        fs = FileSystem(".")
        
        newFile = fs.absPath("%s/file1" % watchedPath)
        if fs.isFile(newFile):
            fs.delete(newFile)
            
        linuxWatcher = EventWatcherClass(watchedPath, fs)
        fs.writeFile(newFile, "new file 1")
        sleep(1)
        #linuxWatcher.notifier.stop()

        allEvent = linuxWatcher.eventList()
        self.assertEquals(len(allEvent), 1)
        
        event, filePath, time = allEvent[0]
        self.assertEquals(event, 'mod')
        self.assertEquals(filePath, '/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/file1')
        
    def testCreateNewFile2(self):
        watchedPath = ["watchDir1", "watchDir2"]
        fs = FileSystem(".")
        
        newFile = fs.absPath("%s/file1" % watchedPath[0])
        if fs.isFile(newFile):
            fs.delete(newFile)
            
        linuxWatcher = EventWatcherClass(watchedPath, fs)
        fs.writeFile(newFile, "new file 1")
        sleep(1)
        #linuxWatcher.notifier.stop()

        allEvent = linuxWatcher.eventList()
        self.assertEquals(len(allEvent), 1)
        
        event, filePath, time = allEvent[0]
        self.assertEquals(event, 'mod')
        self.assertEquals(filePath, '/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/file1')
        
    #this is the issue with modifying a big file.
    #event time will be fired when the file started to be saved, not after it's saved
    def testTouchingAVeryBigFile(self):
        watchedPath = "watchDir1"
        fs = FileSystem(".")
        
        linuxWatcher = EventWatcherClass(watchedPath, fs)
        newFile = "%s/ubuntu.iso" % watchedPath
        os.utime(newFile, None)
        sleep(1)
        #linuxWatcher.notifier.stop()
        
        allEvent = linuxWatcher.eventList()
        self.assertEquals(len(allEvent), 1)
        event, filePath, time = allEvent[0]
        self.assertEquals(event, "mod")
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/ubuntu.iso")
        
        
    def testDeletingFile(self):
        watchedPath = "watchDir1"
        fs = FileSystem(".")
        
        newFile = fs.absPath("%s/file1" % watchedPath)
        if not fs.isFile(newFile):
            fs.writeFile(newFile, "writing")
        
        linuxWatcher = EventWatcherClass(watchedPath, fs)
        fs.delete(newFile)            
        sleep(1)
        #linuxWatcher.notifier.stop()
        
        allEvent = linuxWatcher.eventList()
        self.assertEquals(len(allEvent), 1)
        event, filePath, time = allEvent[0]
        self.assertEquals(event, "del")
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/file1")
        
    def testRenamingFile(self):
        watchedPath = "watchDir1"
        fs = FileSystem(".")
        
        oldFile = "%s/file2" % watchedPath
        if not fs.isFile(oldFile):
            fs.writeFile(oldFile, "to be renamed")
        newFile = "%s/file2_rename" % watchedPath
        if fs.isFile(newFile):
            fs.delete(newFile)
        
        linuxWatcher = EventWatcherClass(watchedPath, fs)
        fs.rename(oldFile, newFile)
        sleep(1)
        #linuxWatcher.notifier.stop()
        
        allEvent = linuxWatcher.eventList()
        self.assertEquals(len(allEvent), 2)
        event, filePath, time = allEvent[0]
        self.assertEqual(event, 'del')
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/file2")
        event, filePath, time = allEvent[1]
        self.assertEqual(event, 'mod')
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/file2_rename")
        
        
    def testModifyFileInSubdirectory(self):
        watchedPath = "watchDir1"
        fs = FileSystem(".")
        
        newFile = "%s/dir/file1" % watchedPath
        if not fs.isFile(newFile):
            fs.writeFile(newFile, 'file in dir')
        linuxWatcher = EventWatcherClass(watchedPath, fs)
        os.utime(newFile, None)
        sleep(1)
        
        allEvent = linuxWatcher.eventList()
        self.assertEquals(len(allEvent), 1)
        event, filePath, time = allEvent[0]
        self.assertEquals(event, "mod")
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/dir/file1")
        
    def testCreateNewDir(self):
        watchedPath = "watchDir1"
        fs = FileSystem(".")
        
        newDir = "%s/sub_dir" % watchedPath
        if fs.isDirectory(newDir):
            fs.delete(newDir)
        
        linuxWatcher = EventWatcherClass(watchedPath, fs)
        fs.makeDirectory(newDir)
        sleep(1)
        
        allEvent = linuxWatcher.eventList()
        self.assertEquals(len(allEvent), 1)
        event, filePath, time = allEvent[0]
        self.assertEquals(event, "mod")
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/sub_dir")
        
    def testRenameEmptyDir(self):
        watchedPath = "watchDir1"
        fs = FileSystem(".")
        
        newDir = "%s/sub_dir_rename" % watchedPath
        if fs.isDirectory(newDir):
            fs.delete(newDir)
        
        linuxWatcher = EventWatcherClass(watchedPath, fs)
        oldDir = "%s/sub_dir" % watchedPath
        if not fs.isDirectory(oldDir):
            fs.makeDirectory(oldDir)
        sleep(1)
        fs.rename(oldDir, newDir)
        sleep(1)
                    
        allEvent = linuxWatcher.eventList()
        self.assertEquals(len(allEvent), 2)
        event, filePath, time = allEvent[0]
        self.assertEquals(event, "del")
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/sub_dir")
        event, filePath, time = allEvent[1]
        self.assertEquals(event, "mod")
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/sub_dir_rename")
        
    def testDeleteEmptyDirectory(self):
        watchedPath = "watchDir1"
        fs = FileSystem(".")
        
        newDir = "%s/sub_dir_rename" % watchedPath
        if not fs.isDirectory(newDir):
            fs.makeDirectory(newDir)
        
        linuxWatcher = EventWatcherClass(watchedPath, fs)
        fs.delete(newDir)
        sleep(1)
        
        allEvent = linuxWatcher.eventList()
        self.assertEquals(len(allEvent), 1)
        event, filePath, time = allEvent[0]
        self.assertEquals(event, "del")
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/sub_dir_rename")
        
    def testCreateDirAndFile(self):
        watchedPath = "watchDir1"
        fs = FileSystem(".")
        
        newDir = "%s/dir" % watchedPath
        newFile = "%s/file1" % newDir
        if fs.isDirectory(newDir):
            fs.delete(newDir)
                
        linuxWatcher = EventWatcherClass(watchedPath, fs)
        fs.makeDirectory(newDir)
        sleep(1)
        fs.writeFile(newFile, "newFileInDirectory")
        sleep(1)
        
        allEvent = linuxWatcher.eventList()
        self.assertEquals(len(allEvent), 2)
        event, filePath, time = allEvent[0]
        self.assertEquals(event, "mod")
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/dir")
        event, filePath, time = allEvent[1]
        self.assertEquals(event, "mod")
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/dir/file1")
        
    def testDeleteDirectoryWithFile(self):
        watchedPath = "watchDir1"
        fs = FileSystem(".")
        
        newDir = "%s/dir" % watchedPath
        newFile = "%s/file1" % newDir
        if not fs.isDirectory(newDir):
            fs.makeDirectory(newDir)
                
        linuxWatcher = EventWatcherClass(watchedPath, fs)
        fs.writeFile(newFile, "newFileInDirectory")
        sleep(1)
        fs.delete(newDir)
        sleep(1)
        
        allEvent = linuxWatcher.eventList()
        self.assertEquals(len(allEvent), 2)
        event, filePath, time = allEvent[0]
        self.assertEquals(event, "del")
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/dir/file1")
        event, filePath, time = allEvent[1]
        self.assertEquals(event, "del")
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/dir")
        
    def testCreateDirSubdirAndFileinSubdir(self):
        watchedPath = "watchDir1"
        fs = FileSystem(".")
        
        newDir = "%s/dir" % watchedPath
        if fs.isDirectory(newDir):
            fs.delete(newDir)
        
        newSubDir = "%s/sub_dir" % newDir
        if fs.isDirectory(newSubDir):
            fs.delete(newSubDir)
            
        linuxWatcher = EventWatcherClass(watchedPath, fs)
        fs.makeDirectory(newDir)
        fs.makeDirectory(newSubDir)
        sleep(1)
        newFile = "%s/file1" % newSubDir
        fs.writeFile(newFile, "newFileInDirectory")
        sleep(1)

        allEvent = linuxWatcher.eventList()
        self.assertEquals(len(allEvent), 3)
        event, filePath, time = allEvent[0]
        self.assertEquals(event, "mod")
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/dir")
        event, filePath, time = allEvent[1]
        self.assertEquals(event, "mod")
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/dir/sub_dir")
        event, filePath, time = allEvent[2]
        self.assertEquals(event, "mod")
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/dir/sub_dir/file1")
        
    def testDeleteDirectoryWithSubdirAndFileInSubDir(self):
        watchedPath = "watchDir1"
        fs = FileSystem(".")
        
        newDir = "%s/dir" % watchedPath
        if not fs.isDirectory(newDir):
            fs.makeDirectory(newDir)
        newSubDir = "%s/sub_dir" % newDir
        if not fs.isDirectory(newSubDir):
            fs.makeDirectory(newSubDir)
        newFile = "%s/file1" % newSubDir
        if not fs.isFile(newFile):
            fs.writeFile(newFile, "someData")
            
        linuxWatcher = EventWatcherClass(watchedPath, fs)
        fs.delete(newDir)
        sleep(1)
        
        allEvent = linuxWatcher.eventList()    
        self.assertEquals(len(allEvent), 3)
        event, filePath, time = allEvent[0]
        self.assertEquals(event, "del")
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/dir/sub_dir/file1")
        event, filePath, time = allEvent[1]
        self.assertEquals(event, "del")
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/dir/sub_dir")
        event, filePath, time = allEvent[2]
        self.assertEquals(event, "del")
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/dir")
        
    def testRenameDirectoryWithFile(self):
        watchedPath = "watchDir1"
        fs = FileSystem(".")
        
        newDir = "%s/dir" % watchedPath
        if not fs.isDirectory(newDir):
            fs.makeDirectory(newDir)
            
        renameDir = "%s/dir_new" % watchedPath
        if fs.isDirectory(renameDir):
            fs.delete(renameDir)
        
        linuxWatcher = EventWatcherClass(watchedPath, fs)            
        newFile = "%s/file1" % newDir
        fs.writeFile(newFile, "newFileInDirectory")
        sleep(1)
        
        fs.rename(newDir, renameDir)
        sleep(1)
            
        allEvent = linuxWatcher.eventList()
        self.assertEquals(len(allEvent), 3)
        event, filePath, time = allEvent[0]
        self.assertEquals(event, "mod")
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/dir/file1")
        event, filePath, time = allEvent[1]
        self.assertEquals(event, "del") #QUEUE NEED TO REMOVE the events of files under this dir from queue table
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/dir")
        event, filePath, time = allEvent[2]
        self.assertEquals(event, "mod") #QUEUE NEED TO ADD the new mod event for each of the files in the queue table
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/dir_new")
            
    def testRenameDirectoryWithSubDirAndFile(self):
        watchedPath = "watchDir1"
        fs = FileSystem(".")
        
        renameDir = "%s/dir_new" % watchedPath
        if fs.isDirectory(renameDir):
            fs.delete(renameDir)
        
        newDir = "%s/dir" % watchedPath
        if not fs.isDirectory(newDir):
            fs.makeDirectory(newDir)
        
        subNewDir = "%s/sub_dir" % newDir
        if not fs.isDirectory(subNewDir):
            fs.makeDirectory(subNewDir)
            
        newFile = "%s/file1" % subNewDir
        fs.writeFile(newFile, "newFileInDirectory")
                
        linuxWatcher = EventWatcherClass(watchedPath, fs)            
        fs.rename(newDir, renameDir)
        sleep(1)
            
        allEvent = linuxWatcher.eventList()
        self.assertEquals(len(allEvent), 2)
        event, filePath, time = allEvent[0]
        self.assertEquals(event, "del") #QUEUE NEED TO REMOVE the events of files under this dir from queue table
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/dir")
        event, filePath, time = allEvent[1]
        self.assertEquals(event, "mod") #QUEUE NEED TO ADD the new mod event for each of the files in the queue table
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/dir_new")

    def testCopyFileFromUnwatchDirToWatchedDir(self):
        watchedPath = "watchDir1"
        fs = FileSystem(".")
        
        unwatchedDir = "unwatched"
        unwatchedFile = "%s/file10" % unwatchedDir
        if not fs.isFile(unwatchedFile):
            fs.writeFile(unwatchedFile, "this file is not watched")
        
        copyToWatched = "%s/file10" % watchedPath
        if fs.isFile(copyToWatched):
            fs.delete(copyToWatched)
        
        linuxWatcher = EventWatcherClass(watchedPath, fs)
        fs.copy(unwatchedFile, copyToWatched)
        sleep(1)
        
        allEvent = linuxWatcher.eventList()
        self.assertEquals(len(allEvent), 1)
        event, filePath, time = allEvent[0]
        self.assertEquals(event, "mod") 
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/file10")
        
    def testCopyFileFromUnwatchDirToWatchedDirWithSubDir(self):
        watchedPath = "watchDir1"
        fs = FileSystem(".")
        
        unwatchedDir = "unwatched"
        unwatchedSubDir = "%s/sub_dir" % unwatchedDir
        if not fs.isDirectory(unwatchedSubDir):
            fs.makeDirectory(unwatchedSubDir)
        unwatchedFile = "%s/file10" % unwatchedSubDir
        if not fs.isFile(unwatchedFile):
            fs.writeFile(unwatchedFile, "this file is not watched")
        
        copyToWatched = "%s/sub_dir/file10" % watchedPath
        if fs.isDirectory("%s/sub_dir" % watchedPath):
            fs.delete("%s/sub_dir" % watchedPath)
        if fs.isFile(copyToWatched):
            fs.delete(copyToWatched)
        
        linuxWatcher = EventWatcherClass(watchedPath, fs)
        fs.copy(unwatchedFile, copyToWatched)
        sleep(1)
        
        allEvent = linuxWatcher.eventList()
        self.assertEquals(len(allEvent), 2)
        event, filePath, time = allEvent[0]
        self.assertEquals(event, "mod") 
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/sub_dir")
        event, filePath, time = allEvent[1]
        self.assertEquals(event, "mod") 
        self.assertEquals(filePath, "/home/octalina/workspace/watcher/fswatcher/linux/watchDir1/sub_dir/file10")
        

    def testWatchingNotExistedDirectory(self):
        watchedPath = "notExistDir"
        fs = FileSystem(".")
        
        linuxWatcher = EventWatcherClass(watchedPath, fs)

    #not working yet.... 
    def testIgnorePrivateFile(self):
        watchedPath = "watchDir1"
        fs = FileSystem(".")
        
        linuxWatcher = EventWatcherClass(watchedPath, fs, exclFileName='exclude.pattern', exclFile=('excl_lst1'))
        newFile = "%s/.privateFile" % watchedPath
        fs.writeFile(newFile, 'private data')
        sleep(1)
        

if __name__ == "__main__":
    print "\n\n"
    print "---- Testing ----"
    print
    args = list(sys.argv)
    sys.argv = sys.argv[:1]
    args.pop(0)
    runTests = args
    runTests = [ i.lower().strip(", ") for i in runTests]
    runTests = ["test"+i for i in runTests if not i.startswith("test")] + \
                [i for i in runTests if i.startswith("test")]
    if runTests!=[]:
        testClasses = [i for i in locals().values() \
                        if hasattr(i, "__bases__") and TestCase in i.__bases__]
        for x in testClasses:
            l = dir(x)
            l = [ i for i in l if i.startswith("test") and callable(getattr(x, i))]
            testing = []
            for i in l:
                if i.lower() not in runTests:
                    #print "Removing '%s'" % i
                    delattr(x, i)
                else:
                    #print "Testing '%s'" % i
                    testing.append(i)
        x = None
        print "Running %s selected tests - %s" % (len(testing), str(testing)[1:-1])

    unittest.main()