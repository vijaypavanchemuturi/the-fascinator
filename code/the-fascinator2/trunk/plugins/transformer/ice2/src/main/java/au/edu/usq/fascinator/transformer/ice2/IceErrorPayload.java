/*
 * The Fascinator - Plugin - Transformer - ICE 2
 * Copyright (C) 2009 University of Southern Queensland
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
package au.edu.usq.fascinator.transformer.ice2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;

import au.edu.usq.fascinator.api.storage.PayloadType;
import au.edu.usq.fascinator.common.storage.impl.GenericPayload;

/**
 * Error payload to describe why an ICE render failed
 * 
 * @author Linda Octalina
 * @author Oliver Lucido
 */
public class IceErrorPayload extends GenericPayload {

    private String message;

    public IceErrorPayload(File file, String message) {
        this.message = message;
        String name = FilenameUtils.getBaseName(file.getName()) + "_error.htm";
        setId(name);
        setLabel("ICE conversion errors");
        setContentType("text/html");
        setType(PayloadType.Enrichment);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(message.getBytes("UTF-8"));
    }
}
