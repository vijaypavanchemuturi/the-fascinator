package au.edu.usq.solr.harvest.fedora.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class DatastreamType {

    @XmlAttribute
    private String dsid;

    @XmlAttribute
    private String label;

    @XmlAttribute
    private String mimeType;

    public String getDsid() {
        return dsid;
    }

    public String getLabel() {
        return label;
    }

    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String toString() {
        return label;
    }
}
