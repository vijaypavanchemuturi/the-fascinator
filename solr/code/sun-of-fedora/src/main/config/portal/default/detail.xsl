<?xml version="1.0" encoding="UTF-8"?>
<!--
  Sun of Fedora - Solr Portal
  Copyright (C) 2008  University of Southern Queensland
  
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along
  with this program; if not, write to the Free Software Foundation, Inc.,
  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
    xmlns:dc="http://purl.org/dc/elements/1.1/" exclude-result-prefixes="oai_dc dc">

    <xsl:output indent="yes"/>

    <xsl:template match="oai_dc:dc">
      <div class="item">
        <ul>
          <xsl:apply-templates/>
        </ul>
      </div>
    </xsl:template>

    <!-- title -->
    <xsl:template match="dc:title">
      <h2><xsl:apply-templates/></h2>
    </xsl:template>
    
    <!-- basic dublin core field-->
    <xsl:template match="dc:*">
      <li>
        <strong><xsl:value-of select="local-name()"/></strong>: <xsl:apply-templates/>
      </li>
    </xsl:template>

    <!-- normalise whitespace for text -->
    <xsl:template match="text()">
        <xsl:value-of select="normalize-space(.)"/>
    </xsl:template>

</xsl:stylesheet>
