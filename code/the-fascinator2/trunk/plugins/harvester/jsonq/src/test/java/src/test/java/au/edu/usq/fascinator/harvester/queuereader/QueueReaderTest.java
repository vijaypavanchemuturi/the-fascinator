package src.test.java.au.edu.usq.fascinator.harvester.queuereader;

import au.edu.usq.fascinator.harvester.queuereader.QueueReader;
import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.Assert;

public class QueueReaderTest {
	String urlStr = "http://localhost:9000";
	
	//For now just build a simple test 
	@Test
	public void setup() {
		try {
			QueueReader qr = new QueueReader(new URL(urlStr));
			Assert.assertEquals(new URL(urlStr), qr.getUrl());
			
			qr.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					System.out.println(evt.getPropertyName() + ": "
							+ evt.getNewValue());
				}
			});
			qr.checkQueue();
			
			Assert.assertEquals(200, qr.getResponseCode());
			Assert.assertEquals("application/json", qr.getContentType());
			//getContent
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
