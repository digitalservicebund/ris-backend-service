# Domain Model

```mermaid
classDiagram
  class Norm {
    +UUID guid
    +List~Article~ articles

    +String officialLongTitle
    String risAbbreviation
    String risAbbreviationInternationalLaw
    String documentNumber
    String divergentDocumentNumber
    String documentCategory
    String frameKeywords

    String documentTypeName
    String documentNormCategory
    String documentTemplateName

    String providerEntity
    String providerDecidingBody
    Boolean providerIsResolutionMajority

    String participationType
    String participationInstitution

    String leadJurisdiction
    String leadUnit

    String subjectFna
    String subjectPreviousFna
    String subjectGesta
    String subjectBgb3

    String officialShortTitle
    String officialAbbreviation
    String unofficialLongTitle
    String unofficialShortTitle
    String unofficialAbbreviation

    Date entryIntoForceDate
    UndefinedDate entryIntoForceDateState
    Date principleEntryIntoForceDate
    UndefinedDate principleEntryIntoForceDateState
    Date divergentEntryIntoForceDate
    UndefinedDate divergentEntryIntoForceDateState

    Date expirationDate
    String expirationDateState
    Boolean isExpirationDateTemp
    Date principleExpirationDate
    UndefinedDate principleExpirationDateState
    Date divergentExpirationDate
    UndefinedDate divergentExpirationDateState
    String expirationNormCategory

    Date announcementDate
    Date publicationDate

    Date citationDate

    String printAnnouncementGazette
    String printAnnouncementYear
    String printAnnouncementNumber
    String printAnnouncementPage
    String printAnnouncementInfo
    String printAnnouncementExplanations
    String digitalAnnouncementMedium
    Date digitalAccouncementDate
    String digitalAnnouncementEdition
    String digitalAnnouncementYear
    String digitalAnnouncementPage
    String digitalAnnouncementArea
    String digitalAnnouncementAreaNumber
    String digitalAnnouncementInfo
    String digitalAnnouncementExplanations
    String euAnnouncementGazette
    String euAnnouncementYear
    String euAnnouncementSeries
    String euAnnouncementNumber
    String euAnnouncementPage
    String euAnnouncementInfo
    String euAnnouncementExplanations
    String otherOfficialAnnouncement

    String unofficialReference

    String completeCitation

    String statusNote
    String statusDescription
    Date statusDate
    String statusReference
    String repealNote
    String repealArticle
    Date repealDate
    String repealReferences
    String reissueNote
    String reissueArticle
    Date reissueDate
    String reissueReference
    String otherStatusNote

    String documentStatusWorkNote
    String documentStatusDescription
    Date documentStatusDate
    String documentStatusReference
    Date documentStatusEntryIntoForceDate
    String documentStatusProof
    String documentTextProof
    String otherDocumentNote

    String applicationScopeArea
    Date applicationScopeStartDate
    Date applicationScopeEndDate

    String categorizedReference

    String otherFootnote

    String validityRule

    String digitalEvidenceLink
    String digitalEvidenceRelatedData
    String digitalEvidenceExternalDataNote
    String digitalEvidenceAppendix

    String referenceNumber

    String europeanLegalIdentifier

    String celexNumber

    String ageIndicationStart
    String ageIndicationEnd

    String definition

    String ageOfMajorityIndication

    Stirng text
  }

  class Article {
    +UUID guid
    +String marker
    +String title
    +List~Paragraph~ paragraphs
  }

  class Paragraph {
    +UUID guid
    +String marker
    +String text
  }


  class UndefinedDate  {
     <<enumeration>>
    UNDEFINED_UNKNOWN
    UNDEFINED_FUTURE
    UNDEFINED_NOT_PRESENT
  }

  Norm "1" --> "*" Article
  Article "1" --> "*" Paragraph
```
