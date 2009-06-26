import java.io.*;
import java.net.*;

class transformer{
	
	public static void main(String [] args) {
		String convertUrl = "http://ice-service.usq.edu.au/api/convert/";
		//String fileName = "/Users/lindaoctalina/Sites/workspace/transformer/sefton.odt";
		if(args.length == 1) {
			String fileName = args[0];
			System.out.println(fileName);
			File f = new File(fileName);
			if(f.exists()) {
				getRendition(convertUrl, f);
			} else {
				System.out.println("Error: file doesn't exist!");
			}
		} else {
			System.out.println("Usage: java transformer fileName");
		}
	}
	
	public static void getRendition (String convertUrl, String fileName) {
		getRendition(convertUrl, new File(fileName));
	}
	
	public static void getRendition (String convertUrl, File sourceFile) {
		String[] parts = sourceFile.getName().split("\\.");
		String ext = "";
		if(parts.length == 2) { // check we have exactly two parts
			ext = parts[1];
		} else {
			System.out.println("Error: Unable to detect file extension properly!");
			return;
		}
		
		System.out.println("convertUrl: " + convertUrl);
		convertUrl = convertUrl + ext;
		System.out.println("fileName: " + sourceFile.getAbsolutePath());
		
		try{
				//Create connection
				URL url = new URL(convertUrl);
				
				//create a boundary string
				String boundary = MultiPartFormOutputStream.createBoundary();
				URLConnection urlConn = MultiPartFormOutputStream.createConnection(url);
				urlConn.setRequestProperty("Accept", "*/*");
				urlConn.setRequestProperty("Content-Type", 
					MultiPartFormOutputStream.getContentType(boundary));
				//set some other request headers...
				urlConn.setRequestProperty("Connection", "Keep-Alive");
				urlConn.setRequestProperty("Cache-Control", "no-cache");
				//no need to connect cuz getOutputStream() does it
				MultiPartFormOutputStream out = 
					new MultiPartFormOutputStream(urlConn.getOutputStream(), boundary);

				//Setting up all the options
				out.writeField("zip", "1");
				out.writeField("toc", "1");
				out.writeField("pdfLink", "1");
				out.writeField("pathext", "");
				out.writeField("template", getTemplate());
				out.writeFile("file", "application/zip", sourceFile);
				out.close();
				
			    //Get Response	
			    InputStream is = urlConn.getInputStream();
			    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			    
			    //zip 
			    String outputFilename = "/tmp/rendition.zip";
                System.out.println("Rendition file is saved in: "+ outputFilename);
			    FileOutputStream fos = new FileOutputStream(outputFilename);
	            Writer fileout = new OutputStreamWriter(fos);
	            int ch;
	            while ((ch = rd.read()) > -1) {
	                fileout.write((char)ch);
	            }
	            fileout.close();

			    
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getClass());
			//e.printStackTrace();
		}
	}
	
	private static String getTemplate (){
		String template = "<html>" +
  "<head>" +
    "<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>" +
    "<title>Default Template</title>" +
    "<style type='text/css'>" +
      ".rendition-links { text-align: right; }" +
      ".body table td { vertical-align: top; }" +
    "</style>" +
    "<style class='sub style-css' type='text/css'></style>" +
  "</head>" +
  "<body>" +
    "<div class='rendition-links'>" +
      "<span class='ins source-link'></span>" +
      "<span class='ins slide-link'></span>" +
      "<span class='ins pdf-rendition-link'></span>" +
    "</div>" +
    "<h1 class='ins title'></h1>" +
    "<div class='ins page-toc'></div>" +
    "<div class='ins body'></div>" +
  "</body>" +
"</html>";
		return template;
	}
}