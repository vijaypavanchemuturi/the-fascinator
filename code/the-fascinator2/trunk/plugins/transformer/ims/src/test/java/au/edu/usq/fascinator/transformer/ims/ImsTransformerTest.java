/*
 * The Fascinator - Plugin - Transformer - Ims
 * Copyright (C) 2010 University of Southern Queensland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package au.edu.usq.fascinator.transformer.ims;

import au.edu.usq.fascinator.api.PluginException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import au.edu.usq.fascinator.api.storage.DigitalObject;
import au.edu.usq.fascinator.api.storage.StorageException;
import au.edu.usq.fascinator.api.transformer.TransformerException;
import au.edu.usq.fascinator.common.storage.impl.GenericDigitalObject;

/**
 * NOTE: Count not perform the test to talk to ffmpeg directly as ffmpeg might
 * not be installed locally
 * 
 * @author Ron Ward, Linda Octalina
 * 
 */

public class ImsTransformerTest {

	private GenericDigitalObject zipObject;
	private DigitalObject imsDigitalObject;
	private ImsTransformer imsTransformer = new ImsTransformer();

	@Test
	public void testCheckIfZipIsImsPackage() throws URISyntaxException,
			StorageException, IOException {
		File zipFile = new File(getClass().getResource("/mybook.zip").toURI());

		zipObject = new GenericDigitalObject(zipFile.getAbsolutePath());
		imsDigitalObject = imsTransformer.createImsPayload(zipObject, zipFile);
		// Assert.assertTrue(imsDigitalObject.getIsImsPackage());
		Assert.assertEquals(imsDigitalObject.getPayloadIdList().size(), 194);
	}

	@Test
	public void testGetExt() throws URISyntaxException {
		ImsTransformer imsTransformer = new ImsTransformer();
		File zipFile = new File("mybook...zip");
		Assert.assertEquals(".zip", imsTransformer.getFileExt(zipFile));
	}

	@Test
	public void testTransform() throws URISyntaxException,
			TransformerException, PluginException {
		ImsTransformer imsTransformer = new ImsTransformer();
		File zipFile = new File(getClass().getResource("/mybook.zip").toURI());

		GenericDigitalObject zipObject = new GenericDigitalObject(zipFile
				.getAbsolutePath());

		imsTransformer.init("{}");
		DigitalObject object = imsTransformer.transform(zipObject);
		// System.out.println("000 " + object.getPayloadList());
	}

}
