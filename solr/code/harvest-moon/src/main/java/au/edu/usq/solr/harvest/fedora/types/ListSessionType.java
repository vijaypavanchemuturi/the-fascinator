package au.edu.usq.solr.harvest.fedora.types;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class ListSessionType {

    @XmlElement(namespace = ResultType.NAMESPACE)
    private String token;

    @XmlElement(namespace = ResultType.NAMESPACE)
    private int cursor;

    @XmlElement(name = "expirationDate", namespace = ResultType.NAMESPACE)
    private Date expirationDate;

    public String getToken() {
        return token;
    }

    public int getCursor() {
        return cursor;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    @Override
    public String toString() {
        return token + ":" + cursor;
    }
}
