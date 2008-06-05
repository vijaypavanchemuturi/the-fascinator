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

    <!-- repository name facet value -->
    <xsl:param name="repository-name" select="'Unknown'"/>
    <!--  identifier -->
    <xsl:param name="doc-id" select="''"/>
    <!--  pdf full text -->
    <xsl:param name="tmp-dir" select="'/tmp'"/>
    <!-- whether or not full text exists -->
    <xsl:param name="has-full-text" select="'false'"/>

    <!-- create a Solr document for each dublin core record -->
    <xsl:template match="oai_dc:dc">
        <xsl:variable name="id">
            <xsl:value-of select="$doc-id"/>
        </xsl:variable>
        <xsl:variable name="full-text">
            <xsl:value-of select="$tmp-dir"/>
            <xsl:text>/</xsl:text>
            <xsl:value-of select="translate($id, ':/', '._')" />
            <xsl:text>.fulltext.xml</xsl:text>
        </xsl:variable>
        <add allowDups="false">
        <doc>
            <field name="repository_name">
                <xsl:value-of select="$repository-name"/>
            </field>
            <field name="full_text">
                <xsl:if test="$has-full-text">
                    <xsl:value-of select="document($full-text)/fulltext"/>
                </xsl:if>
            </field>
            <field name="id">
                <xsl:value-of select="$id"/>
            </field>
            <xsl:apply-templates/>
        </doc>
        </add>
    </xsl:template>

    <!-- add a Solr field for every dublin core field-->
    <xsl:template match="dc:*">
        <field name="{local-name()}">
            <xsl:apply-templates/>
        </field>
    </xsl:template>

    <!-- normalise whitespace for text -->
    <xsl:template match="text()">
        <xsl:value-of select="normalize-space(.)"/>
    </xsl:template>

</xsl:stylesheet>
