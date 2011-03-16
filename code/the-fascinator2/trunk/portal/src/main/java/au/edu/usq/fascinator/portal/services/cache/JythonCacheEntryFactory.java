/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.usq.fascinator.portal.services.cache;

import au.edu.usq.fascinator.api.indexer.Indexer;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.portal.services.ByteRangeRequestCache;
import au.edu.usq.fascinator.portal.services.DatabaseServices;
import au.edu.usq.fascinator.portal.services.DynamicPageService;
import au.edu.usq.fascinator.portal.services.HarvestManager;
import au.edu.usq.fascinator.portal.services.HouseKeepingManager;
import au.edu.usq.fascinator.portal.services.PortalManager;
import au.edu.usq.fascinator.portal.services.ScriptingServices;
import au.edu.usq.fascinator.portal.services.VelocityService;
import java.io.InputStream;
import java.util.List;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lucido
 */
public class JythonCacheEntryFactory implements CacheEntryFactory {

    private Logger log = LoggerFactory.getLogger(JythonCacheEntryFactory.class);

    private VelocityService velocityService;

    private ScriptingServices scriptingServices;

    private String portalPath;

    private String defaultPortal;

    private List<String> skinPriority;

    private int refreshIntervalSeconds;

    public JythonCacheEntryFactory(PortalManager portalManager,
            VelocityService velocityService,
            ScriptingServices scriptingServices,
            int refreshIntervalSeconds) {
        this.velocityService = velocityService;
        this.scriptingServices = new DeprecatedServicesWrapper(scriptingServices);
        this.refreshIntervalSeconds = refreshIntervalSeconds;
        defaultPortal = portalManager.getDefaultPortal();
        portalPath = portalManager.getHomeDir().toString();
        skinPriority = portalManager.getSkinPriority();
    }

    @Override
    public Object createEntry(Object key) throws Exception {
        //log.debug("createEntry({})", key);
        String path = key.toString();
        int qmarkPos = path.lastIndexOf("?");
        if (qmarkPos != -1) {
            path = path.substring(0, qmarkPos);
        }
        int slashPos = path.indexOf("/");
        String portalId = path.substring(0, slashPos);
        PyObject scriptObject = null;
        InputStream in = velocityService.getResource(path);
        if (in == null) {
            log.debug("Failed to load script: '{}'", path);
        } else {
            // add current and default portal directories to python sys.path
            addSysPaths(portalId, Py.getSystemState());
            // setup the python interpreter
            PythonInterpreter python = PythonInterpreter.threadLocalStateInterpreter(null);
            // expose services for backward compatibility - deprecated
            python.set("Services", scriptingServices);
            //python.setLocals(scriptObject);
            //JythonLogger jythonLogger = new JythonLogger(path);
            //python.setOut(jythonLogger);
            //python.setErr(jythonLogger);
            python.execfile(in, path);
            String scriptClassName = StringUtils.capitalize(
                    FilenameUtils.getBaseName(path)) + "Data";
            PyObject scriptClass = python.get(scriptClassName);
            if (scriptClass != null) {
                scriptObject = scriptClass.__call__();
            } else {
                log.debug("Failed to find class '{}'", scriptClassName);
            }
            python.cleanup();
        }
        return scriptObject;
    }

    private void addSysPaths(String portalId, PySystemState sys) {
        for (String skin : skinPriority) {
            String sysPath = portalPath + "/" + portalId + "/" + skin
                    + "/scripts";
            sys.path.append(Py.newString(sysPath));
        }
        if (!defaultPortal.equals(portalId)) {
            addSysPaths(defaultPortal, sys);
        }
    }

    private class DeprecatedServicesWrapper implements ScriptingServices {

        private ScriptingServices scriptingServices;

        public DeprecatedServicesWrapper(ScriptingServices scriptingServices) {
            this.scriptingServices = scriptingServices;
        }

        @Override
        public DatabaseServices getDatabase() {
            log.warn("Global scope Services is deprecated, use the context");
            return scriptingServices.getDatabase();
        }

        @Override
        public DynamicPageService getPageService() {
            log.warn("Global scope Services is deprecated, use the context");
            return scriptingServices.getPageService();
        }

        @Override
        public Indexer getIndexer() {
            log.warn("Global scope Services is deprecated, use the context");
            return scriptingServices.getIndexer();
        }

        @Override
        public Storage getStorage() {
            log.warn("Global scope Services is deprecated, use the context");
            return scriptingServices.getStorage();
        }

        @Override
        public HarvestManager getHarvestManager() {
            log.warn("Global scope Services is deprecated, use the context");
            return scriptingServices.getHarvestManager();
        }

        @Override
        public HouseKeepingManager getHouseKeepingManager() {
            log.warn("Global scope Services is deprecated, use the context");
            return scriptingServices.getHouseKeepingManager();
        }

        @Override
        public PortalManager getPortalManager() {
            log.warn("Global scope Services is deprecated, use the context");
            return scriptingServices.getPortalManager();
        }

        @Override
        public ByteRangeRequestCache getByteRangeCache() {
            log.warn("Global scope Services is deprecated, use the context");
            return scriptingServices.getByteRangeCache();
        }

        @Override
        public VelocityService getVelocityService() {
            log.warn("Global scope Services is deprecated, use the context");
            return scriptingServices.getVelocityService();
        }
    }
}
