package au.edu.usq.fascinator.portal.ims;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import au.edu.usq.fascinator.common.JsonConfigHelper;
import au.edu.usq.fascinator.common.storage.impl.GenericDigitalObject;
import au.edu.usq.fascinator.ims.FileType;
import au.edu.usq.fascinator.ims.ItemType;
import au.edu.usq.fascinator.ims.MetadataType;
import au.edu.usq.fascinator.ims.OrganizationType;
import au.edu.usq.fascinator.ims.ResourceType;
import au.edu.usq.fascinator.ims.ResourcesType;

public class ImsPackageTest {
    private JsonConfigHelper json;

    private ImsPackage imsPackage;

    private GenericDigitalObject object;

    private String tempDir = System.getProperty("java.io.tmpdir");

    @Before
    public void setup() {
        json = new JsonConfigHelper();
        json.set("manifest/node-0ee3cf2b2e93ed766b71ba20a847a272/title",
                "Aliquam pharetra neque ");
        json.set("manifest/node-0ee3cf2b2e93ed766b71ba20a847a272/id",
                "/home/octalina/Documents/picture/text/chapter01.odt");
        json.set("manifest/node-b9d1e5687882b86f2979e2751e472fe9/title",
                "power.odp");
        json.set("manifest/node-b9d1e5687882b86f2979e2751e472fe9/id",
                "/home/octalina/Documents/power.odp");
        json.set("manifest/node-d13cf6317b41e3e414eae2a70389f608/title",
                "spreadsheet.ods");
        json.set("manifest/node-d13cf6317b41e3e414eae2a70389f608/id",
                "/home/octalina/Documents/spreadsheet.ods");
        json.set("manifest/node-91ada6aa529f505c4ca685b0f263b003/title",
                "test.ODT");
        json.set("manifest/node-91ada6aa529f505c4ca685b0f263b003/id",
                "/home/octalina/Documents/text/test.ODT");
        json.set("manifest/node-91ada6aa529f505c4ca685b0f263b003/hidden",
                "true");
        imsPackage = new ImsPackage();
        imsPackage.setManifestFile(new File(tempDir, "imsmanifest.xml"));
        imsPackage.setManifestItems(json.getJsonMap("manifest"));
    }

    @After
    public void cleanup() {
        File tempDirFolder = new File(tempDir);
        if (tempDirFolder.exists()) {
            tempDirFolder.delete();
        }
    }

    @Test
    public void testSetOrganisation() {
        OrganizationType ot = imsPackage.setOrganization("default",
                "test title");
        Assert.assertEquals(ot.getIdentifier(), "default");
        Assert.assertEquals(ot.getTitle(), "test title");
    }

    @Test
    public void testSetMetadataType() {
        MetadataType mt = imsPackage.setMetadataType("IMS Content", "1.1.4");
        Assert.assertEquals(mt.getSchema(), "IMS Content");
        Assert.assertEquals(mt.getSchemaversion(), "1.1.4");
    }

    @Test
    public void testSetItemType() {
        ItemType it = imsPackage.setItemType("123", false, "title 123");
        Assert.assertEquals(it.getIdentifier(), "123");
        Assert.assertEquals(it.getIdentifierref(), "default-123");
        Assert.assertEquals(String.valueOf(it.isIsvisible()), String
                .valueOf(false));
        Assert.assertEquals(it.getTitle(), "title 123");

        ItemType it2 = imsPackage.setItemType("123", true, "title 123");
        Assert.assertEquals(it2.getIdentifier(), "123");
        Assert.assertEquals(it2.getIdentifierref(), "default-123");
        Assert.assertEquals(String.valueOf(it2.isIsvisible()), String
                .valueOf(true));
        Assert.assertEquals(it2.getTitle(), "title 123");
    }

    @Test
    public void testSetResourceType() {
        // One file
        List<String> fileList = new ArrayList();
        fileList.add("/skin/test.htm");
        ResourceType rt = imsPackage.setResourceType("123", "webcontent",
                "/skin/test.htm", fileList);
        Assert.assertEquals(rt.getIdentifier(), "123");
        Assert.assertEquals(rt.getType(), "webcontent");
        Assert.assertEquals(rt.getHref(), "/skin/test.htm");
        Assert.assertEquals(1, rt.getFile().size());
        FileType ft = rt.getFile().get(0);
        Assert.assertEquals(ft.getHref(), "/skin/test.htm");

        // More than one files.....
        fileList = new ArrayList();
        fileList.add("/skin/test.htm");
        fileList.add("/skin/test2.htm");
        rt = imsPackage.setResourceType("123", "webcontent", "/skin/test.htm",
                fileList);
        Assert.assertEquals(rt.getIdentifier(), "123");
        Assert.assertEquals(rt.getType(), "webcontent");
        Assert.assertEquals(rt.getHref(), "/skin/test.htm");
        Assert.assertEquals(2, rt.getFile().size());
        ft = rt.getFile().get(0);
        Assert.assertEquals(ft.getHref(), "/skin/test.htm");
        ft = rt.getFile().get(1);
        Assert.assertEquals(ft.getHref(), "/skin/test2.htm");
    }

    @Test
    public void testSetResourcesForAllPayloads() {
        // Create payload list... and the images files should be linked to the
        // html
        List<String> fileList = Arrays.asList("/home/test.odt",
                "/home/test.pdf", "/home/test.htm",
                "/home/test_files/pic1.jpg", "/home/test_files/pic2.jpg",
                "/home/test_files/pic3.jpg");

        ResourcesType rst = new ResourcesType();
        ResourceType rs = imsPackage.setResourceType("123", "webcontent",
                "/home/test.htm", fileList);
        rst.getResource().add(rs);
        Assert.assertEquals(rst.getResource().get(0).getFile().size(), 6);

        // CASE:
        // * images are not in _files folder, need to create a resource for
        // itself + the html file (this html can be used for... epub)

    }

    @Ignore
    @Test
    public void testManifestSimple() throws JAXBException, IOException {
        System.out.println("0000");
        imsPackage.generateManifest();
        System.out.println("0000 done...");
        File file = new File(tempDir, "imsmanifest.xml");
        System.out.println("" + file.exists());
        if (file.exists()) {
            StringBuffer contents = new StringBuffer();
            BufferedReader reader = null;
            reader = new BufferedReader(new FileReader(file));
            String text = null;
            while ((text = reader.readLine()) != null) {
                contents.append(text).append(
                        System.getProperty("line.separator"));
            }
            System.out.println(contents.toString());
        }
        // System.out
        // .println("" + json.getJsonMap("manifest") + " ... " + tempDir);
    }
}
