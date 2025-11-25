package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlValue;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Rechtsmittel {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Rechtsmittel";

  @XmlElementWrapper(name = "rechtsmittelfuehrerListe", namespace = CaseLawLdml.RIS_NS)
  @XmlElement(name = "rechtsmittelfuehrer", namespace = CaseLawLdml.RIS_NS)
  private List<Rechtsmittelfuehrer> rechtsmittelfuehrer;

  @XmlElementWrapper(name = "revisionKlaegerListe", namespace = CaseLawLdml.RIS_NS)
  @XmlElement(name = "revisionKlaeger", namespace = CaseLawLdml.RIS_NS)
  private List<RevisionKlaeger> revisionKlaeger;

  @XmlElementWrapper(name = "revisionBeklagterListe", namespace = CaseLawLdml.RIS_NS)
  @XmlElement(name = "revisionBeklagter", namespace = CaseLawLdml.RIS_NS)
  private List<RevisionBeklagter> revisionBeklagte;

  @XmlElementWrapper(name = "anschlussRevisionKlaegerListe", namespace = CaseLawLdml.RIS_NS)
  @XmlElement(name = "anschlussRevisionKlaeger", namespace = CaseLawLdml.RIS_NS)
  private List<AnschlussRevisionKlaeger> anschlussRevisionKlaeger;

  @XmlElementWrapper(name = "anschlussRevisionBeklagterListe", namespace = CaseLawLdml.RIS_NS)
  @XmlElement(name = "anschlussRevisionBeklagter", namespace = CaseLawLdml.RIS_NS)
  private List<AnschlussRevisionBeklagter> anschlussRevisionBeklagte;

  @XmlElementWrapper(name = "nzbKlaegerListe", namespace = CaseLawLdml.RIS_NS)
  @XmlElement(name = "nzbKlaeger", namespace = CaseLawLdml.RIS_NS)
  private List<NzbKlaeger> nzbKlaeger;

  @XmlElementWrapper(name = "nzbBeklagterListe", namespace = CaseLawLdml.RIS_NS)
  @XmlElement(name = "nzbBeklagter", namespace = CaseLawLdml.RIS_NS)
  private List<NzbBeklagter> nzbBeklagte;

  @XmlElement(name = "zuruecknahmeDerRevision", namespace = CaseLawLdml.RIS_NS)
  private ZuruecknahmeDerRevision zuruecknahmeDerRevision;

  @XmlElement(name = "pkhAntragKlaeger", namespace = CaseLawLdml.RIS_NS)
  private PkhAntragKlaeger pkhAntragKlaeger;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Rechtsmittelfuehrer {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Rechtsmittelführer";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class RevisionKlaeger {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Revision (Kläger)";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class RevisionBeklagter {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Revision (Beklagter)";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class AnschlussRevisionKlaeger {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Anschlussrevision (Kläger)";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class AnschlussRevisionBeklagter {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Anschlussrevision (Beklagter)";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class NzbKlaeger {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "NZB (Kläger)";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class NzbBeklagter {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "NZB (Beklagter)";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class ZuruecknahmeDerRevision {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Zurücknahme der Revision";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class PkhAntragKlaeger {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "PKH-Antrag (Kläger)";

    @XmlValue private String value;
  }
}
