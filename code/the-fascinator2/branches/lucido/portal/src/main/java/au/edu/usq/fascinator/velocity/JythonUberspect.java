package au.edu.usq.fascinator.velocity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.UberspectImpl;
import org.apache.velocity.util.introspection.VelMethod;
import org.apache.velocity.util.introspection.VelPropertyGet;
import org.apache.velocity.util.introspection.VelPropertySet;
import org.python.core.PyBoolean;
import org.python.core.PyDictionary;
import org.python.core.PyInteger;
import org.python.core.PyJavaType;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PySequence;
import org.python.core.PyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JythonUberspect extends UberspectImpl {

    private static Logger log = LoggerFactory.getLogger(JythonUberspect.class);

    @Override
    @SuppressWarnings("unchecked")
    public Iterator getIterator(Object obj, Info i) throws Exception {
        log.debug("getIterator obj:" + obj + " i:" + i);

        if (obj instanceof PyObject) {
            PyObject pyObject = (PyObject) obj;
            PyType pyType = pyObject.getType();
            if (pyObject instanceof PySequence) {
                return new JythonIterator((PySequence) pyObject);
            } else if (pyObject instanceof PyDictionary) {
                return ((PyDictionary) pyObject).values().iterator();
            } else if (pyType instanceof PyJavaType) {
                Class cls = ((PyJavaType) pyType).getProxyType();
                log.debug("ProxyType:" + pyType);
                List<Class> interfaces = Arrays.asList(cls.getInterfaces());
                Object javaObject = pyObject.__tojava__(cls);
                if (interfaces.contains(Map.class)) {
                    return ((Map) javaObject).values().iterator();
                } else if (interfaces.contains(List.class)) {
                    return ((List) javaObject).iterator();
                } else {
                    log.error("Unsupported Java type: " + pyType);
                }
            } else {
                log.error("Unsupported class:{} type:{}", pyObject.getClass(),
                        pyType);
            }
            return null;
        }

        return super.getIterator(obj, i);
    }

    @Override
    public VelMethod getMethod(Object obj, String methodName, Object[] args,
            Info i) throws Exception {
        log.debug("getMethod obj:" + obj + " methodName:" + methodName + " i:"
                + i);
        if (args != null && args.length > 0) {
            for (Object arg : args) {
                log.debug("          arg:" + arg);
            }
        }

        if (obj instanceof PyObject) {
            return new JythonVelMethod(methodName);
        }

        return super.getMethod(obj, methodName, args, i);
    }

    @Override
    public VelPropertyGet getPropertyGet(Object obj, String identifier, Info i)
            throws Exception {
        log.debug("getPropertyGet obj:" + obj + " identifier:" + identifier
                + " i:" + i);

        if (obj instanceof PyObject) {
            return new JythonVelPropertyGet(identifier);
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

    public static Object toJava(Object obj) {
        if (obj instanceof PyObject) {
            PyObject pyObject = (PyObject) obj;
            if (pyObject instanceof PyNone) {
                return null;
            } else if (pyObject instanceof PyBoolean) {
                return Boolean.parseBoolean(pyObject.toString());
            } else if (pyObject instanceof PyInteger) {
                return Integer.parseInt(pyObject.toString());
            } else if (pyObject instanceof PyDictionary) {
                return new HashMap((Map) pyObject);
            } else {
                log.debug("toJava unhandled type:{}", pyObject.getClass()
                        .getName());
            }
        }
        return obj;
    }
}
