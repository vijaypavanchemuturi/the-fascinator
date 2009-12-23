package au.edu.usq.fascinator.portal.ims;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import au.edu.usq.fascinator.common.JsonConfigHelper;
import au.edu.usq.fascinator.ims.FileType;
import au.edu.usq.fascinator.ims.ItemType;
import au.edu.usq.fascinator.ims.ManifestType;
import au.edu.usq.fascinator.ims.MetadataType;
import au.edu.usq.fascinator.ims.OrganizationType;
import au.edu.usq.fascinator.ims.OrganizationsType;
import au.edu.usq.fascinator.ims.ResourceType;
import au.edu.usq.fascinator.ims.ResourcesType;

public class ImsPackage {

    // private String fileName = "imsmanifest.xml";

    private File manifestFile;

    // private Portal portal;

    private ManifestType generatedImsXml;

    private Map<String, JsonConfigHelper> manifestItems;

    // private Logger log = LoggerFactory.getLogger(ImsPackage.class);

    public ImsPackage() {

    }

    public void setManifestFile(File manifestFile) {
        this.manifestFile = manifestFile;
    }

    public void setManifestItems(Map<String, JsonConfigHelper> manifestItems) {
        this.manifestItems = manifestItems;
    }

    public OrganizationType setOrganization(String identifier, String title) {
        OrganizationType ot = new OrganizationType();
        ot.setIdentifier(identifier);
        ot.setStructure("");
        ot.setTitle(title);
        return ot;
    }

    public MetadataType setMetadataType(String schema, String schemaVersion) {
        MetadataType imsMeta = new MetadataType();
        imsMeta.setSchema(schema);
        imsMeta.setSchemaversion(schemaVersion);
        return imsMeta;
    }

    public ItemType setItemType(String identifier, boolean visible, String title) {
        ItemType itemType = new ItemType();
        itemType.setIdentifier(identifier);
        itemType.setIdentifierref("default-" + identifier);
        itemType.setIsvisible(visible);
        itemType.setTitle(title);
        return itemType;
    }

    public ResourceType setResourceType(String identifier, String type,
            String href, List<String> fileList) {
        ResourceType rs = new ResourceType();
        rs.setIdentifier(identifier);
        rs.setType(type);
        rs.setHref(href);

        for (String file : fileList) {
            FileType ft = new FileType();
            ft.setHref(file);
            rs.getFile().add(ft);
        }
        return rs;
    }

    public void generateManifest() throws JAXBException, IOException {
        ManifestType ms = new ManifestType();

        MetadataType imsMeta = setMetadataType("IMS Content", "1.1.4");
        ms.setMetadata(imsMeta);

        OrganizationsType ots = new OrganizationsType();
        // ots.setDefault("default"); //If this is defined, it's not really

        OrganizationType ot = setOrganization("default", "---- test title --- ");
        ots.getOrganization().add(ot);

        ResourcesType resType = new ResourcesType();
        ms.setResources(resType);
        // Do item here
        for (String itemHash : manifestItems.keySet()) {
            JsonConfigHelper js = manifestItems.get(itemHash);

            boolean visible = false;
            if (js.get("hidden") == null) {
                visible = true;
            }

            ItemType itemType = setItemType(itemHash, visible, js.get("title"));
            ot.getItem().add(itemType);

            // need to check if oliver include other file in the same node in
            // organizer
            List<String> fileList = new ArrayList();

            fileList.add(js.get("id"));
            ResourceType rs = setResourceType(itemHash, "webcontent", js
                    .get("id"), fileList);
            resType.getResource().add(rs);
        }

        ms.setOrganizations(ots);

        JAXBContext context = JAXBContext.newInstance(ManifestType.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        Writer w = null;

        try {
            w = new FileWriter(manifestFile.getAbsolutePath());
            m.marshal(new JAXBElement(new QName(
                    "http://www.imsglobal.org/xsd/imscp_v1p1", "manifest"),
                    ManifestType.class, ms), w);
            System.out.println("done item1... ");
        } finally {
            try {
                w.close();
                System.out.println("done3 item... ");
            } catch (Exception e) {
                System.out.println("error???");
            }
        }
        System.out.println("done item2... ");
    }
    // public ImsPackage(String manifestFilePath, Portal portal,
    // Map<String, JsonConfigHelper> manifestItems) {
    // manifestFile = new File(manifestFilePath, fileName);
    //
    // this.portal = portal;
    // this.manifestItems = manifestItems;
    // createImsPackage();
    // try {
    // saveToFile();
    // } catch (JAXBException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
    //
    // public void createImsPackage() {
    // // generatedImsXml = new ManifestType();
    // // generatedImsXml.setVersion("1.1.4");
    // }
    //
    // public void saveToFile() throws JAXBException, IOException {
    // ManifestType ms = new ManifestType();
    // OrganizationsType ots = new OrganizationsType();
    // OrganizationType ot = new OrganizationType();
    // ots.getOrganization().add(ot);
    // ms.setOrganizations(ots);
    // JAXBContext context = JAXBContext.newInstance(ManifestType.class);
    // Marshaller m = context.createMarshaller();
    // m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    //
    // // m.marshal(generatedImsXml, System.out);
    //
    // Writer w = null;
    // try {
    // w = new FileWriter(manifestFile.getAbsolutePath());
    // // m.marshal(ms, w);
    // m.marshal(new JAXBElement(new QName(
    // "http://www.imsglobal.org/xsd/imscp_v1p1", "manifest"),
    // ManifestType.class, ms), w);
    // } finally {
    // try {
    // w.close();
    //
    // } catch (Exception e) {
    // }
    // }
    //
    // }
}
