package au.edu.usq.fascinator.velocity;

import java.util.Iterator;

import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.UberspectImpl;
import org.apache.velocity.util.introspection.VelMethod;
import org.apache.velocity.util.introspection.VelPropertyGet;
import org.apache.velocity.util.introspection.VelPropertySet;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JythonUberspect extends UberspectImpl {

    private Logger log = LoggerFactory.getLogger(JythonUberspect.class);

    @Override
    @SuppressWarnings("unchecked")
    public Iterator getIterator(Object obj, Info i) throws Exception {
        log.debug("getIterator obj:" + obj + " i:" + i);
        return super.getIterator(obj, i);
    }

    @Override
    public VelMethod getMethod(Object obj, String methodName, Object[] args,
            Info i) throws Exception {
        log.debug("getMethod obj:" + obj + " methodName:" + methodName
                + " args:" + args + " i:" + i);
        return super.getMethod(obj, methodName, args, i);
    }

    @Override
    public VelPropertyGet getPropertyGet(Object obj, String identifier, Info i)
            throws Exception {
        log.debug("getPropertyGet obj:" + obj + " identifier:" + identifier
                + " i:" + i);

        if (obj instanceof PyObject) {
        }

        return super.getPropertyGet(obj, identifier, i);
    }

    @Override
    public VelPropertySet getPropertySet(Object obj, String identifier,
            Object arg, Info i) throws Exception {
        log.debug("getPropertySet obj:" + obj + " identifier:" + identifier
                + " arg:" + arg + " i:" + i);
        return super.getPropertySet(obj, identifier, arg, i);
    }
}
