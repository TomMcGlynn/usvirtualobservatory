<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:msxsl="urn:schemas-microsoft-com:xslt" exclude-result-prefixes="msxsl"
>
    <xsl:output method="xml" indent="yes"/>

  <xsl:template match="VOTABLE">
    <xsl:apply-templates select="VOTABLE"/>
 <NewConfig>
      <Databases>
        <xsl:apply-templates select="RESOURCE">
          <xsl:with-param name="desc" select="DESCRIPTION"/>
        </xsl:apply-templates>
        </Databases>
  </NewConfig>
  </xsl:template>

  <xsl:template match="RESOURCE">
    <xsl:param name="desc"></xsl:param>
    <Database>
      <xsl:attribute name="schemaName">
        <xsl:value-of select="TABLE/@name"/>
      </xsl:attribute>
      <xsl:attribute name="database">
        <xsl:value-of select="INFO/@value"/>
      </xsl:attribute>
      <xsl:attribute name="description">
        <xsl:value-of select="''"/>
      </xsl:attribute>
      <xsl:attribute name="utype">
        <xsl:value-of select="'no utype'"/>
      </xsl:attribute>
      <xsl:element name="Connection">
        <xsl:attribute name="value">
          <xsl:value-of  select="'Server=server;UID=web user;Pwd=pwd;Database=dbname'"/>
        </xsl:attribute>
      </xsl:element>
      <Tables>
        <Table>
          <xsl:attribute name="name">
            <xsl:value-of select="TABLE/@internalName"/>
          </xsl:attribute>
          <xsl:attribute name="internalName">
            <xsl:value-of select="INFO/@value"/>
          </xsl:attribute>        
          <xsl:attribute name="type">
            <xsl:value-of select="'table'"/>
          </xsl:attribute>
          <Description>
            <xsl:value-of select="$desc"/>
          </Description>
          <Utype>Table.utype</Utype>
          <Columns>
            <xsl:apply-templates select="TABLE/FIELD"/>
          </Columns>
        </Table>
      </Tables>
    </Database>
  </xsl:template>

  <xsl:template match="FIELD">
    <xsl:variable name="queryable">
      <xsl:for-each select="VALUES/OPTION">
        <xsl:if test="@name = 'queryable' and @value = 'yes'">
          <xsl:text>true</xsl:text>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>
    <xsl:if test="$queryable='true'">
      <Column>
        <!--<xsl:attribute name="name">
        <xsl:value-of select="@name"/>
      </xsl:attribute>-->
        <xsl:apply-templates select="VALUES" />
        <xsl:element name="Description">
          <xsl:value-of select="DESCRIPTION"/>
        </xsl:element>
        <xsl:if test="@datatype and string(@datatype)">
          <xsl:element name="Datatype">
            <xsl:value-of select="@datatype"/>
          </xsl:element>
        </xsl:if>
        <xsl:if test="@utype and string(@utype)">
          <xsl:element name="Utype">
            <xsl:value-of select="@utype"/>
          </xsl:element>
        </xsl:if>
        <xsl:if test="@ucd and string(@ucd)">
          <xsl:element name="Ucd">
            <xsl:value-of select="@ucd"/>
          </xsl:element>
        </xsl:if>
      </Column>
    </xsl:if>
  </xsl:template>

  <xsl:template match="VALUES">
    <xsl:for-each select="OPTION">
      <xsl:choose>
        <xsl:when test="@name = 'column_name'">
          <xsl:attribute name="name">
            <xsl:value-of select="@value"/>
          </xsl:attribute>
          <xsl:attribute name="internalName">
            <xsl:value-of select="@value"/>
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="@name = 'default_output'">
          <xsl:attribute name="std">
            <xsl:choose>
              <xsl:when test="@value = 'yes'">
                <xsl:value-of select="'true'"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'false'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="@name = 'units' and string(@value)">
          <xsl:element name="Unit">
            <xsl:value-of select="@value"/>
          </xsl:element>
        </xsl:when>
        <xsl:when test="@name = 'indexed'">
          <xsl:element name="Indexed">
            <xsl:choose>
              <xsl:when test="@value = 'yes'">
                <xsl:value-of select="'true'"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'false'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>
        </xsl:when>
          <xsl:otherwise>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>
  
  
</xsl:stylesheet>
