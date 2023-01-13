package de.bund.digitalservice.ris.norms.framework.adapter.output.xml

import org.junit.jupiter.api.Test

class HtmlToXmlConverterTest {
    @Test
    fun `it creates xml from html`() {
        toContentDto(
            "Dieses Gesetz regelt die Zuständigkeit der deutschen Sozialversicherungsträger und anderer für die\n" +
                "                    soziale Sicherheit zuständiger Träger und Behörden bei der Anwendung und Durchführung folgender\n" +
                "                    Verordnungen in ihrer jeweils geltenden Fassung:\n" +
                "                    <DL Type=\"arabic\">\n" +
                "                            <DT>1.</DT>\n" +
                "                        <DD Font=\"normal\">\n" +
                "                            <LA>der Verordnung (EG) Nr. 883/2004 des Europäischen Parlaments und des Rates vom 29. April\n" +
                "                                2004 zur Koordinierung der Systeme der sozialen Sicherheit (ABl. L 166 vom 30.4.2004, S.\n" +
                "                                1, L 200 vom 7.6.2004, S. 1), die zuletzt durch die Verordnung (EG) Nr. 988/2009 (ABl. L\n" +
                "                                284 vom 30.10.2009, S. 43) geändert worden ist, und\n" +
                "                            </LA>\n" +
                "                        </DD>\n" +
                "                        <DT>2.</DT>\n" +
                "                        <DD Font=\"normal\">\n" +
                "                            <LA>der Verordnung (EG) Nr. 987/2009 des Europäischen Parlaments und des Rates vom 16.\n" +
                "                                September 2009 zur Festlegung der Modalitäten für die Durchführung der Verordnung (EG)\n" +
                "                                Nr. 883/2004 über die Koordinierung der Systeme der sozialen Sicherheit (ABl. L 284 vom\n" +
                "                                30.10.2009, S. 1).\n" +
                "                                <Rec></Rec>\n" +
                "                            </LA>\n" +
                "                        </DD>\n" +
                "                    </DL>\n" +
                "                "
        )
    }

    @Test
    fun `nested lists`() {
        toContentDto(
            "Zugangsstellen für den elektronischen Datenaustausch nach Artikel 1 Absatz 2 Buchstabe a der\n" +
                "                    Verordnung (EG) Nr. 987/2009 sind\n" +
                "                    <DL Font=\"normal\" Type=\"arabic\">\n" +
                "                        <DT>1.</DT>\n" +
                "                        <DD Font=\"normal\">\n" +
                "                            <LA Size=\"normal\">der Spitzenverband Bund der Krankenkassen, Deutsche Verbindungsstelle\n" +
                "                                Krankenversicherung – Ausland,\n" +
                "                                <DL Font=\"normal\" Type=\"alpha\">\n" +
                "                                    <DT>a)</DT>\n" +
                "                                    <DD Font=\"normal\">\n" +
                "                                        <LA Size=\"normal\">für den Bereich der Leistungen bei Krankheit sowie der\n" +
                "                                            Leistungen bei Mutterschaft und gleichgestellter Leistungen bei Vaterschaft\n" +
                "                                            (Artikel 3 Absatz 1 Buchstabe a und b der Verordnung (EG) Nr. 883/2004),\n" +
                "                                        </LA>\n" +
                "                                    </DD>\n" +
                "                                    <DT>b)</DT>\n" +
                "                                    <DD Font=\"normal\">\n" +
                "                                        <LA Size=\"normal\">für den Bereich des anwendbaren Rechts in den Fällen\n" +
                "                                            <DL Font=\"normal\" Type=\"a-alpha\">\n" +
                "                                                <DT>aa)</DT>\n" +
                "                                                <DD Font=\"normal\">\n" +
                "                                                    <LA Size=\"normal\">des Artikels 15 der Verordnung (EG) Nr.\n" +
                "                                                        987/2009,\n" +
                "                                                    </LA>\n" +
                "                                                </DD>\n" +
                "                                                <DT>bb)</DT>\n" +
                "                                                <DD Font=\"normal\">\n" +
                "                                                    <LA Size=\"normal\">des Artikels 16 der Verordnung (EG) Nr. 987/2009,\n" +
                "                                                        wenn der Wohnort der betreffenden Person in Deutschland liegt,\n" +
                "                                                    </LA>\n" +
                "                                                </DD>\n" +
                "                                                <DT>cc)</DT>\n" +
                "                                                <DD Font=\"normal\">\n" +
                "                                                    <LA Size=\"normal\">des Artikels 17 der Verordnung (EG) Nr.\n" +
                "                                                        987/2009,\n" +
                "                                                    </LA>\n" +
                "                                                </DD>\n" +
                "                                                <DT>dd)</DT>\n" +
                "                                                <DD Font=\"normal\">\n" +
                "                                                    <LA Size=\"normal\">des Artikels 18 der Verordnung (EG) Nr.\n" +
                "                                                        987/2009;\n" +
                "                                                    </LA>\n" +
                "                                                </DD>\n" +
                "                                            </DL>\n" +
                "                                        </LA>\n" +
                "                                    </DD>\n" +
                "                                </DL>\n" +
                "                            </LA>\n" +
                "                        </DD>\n" +
                "                        <DT>2.</DT>\n" +
                "                        <DD Font=\"normal\">\n" +
                "                            <LA Size=\"normal\">die Deutsche Gesetzliche Unfallversicherung e. V., Deutsche\n" +
                "                                Verbindungsstelle Unfallversicherung – Ausland, für den Bereich der Leistungen bei\n" +
                "                                Arbeitsunfällen und Berufskrankheiten sowie des Sterbegeldes (Artikel 3 Absatz 1\n" +
                "                                Buchstabe f und g der Verordnung (EG) Nr. 883/2004);\n" +
                "                            </LA>\n" +
                "                        </DD>\n" +
                "                        <DT>3.</DT>\n" +
                "                        <DD Font=\"normal\">\n" +
                "                            <LA Size=\"normal\">die Datenstelle der Rentenversicherung\n" +
                "                                <DL Font=\"normal\" Type=\"alpha\">\n" +
                "                                    <DT>a)</DT>\n" +
                "                                    <DD Font=\"normal\">\n" +
                "                                        <LA Size=\"normal\">für den Bereich der Leistungen bei Invalidität, bei Alter und\n" +
                "                                            an Hinterbliebene sowie der Vorruhestandsleistungen (Artikel 3 Absatz 1\n" +
                "                                            Buchstabe c bis e und i der Verordnung (EG) Nr. 883/2004),\n" +
                "                                        </LA>\n" +
                "                                    </DD>\n" +
                "                                    <DT>b)</DT>\n" +
                "                                    <DD Font=\"normal\">\n" +
                "                                        <LA Size=\"normal\">für den Bereich des anwendbaren Rechts in den Fällen\n" +
                "                                            <DL Font=\"normal\" Type=\"a-alpha\">\n" +
                "                                                <DT>aa)</DT>\n" +
                "                                                <DD Font=\"normal\">\n" +
                "                                                    <LA Size=\"normal\">des Artikels 15 der Verordnung (EG) Nr.\n" +
                "                                                        987/2009,\n" +
                "                                                    </LA>\n" +
                "                                                </DD>\n" +
                "                                                <DT>bb)</DT>\n" +
                "                                                <DD Font=\"normal\">\n" +
                "                                                    <LA Size=\"normal\">des Artikels 16 der Verordnung (EG) Nr. 987/2009,\n" +
                "                                                        wenn der Wohnort der betreffenden Person außerhalb Deutschlands\n" +
                "                                                        liegt;\n" +
                "                                                    </LA>\n" +
                "                                                </DD>\n" +
                "                                            </DL>\n" +
                "                                        </LA>\n" +
                "                                    </DD>\n" +
                "                                </DL>\n" +
                "                            </LA>\n" +
                "                        </DD>\n" +
                "                        <DT>4.</DT>\n" +
                "                        <DD Font=\"normal\">\n" +
                "                            <LA Size=\"normal\">die Bundesagentur für Arbeit für den Bereich der Leistungen bei\n" +
                "                                Arbeitslosigkeit (Artikel 3 Absatz 1 Buchstabe h der Verordnung (EG) Nr. 883/2004);\n" +
                "                            </LA>\n" +
                "                        </DD>\n" +
                "                        <DT>5.</DT>\n" +
                "                        <DD Font=\"normal\">\n" +
                "                            <LA Size=\"normal\">die Bundesagentur für Arbeit, Familienkasse Direktion, für den Bereich der\n" +
                "                                Familienleistungen (Artikel 3 Absatz 1 Buchstabe j der Verordnung (EG) Nr. 883/2004).\n" +
                "                                <Rec></Rec>\n" +
                "                            </LA>\n" +
                "                        </DD>\n" +
                "                    </DL>"
        )
    }
}
