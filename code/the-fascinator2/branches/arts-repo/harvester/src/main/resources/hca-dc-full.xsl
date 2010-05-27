<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:seer="http://seer.arc.gov.au/2009/seer/1" xmlns:usq="http://usq.edu.au/research/">

    <xsl:template match="usq:hcaOutput">
        <oai_dc:dc>
            <xsl:apply-templates />
        </oai_dc:dc>
    </xsl:template>

    <xsl:template match="usq:title">
        <dc:title>
            <xsl:value-of select="@nativeScript" />
        </dc:title>
    </xsl:template>

    <xsl:template match="usq:otherLocation|usq:placeOfPublication">
        <dc:coverage>
            <xsl:value-of select="." />
        </dc:coverage>
    </xsl:template>

    <xsl:template match="usq:extent">
        <dc:extent>
            <xsl:value-of select="." />
        </dc:extent>
    </xsl:template>

    <xsl:template match="usq:creators">
        <xsl:for-each select="creator">
            <dc:creator>
                <xsl:value-of select="@lastName" />
                <xsl:text>, </xsl:text>
                <xsl:value-of select="@firstName" />
            </dc:creator>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="usq:seoCode">
        <dc:subject>
            <xsl:value-of select="." />
        </dc:subject>
    </xsl:template>

    <xsl:template match="usq:category">
        <dc:subject>
            <xsl:value-of select="@usq:category" />
        </dc:subject>
    </xsl:template>

    <xsl:template match="usq:eraCategory/originalCreativeWork">
        <dc:subject>
            <xsl:value-of select="@outputType" />
        </dc:subject>
    </xsl:template>

    <xsl:template match="usq:eraCategory/livePerformance">
        <dc:subject>
            <xsl:value-of select="@performanceType" />
        </dc:subject>
    </xsl:template>

    <xsl:template match="usq:eraCategory/recordedOrRenderedWork">
        <dc:subject>
            <xsl:value-of select="@workType" />
        </dc:subject>
    </xsl:template>

    <xsl:template match="usq:eraCategory/curatedOrExhibitionWork">
        <dc:subject>
            <xsl:value-of select="@workType" />
        </dc:subject>
    </xsl:template>

    <xsl:template match="usq:research_statement">
        <dc:description>
            <xsl:value-of select="." />
        </dc:description>
    </xsl:template>

    <xsl:template match="text()">
        <xsl:value-of select="normalize-space(.)" />
    </xsl:template>

    <xsl:template match="*">
    </xsl:template>

</xsl:stylesheet>
