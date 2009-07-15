package au.edu.usq.fascinator.harvester;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.harvester.Harvester;
import au.edu.usq.fascinator.api.harvester.HarvesterException;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.common.JsonConfig;

public class FileSystemHarvester implements Harvester {

    private File rootDir;

    private File currentDir;

    private boolean hasMore;

    private Stack<File> subDirs;

    @Override
    public String getId() {
        return "file-system";
    }

    @Override
    public String getName() {
        return "File System Harvester";
    }

    @Override
    public void init(File jsonFile) throws PluginException {
        try {
            JsonConfig config = new JsonConfig(jsonFile);
            rootDir = new File(config.get("harvester/file-system/rootDir", "."));
            currentDir = rootDir;
            hasMore = true;
            subDirs = new Stack<File>();
        } catch (IOException ioe) {
            throw new PluginException(ioe);
        }
    }

    @Override
    public void shutdown() throws PluginException {
    }

    @Override
    public List<DigitalObject> getObjects() throws HarvesterException {
        File[] files = currentDir.listFiles(new FileFilter() {
            public boolean accept(File path) {
                // TODO configurable ignore list
                if (".svn".equals(path.getName())) {
                    return false;
                }
                return true;
            }
        });
        List<DigitalObject> fileObjects = new ArrayList<DigitalObject>();
        for (File file : files) {
            if (file.isDirectory()) {
                subDirs.push(file);
            } else {
                fileObjects.add(new FileSystemDigitalObject(file));
            }
        }
        hasMore = !subDirs.isEmpty();
        if (hasMore) {
            currentDir = subDirs.pop();
        }
        return fileObjects;
    }

    @Override
    public boolean hasMoreObjects() {
        return hasMore;
    }
}
