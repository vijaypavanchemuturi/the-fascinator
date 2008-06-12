package au.edu.usq.solr.harvest.fedora.types;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "objectDatastreams")
@XmlAccessorType(XmlAccessType.NONE)
public class ObjectDatastreamsType {

    @XmlAttribute
    private String pid;

    @XmlAttribute(name = "baseURL")
    private String baseUrl;

    public String getPid() {
        return pid;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    @XmlElement(name = "datastream")
    private List<DatastreamType> datastreams;

    public List<DatastreamType> getDatastreams() {
        return datastreams;
    }
}
