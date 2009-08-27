package au.edu.usq.fascinator.velocity;

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.util.introspection.VelMethod;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JythonVelMethod implements VelMethod {

    private Logger log = LoggerFactory.getLogger(JythonVelMethod.class);

    private String methodName;

    public JythonVelMethod(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class getReturnType() {
        return Object.class;
    }

    @Override
    public Object invoke(Object o, Object[] params) throws Exception {
        log.debug("invoke:" + o);
        if (params != null) {
            for (Object param : params) {
                log.debug("       param:" + param);
            }
        }
        Object retVal = null;
        PyObject pyObject = (PyObject) o;
        PyObject method = pyObject.__findattr__(methodName);
        if (method != null) {
            log.debug("method:" + method);
            if (params == null || params.length < 1) {
                retVal = method.__call__();
            } else {
                List<PyObject> args = new ArrayList<PyObject>();
                for (Object param : params) {
                    log.debug("param:" + param + ":" + param.getClass());
                    if (param instanceof String) {
                        args.add(new PyString(param.toString()));
                    } else if (param instanceof Integer) {
                        args.add(new PyInteger(((Integer) param).intValue()));
                    } else if (param instanceof PyObject) {
                        args.add((PyObject) param);
                    } else {
                        log.error("Unsupported param type:"
                                + param.getClass().getName());
                        return null;
                    }
                }
                retVal = method.__call__(args.toArray(new PyObject[] {}));
            }
        } else {
            log.error("No such method: {}", methodName);
        }
        return JythonUberspect.toJava(retVal);
    }

    @Override
    public boolean isCacheable() {
        return true;
    }
}
