import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 *
 * @author Ploy
 */
public class Installer {

    private static String OS = "";
    private static String USERNAME = "";
    private static String CURRENT_DIR = "";
    private static String DEFAULT_DIR = "";
    private static String DEFAULT_FEDORA_PASSWORD = "";
    private static String DEFAULT_SOLR_PASSWORD = "";
    
    private static String install_dir = "";
    private static String fedora_password = "";
    private static String solr_password = "";

    private static String fascinator_home = "";
    private static String fedora_home = "";
    private static String catalina_home = "";
    private static String solr_home = "";
    private static String portal_config_home = "";
    private static String webapp_dir = "";

    private static String java_home;

    private static Runtime runtime;
    private static BufferedReader br;
    private static BufferedWriter bw;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            init();
            getInput();
            installFedora();
            installSolr();
            installFascinator();
            start();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error, an installation has been aborted");
        }
        
        
    }

    private static void init() throws Exception {
        System.out.println("The Fascinator Installation");

        Properties systemProperty = System.getProperties();
        OS = systemProperty.getProperty("os.name");
        USERNAME = systemProperty.getProperty("user.name");
        CURRENT_DIR = systemProperty.getProperty("user.dir");

        System.out.println("Installing The Fascinator as user '" + USERNAME + "@" + OS + "'");
        System.out.println("Installing from directory '" + CURRENT_DIR + "'");

        Properties theFascinatorProperty = new Properties();
        theFascinatorProperty.load(new FileInputStream("TheFascinatorInstaller.properties"));
        DEFAULT_FEDORA_PASSWORD = theFascinatorProperty.getProperty("fedora.password");
        DEFAULT_SOLR_PASSWORD = theFascinatorProperty.getProperty("solr.password");


        String prefix = "";
        if(OS.indexOf("Windows") != -1) {
            prefix = "windows.";
        }
        
        DEFAULT_DIR = theFascinatorProperty.getProperty(prefix + "default.dir");

        runtime = Runtime.getRuntime();
    }

    private static void getInput() throws Exception {
        br = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Enter the base directory to install to [default is " + DEFAULT_DIR + "]: ");
        String dir = br.readLine();

        install_dir = "".equals(dir) ? DEFAULT_DIR : dir;

        fascinator_home = install_dir + "\\the-fascinator";
        fedora_home = install_dir + "\\fedora";
        catalina_home = fedora_home + "\\tomcat";
        solr_home = install_dir + "\\solr";
        portal_config_home = install_dir + "\\portal-config";

        webapp_dir = catalina_home + "\\webapps";

        System.out.println("Installation directories");
        System.out.println("  The Fascinator: " +  fascinator_home);
        System.out.println("  Fedora Commons: " +  fedora_home);
        System.out.println("  Apache Tomcat: " +  catalina_home);
        System.out.println("  Apache Solr: " +  solr_home);
        System.out.println("  Portal config: " + portal_config_home);

        System.out.print("Enter the Fedora administrator (fedoraAdmin) password [default is " +DEFAULT_FEDORA_PASSWORD+ "]: ");
        String fedoraPassword = br.readLine();

        fedora_password =  "".equals(fedoraPassword) ? DEFAULT_FEDORA_PASSWORD : fedoraPassword;

        System.out.print("Enter the Solr administrator (solrAdmin) password [default is " +DEFAULT_SOLR_PASSWORD+ "]: ");
        String solrPassword = br.readLine();

        solr_password =  "".equals(solrPassword) ? DEFAULT_SOLR_PASSWORD : solrPassword;

        do {
            System.out.print("Enter path to JAVA/Home [example is C:\\Program Files\\java]:");
            java_home = br.readLine();
        } while("".equals(java_home));
    }

    private static String getFromFile(String file) throws Exception{
        br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        String lines = "", line = "";

        while((line = br.readLine()) != null) {
            lines += (line) + "\n";
        }

        br.close();

        return lines;
    }

    private static void writeToFile(String file, String content) throws Exception {
        bw = new BufferedWriter(new FileWriter(file));
        bw.write(content);
        bw.close();
    }

    private static void run(String command) throws Exception {
        //TODO: parse command for m$ and non m$
        String pref = ""; //"cmd /c ";
        Process process = runtime.exec(pref + command);

        System.out.println("running: " + command);

        br = new BufferedReader(new InputStreamReader(process.getInputStream()));

        //br = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        String line;

        while ((line = br.readLine()) != null) {
             //System.out.println(line);
        }

        process.waitFor();
    }


    private static void installFedora() throws Exception {
        Properties fedoraProperty = new Properties();
        fedoraProperty.load(new FileInputStream("fedoratmp.properties"));
        fedoraProperty.put("fedora.admin.pass", fedora_password);

        fedoraProperty.put("database.jdbcURL", "jdbc:mckoi:local://" + fedora_home + "/mckoi1.0.3/db.conf?create_or_boot=true");

        fedoraProperty.put("tomcat.home", catalina_home);
        fedoraProperty.put("fedora.home", fedora_home);
        fedoraProperty.store(new FileOutputStream("fedora.properties"), null);

        System.out.println("Installing Fedora Commons to '" + fedora_home + "'...");

        run("cmd /c java -jar files\\fedorainstaller.jar fedora.properties");
        
        System.out.println("Configuring Fedora Commons namespaces...");

        // backup
        String fedoraConfigFile = fedora_home + "\\server\\config\\fedora.fcfg";
        run("cmd /c copy " + fedoraConfigFile + " " +  fedoraConfigFile + ".backup");

        String config = getFromFile(fedoraConfigFile);

        config = config.replaceAll("name=\"pidNamespace\" value=\".+\">", "name=\"pidNamespace\" value=\"uuid\">");
        config = config.replaceAll("name=\"retainPIDs\" value=\".+\">", "name=\"retainPIDs\" value=\"uuid sof\">");

        writeToFile(fedoraConfigFile, config);
    }

    private static void installSolr() throws Exception {
        System.out.println("Creating Solr home at '" + solr_home + "'...");
        
        run("cmd /c mkdir " + solr_home);

        run("cmd /c xcopy files\\solr\\* " + solr_home + " /s /i");

        System.out.println("Deploying Solr web application...");
        run("cmd /c copy files\\solr.war " + webapp_dir);
        

        System.out.println("Configuring Solr security...");
        String tomcatUserFile = "tomcat-users.xml";

        String content = getFromFile("files\\" + tomcatUserFile);
        content = content.replaceAll("changeme", solr_password);
        writeToFile(catalina_home + "\\conf\\" + tomcatUserFile, content);
    }

    private static void installFascinator() throws Exception {
        String content;

        System.out.println("Creating the fascinator home at '" + fascinator_home + "'...");

        run("cmd /c mkdir " + fascinator_home);

        run("cmd /c xcopy files\\the-fascinator\\* " + fascinator_home + " /s /i");

        System.out.println("Installing Solr schema...");

        run("cmd /c copy " + fascinator_home + "\\index\\solr\\schema.xml " + solr_home + "\\conf");

        System.out.println("Deploying indexer web service...");
        content = getFromFile(fascinator_home + "\\index\\WEB-INF\\classes\\indexer.properties.sample");

        content = content.replace("registry.pass=fedoraAdmin", "registry.pass=" + fedora_password);
        content = content.replace("solr.pass=solrAdmin", "solr.pass=" + solr_password);

        writeToFile(fascinator_home + "\\index\\WEB-INF\\classes\\indexer.properties", content);

        run("cmd /c del " + fascinator_home + "\\index\\WEB-INF\\classes\\indexer.properties.sample");

        System.out.println("Deploying portal web application...");

        content = getFromFile(fascinator_home + "\\portal\\WEB-INF\\config.properties.sample");
        content = content.replace("solr.pass=solrAdmin", "solr.pass=" + solr_password);
        content = content.replace("portals.dir=/opt/portal-config", "portals.dir=" + portal_config_home.replace("\\", "\\\\"));
        writeToFile(fascinator_home + "\\portal\\WEB-INF\\config.properties", content);

        content = getFromFile(fascinator_home + "\\portal\\WEB-INF\\velocity.properties.sample");
        content = content.replace("file.resource.loader.path = /opt/portal-config", "file.resource.loader.path=" + portal_config_home.replace("\\", "\\\\"));
        writeToFile(fascinator_home + "\\portal\\WEB-INF\\velocity.properties", content);

        run("cmd /c del " + fascinator_home + "\\portal\\WEB-INF\\config.properties.sample");
        run("cmd /c del " + fascinator_home + "\\portal\\WEB-INF\\velocity.properties.sample");

        run("cmd /c copy " + fascinator_home + "\\portal\\WEB-INF\\classes\\identify.xml.sample "+ fascinator_home + "\\portal\\WEB-INF\\classes\\identify.xml");

        content = getFromFile(fascinator_home + "\\portal\\WEB-INF\\classes\\proai.properties.sample");
        content = content.replace("proai.db.url = jdbc:mckoi:local:///opt/portal-config/proai/db.conf?create_or_boot=true", "proai.db.url = jdbc:mckoi:local:\\" + portal_config_home+ "\\proai\\db.conf?create_or_boot=true");
        content = content.replace("fascinator.portals.dir=/opt/portal-config", "fascinator.portals.dir=" + portal_config_home.replace("\\", "\\\\"));
        content = content.replace("registry.pass=fedoraAdmin", "registry.pass=" + fedora_password);
        content = content.replace("solr.pass=solrAdmin", "solr.pass=" + solr_password);
        writeToFile(fascinator_home + "\\portal\\WEB-INF\\classes\\proai.properties", content);

        System.out.println("Creating portal configuration directory '" + portal_config_home + "'...");

        run("cmd /c mkdir " + portal_config_home);

        run("cmd /c xcopy " + fascinator_home + "\\portal\\config\\* " + portal_config_home + " /s /i");

        String command = "\"" + java_home + "\\bin\\jar\" fuv " + fascinator_home +"\\index\\indexer.war -C " + fascinator_home + "\\index WEB-INF" + "\n";
        command += "\"" + java_home + "\\bin\\jar\" fuv " + fascinator_home+ "\\portal\\the-fascinator.war -C " + fascinator_home + "\\portal WEB-INF" + "\n";
        command += "copy " + fascinator_home + "\\index\\indexer.war " + webapp_dir + "\n";
        command += "copy " + fascinator_home + "\\portal\\the-fascinator.war " + webapp_dir + "\n";
        
        command += "exit";
        writeToFile(fascinator_home + "\\updateWar.bat", command);
        
        run("cmd /c start c://the-fascinator/updateWar.bat");
    }


    private static void start() throws Exception {
        String content = getFromFile(fascinator_home + "\\harvest\\config\\harvest.properties.sample");
        content += "registry.pass=" + fedora_password;
        writeToFile(fascinator_home + "\\harvest\\config\\harvest.properties", content);

        String command = "";
        command += "SET JAVA_HOME=" + java_home + "\n";
        command += "SET FEDORA_HOME="  + fedora_home + "\n";
        command += "SET CATALINA_HOME=" + catalina_home + "\n";
        command += "SET SOLR_HOME=" + solr_home + "\n";
        command += "SET CATALINA_OPTS=\"-Dsolr.solr.home=" + solr_home + "\"" + "\n";
        command += "SET FASCINATOR_HOME=" + fascinator_home + "\n";
        command += "SET PORTAL_CONFIG_HOME=" + portal_config_home + "\n";

        String startCommand = "%CATALINA_HOME%\\bin\\startup.bat" + "\n";

        String stopCommand = "%CATALINA_HOME%\\bin\\shutdown.bat" + "\n";

        writeToFile(fascinator_home + "\\bin\\startup.bat", command + startCommand + "exit");
        writeToFile(fascinator_home + "\\bin\\shutdown.bat", command + stopCommand + "exit");

        String url = "http://localhost:8080/the-fascinator";

        System.out.println("Congratulations you have successfully installed The Fascinator!");

        System.out.println("Use the scripts in the " + fascinator_home + "\\bin to control the Tomcat server.");

        System.out.println("Installation complete.");
    }
}

