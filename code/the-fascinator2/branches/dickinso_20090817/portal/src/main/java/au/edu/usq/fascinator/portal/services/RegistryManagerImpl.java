package au.edu.usq.fascinator.portal.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.tapestry5.ioc.Resource;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.api.PluginException;
import au.edu.usq.fascinator.api.PluginManager;
import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.Payload;
import au.edu.usq.fascinator.api.storage.Storage;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.common.JsonConfig;
import au.edu.usq.fascinator.model.DCRdf;

public class RegistryManagerImpl implements RegistryManager {
    private static Logger log = LoggerFactory
            .getLogger(RegistryManagerImpl.class);

    private JsonConfig config;

    private Storage storagePlugin;

    private SAXReader saxReader;

    private DCRdf dcRdf;

    public RegistryManagerImpl(Resource configuration) {
        // Properties props = new Properties();
        // try {
        // props.load(configuration.toURL().openStream());
        // String registryUrl = props.getProperty(AppModule.REGISTRY_URL_KEY);
        // String user = props.getProperty(AppModule.REGISTRY_USER_KEY);
        // String pass = props.getProperty(AppModule.REGISTRY_PASS_KEY);
        // client = new FedoraRestClient(registryUrl);
        // if (user != null && pass != null) {
        // client.authenticate(user, pass);
        // }
        // } catch (Exception e) {
        // // log.error(e);
        // throw new RuntimeException(e);
        // }
    }

    @Override
    public Document getXmlDatastream(String id) { // uuid, String dsId) {
        // saxReader = new SAXReader();
        // DigitalObject object = storagePlugin.getObject(id);
        // Payload rdfPayload = object.getPayload("rdf"); // getMetadata()
        //
        // try {
        // if (rdfPayload != null) {
        // if (rdfPayload.getInputStream() != null)
        // return saxReader.read(new InputStreamReader(
        // rdfPayload.getInputStream(), "UTF-8"));
        // }
        // } catch (UnsupportedEncodingException uee) {
        // } catch (DocumentException de) {
        // log.error("Failed to parse XML", de);
        // } catch (IOException io) {
        // }
        return null;
    }

    @Override
    public Object getMetadata(String id, String metaType) {
        InputStream is = getXmlData(id, metaType);
        if (is != null) {
            return new DCRdf(is);
        }
        return null;
    }

    public InputStream getXmlData(String id, String payloadId) {
        DigitalObject object = storagePlugin.getObject(id);
        Payload metaPayload = object.getPayload(payloadId); // getMetadata()
        try {
            if (metaPayload != null) {
                if (metaPayload.getInputStream() != null) {
                    return metaPayload.getInputStream();
                }
            }
        } catch (UnsupportedEncodingException uee) {
        } catch (IOException io) {
        }
        return null;
    }

    @Override
    public List<Payload> getPayloadList(String id) {
        DigitalObject object = storagePlugin.getObject(id);

        // / FILTER out SOF-META and *.properties, or maybe no need to do until
        // oliver put hidden variable in the payload
        return object.getPayloadList();
    }

    @Override
    public Payload getPayload(String id, String payloadName) {
        DigitalObject object = storagePlugin.getObject(id);
        Payload payload = object.getPayload(payloadName);
        return payload;
    }

    // public InputStream getPayload(String id, String payloadName) {
    // DigitalObject object = storagePlugin.getObject(id);
    // List<Payload> payloadList = object.getPayloadList();
    // for (Payload payload : payloadList) {
    // if (payloadName.indexOf(payload.getId()) > -1)
    // try {
    // return payload.getInputStream();
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
    // return null;
    // }

    public void getClient(File jsonFile) {
        try {
            config = new JsonConfig(jsonFile);
            String storageType = config.get("storage/type", null);
            try {
                storagePlugin = PluginManager.getStorage(storageType);
                storagePlugin.init(jsonFile);
                // return storagePlugin;
            } catch (StorageException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (PluginException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (IOException ioe) {
            log.warn("Failed to load config from {}", jsonFile);
        }
    }
}
