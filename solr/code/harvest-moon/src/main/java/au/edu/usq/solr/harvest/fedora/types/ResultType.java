package au.edu.usq.solr.harvest.fedora.types;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "result", namespace = ResultType.NAMESPACE)
@XmlAccessorType(XmlAccessType.NONE)
public class ResultType {

    public static final String NAMESPACE = "http://www.fedora.info/definitions/1/0/types/";

    @XmlElement(name = "listSession", namespace = NAMESPACE)
    private ListSessionType listSession;

    @XmlElementWrapper(name = "resultList", namespace = NAMESPACE)
    @XmlElement(name = "objectFields", namespace = NAMESPACE)
    private List<ObjectFieldType> objectFields;

    public ListSessionType getListSession() {
        return listSession;
    }

    public List<ObjectFieldType> getObjectFields() {
        return objectFields;
    }
}
