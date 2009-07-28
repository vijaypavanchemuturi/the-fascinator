package au.edu.usq.fascinator.portal.services.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.portal.Content;
import au.edu.usq.fascinator.portal.services.ContentManager;

public class ContentManagerImpl implements ContentManager {

    private Logger log = LoggerFactory.getLogger(ContentManagerImpl.class);

    private File contentDir;

    private Map<String, Content> contentMap;

    public ContentManagerImpl() {
        try {
            JsonConfig config = new JsonConfig();
            contentDir = new File(config.get("portal/contentDir"));
            load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Content> getContents() {
        if (contentMap == null) {
            contentMap = new HashMap<String, Content>();
        }
        return contentMap;
    }

    @Override
    public void add(Content content) {
        getContents().put(content.getId(), content);
    }

    @Override
    public Content get(String id) {
        return getContents().get(id);
    }

    @Override
    public void remove(String id) {
        Content content = getContents().remove(id);
        getContents().remove(id);

        // TODO remove .json and .py files
    }

    @Override
    public void save(Content content) throws IOException {
        FileWriter writer = new FileWriter(
                new File(contentDir, content.getId()));
        content.store(writer);
        writer.close();
    }

    private void load() {
        File[] contentFiles = contentDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                String name = file.getName();
                return file.isDirectory() || name.endsWith(".json");
            }
        });
        for (File contentFile : contentFiles) {
            log.debug("Found content file: {}", contentFile);
            try {
                add(new Content(contentFile));
            } catch (IOException ioe) {
                // TODO Auto-generated catch block
                ioe.printStackTrace();
            }
        }
    }

}
