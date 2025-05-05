<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output indent="no" method="html" encoding="utf-8"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="/">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- remove BIB element - only contains metadata -->
    <xsl:template match="BIB.JUDGMENT | BIB.ORDER"/>
    <!-- remove CURR.TITLE - doesn't need to be displayed on the web -->
    <xsl:template match="CURR.TITLE"/>

    <!-- display keywords in a paragraph https://op.europa.eu/documents/3938058/5910419/formex_manual_on_screen_version.html/#INDEX -->
    <xsl:template match="INDEX">
        <p>
            <xsl:apply-templates select="TITLE"/>
            <xsl:value-of select="@IDX.OPEN"/>
            <xsl:for-each select="KEYWORD">
                <xsl:apply-templates/>
                <xsl:if test="position() != last()">
                    <xsl:value-of select="../@SEPARATOR"/>
                </xsl:if>
            </xsl:for-each>
            <xsl:value-of select="@IDX.CLOSE"/>
        </p>
    </xsl:template>

    <!-- wrap INTRO text node in p tags -->
    <xsl:template match="INTRO/text()">
        <p>
            <xsl:value-of select="."/>
        </p>
    </xsl:template>

    <!-- transform TXT and P tags to p tags -->
    <xsl:template match="TXT | P">
        <p>
            <xsl:apply-templates/>
        </p>
    </xsl:template>

    <!-- format highlighted text https://op.europa.eu/documents/3938058/5910419/formex_manual_on_screen_version.html/#HT -->
    <xsl:template match="HT[@TYPE = 'BOLD']">
        <b>
            <xsl:apply-templates/>
        </b>
    </xsl:template>

    <xsl:template match="HT[@TYPE = 'ITALIC']">
        <em>
            <xsl:apply-templates/>
        </em>
    </xsl:template>

    <xsl:template match="HT[@TYPE = 'SC']">
        <span style="font-variant-caps: small-caps; color: red;">
            <xsl:apply-templates/>
        </span>
    </xsl:template>

    <xsl:template match="HT[@TYPE = 'UNDERLINE']">
        <u>
            <xsl:apply-templates/>
        </u>
    </xsl:template>

    <xsl:template match="HT[@TYPE = 'EXPANDED']">
        <span style="letter-spacing: 2px; color: blue;">
            <xsl:apply-templates/>
        </span>
    </xsl:template>

    <xsl:template match="HT">
        <xsl:choose>
            <xsl:when test="@TYPE='BOLD'">
                <b>
                    <xsl:apply-templates/>
                </b>
            </xsl:when>
            <xsl:when test="@TYPE='ITALIC'">
                <em>
                    <xsl:apply-templates/>
                </em>
            </xsl:when>
            <xsl:when test="@TYPE='UNDERLINE'">
                <u>
                    <xsl:apply-templates/>
                </u>
            </xsl:when>
            <xsl:when test="@TYPE='STROKE'">
                <s>
                    <xsl:apply-templates/>
                </s>
            </xsl:when>
            <xsl:when test="@TYPE='SUB'">
                <sub>
                    <xsl:apply-templates/>
                </sub>
            </xsl:when>
            <xsl:when test="@TYPE='SUP'">
                <sup>
                    <xsl:apply-templates/>
                </sup>
            </xsl:when>
            <xsl:when test="@TYPE='UC'">
                <span style="text-transform: uppercase; color: purple;">
                    <xsl:apply-templates/>
                </span>
            </xsl:when>
            <xsl:when test="@TYPE='SC'">
                <span style="font-variant-caps: small-caps; color: red;">
                    <xsl:apply-templates/>
                </span>
            </xsl:when>
            <xsl:when test="@TYPE='EXPANDED'">
                <span style="letter-spacing: 2px; color: blue;">
                    <xsl:apply-templates/>
                </span>
            </xsl:when>
            <xsl:when test="@TYPE='BOX'">
                <span style="outline: solid black; color: magenta;">
                    <xsl:apply-templates/>
                </span>
            </xsl:when>
            <xsl:otherwise>
                <span>
                    <xsl:apply-templates/>
                </span>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- format opening quotation marks https://op.europa.eu/documents/3938058/5910419/formex_manual_on_screen_version.html/#QUOT.START -->
    <xsl:template match="QUOT.START">
        <xsl:call-template name="unicode-char">
            <xsl:with-param name="code" select="@CODE"/>
        </xsl:call-template>
    </xsl:template>

    <!-- format closing quotation marks https://op.europa.eu/documents/3938058/5910419/formex_manual_on_screen_version.html/#QUOT.END -->
    <xsl:template match="QUOT.END">
        <xsl:call-template name="unicode-char">
            <xsl:with-param name="code" select="@CODE"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="unicode-char">
        <xsl:param name="code"/>

        <xsl:choose>
            <xsl:when test="$code = '2018'">
                <xsl:text>‘</xsl:text>
            </xsl:when>
            <xsl:when test="$code = '2019'">
                <xsl:text>’</xsl:text>
            </xsl:when>
            <xsl:when test="$code = '201A'">
                <xsl:text>‚</xsl:text>
            </xsl:when>
            <xsl:when test="$code = '201B'">
                <xsl:text>‛</xsl:text>
            </xsl:when>
            <xsl:when test="$code = '201C'">
                <xsl:text>“</xsl:text>
            </xsl:when>
            <xsl:when test="$code = '201D'">
                <xsl:text>”</xsl:text>
            </xsl:when>
            <xsl:when test="$code = '201E'">
                <xsl:text>„</xsl:text>
            </xsl:when>

            <xsl:when test="$code = '201F'">
                <xsl:text>‟</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <span style="color:red">[Unknown code]</span>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- format lists based on types https://op.europa.eu/documents/3938058/5910419/formex_manual_on_screen_version.html/#LIST -->
    <xsl:template match="LIST">
        <xsl:choose>
            <xsl:when test="@TYPE='NDASH'">
                <ul style="list-style-type:'- ';">
                    <xsl:for-each select="ITEM">
                        <li><xsl:apply-templates/></li>
                    </xsl:for-each>
                </ul>
            </xsl:when>
            <xsl:when test="@TYPE='BULLET'">
                <ul>
                    <xsl:for-each select="ITEM">
                        <li><xsl:apply-templates/></li>
                    </xsl:for-each>
                </ul>
            </xsl:when>
            <xsl:otherwise>
                <dl>
                    <xsl:for-each select="ITEM/NP">
                            <xsl:call-template  name="NumberedParagraph"/>
                    </xsl:for-each>
                </dl>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- transform numbered paragraphs outside of lists https://op.europa.eu/documents/3938058/5910419/formex_manual_on_screen_version.html/#NP -->
    <xsl:template match="NP">
        <xsl:choose>
            <xsl:when test="parent::ITEM"/> <!-- ignore NP inside list items - they have already been processed -->
            <xsl:otherwise>
                <dl>
                    <xsl:call-template name="NumberedParagraph"/>
                </dl>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <xsl:template name="NumberedParagraph">
        <dt><xsl:apply-templates select="NO.P"/></dt>
        <dd><xsl:apply-templates select="*[not(self::NO.P)]"/></dd>
    </xsl:template>

    <!-- transform border numbers https://op.europa.eu/documents/3938058/5910419/formex_manual_on_screen_version.html/#NP.ECR -->
    <xsl:template match="NP.ECR">
        <border-number>
            <number>
                <xsl:apply-templates select="NO.P"/>
            </number>
            <content>
                <xsl:apply-templates select="*[not(self::NO.P)]"/>
            </content>
        </border-number>
    </xsl:template>

</xsl:stylesheet>
