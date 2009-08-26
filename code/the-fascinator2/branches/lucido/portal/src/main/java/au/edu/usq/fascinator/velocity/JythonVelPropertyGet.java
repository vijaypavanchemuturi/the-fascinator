package au.edu.usq.fascinator.velocity;

import org.apache.velocity.util.introspection.VelPropertyGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JythonVelPropertyGet implements VelPropertyGet {

    private Logger log = LoggerFactory.getLogger(JythonVelPropertyGet.class);

    private String methodName;

    public JythonVelPropertyGet(String methodName) {
        log.debug("methodName:" + methodName);
        this.methodName = methodName;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public Object invoke(Object o) throws Exception {
        log.debug("invoke:" + o);
        return null;
    }

    @Override
    public boolean isCacheable() {
        return true;
    }
}
