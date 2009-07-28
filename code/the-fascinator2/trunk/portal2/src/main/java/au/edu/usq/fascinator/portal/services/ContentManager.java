package au.edu.usq.fascinator.portal.services;

import java.io.IOException;
import java.util.Map;

import au.edu.usq.fascinator.portal.Content;

public interface ContentManager {

    public Map<String, Content> getContents();

    public Content get(String name);

    public void add(Content portal);

    public void remove(String name);

    public void save(Content portal) throws IOException;

}
