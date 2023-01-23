import { FrameData, Norm, NullableFrameData } from "@/domain/Norm"

type StringCallback = (value: NullableString) => NullableString
type BooleanCallback = (value: NullableBoolean) => NullableBoolean
export type NullableString = string | undefined | null
export type NullableBoolean = boolean | undefined | null

export function getFrameDataFromNorm(norm: Norm): FrameData {
  const { guid, articles, ...frameData } = norm
  return frameData
}

export function applyToFrameData(
  frameData: FrameData | undefined,
  stringCallback: StringCallback,
  booleanCallback: BooleanCallback,
  dateCallback: StringCallback
): NullableFrameData {
  return {
    documentTemplateName: stringCallback(frameData?.documentTemplateName),
    leadUnit: stringCallback(frameData?.leadUnit),
    participationInstitution: stringCallback(
      frameData?.participationInstitution
    ),
    subjectBgb3: stringCallback(frameData?.subjectBgb3),
    ageIndicationEnd: stringCallback(frameData?.ageIndicationEnd),
    ageIndicationStart: stringCallback(frameData?.ageIndicationStart),
    ageOfMajorityIndication: stringCallback(frameData?.ageOfMajorityIndication),
    announcementDate: dateCallback(frameData?.announcementDate),
    applicationScopeArea: stringCallback(frameData?.applicationScopeArea),
    applicationScopeEndDate: dateCallback(frameData?.applicationScopeEndDate),
    applicationScopeStartDate: dateCallback(
      frameData?.applicationScopeStartDate
    ),
    categorizedReference: stringCallback(frameData?.categorizedReference),
    celexNumber: stringCallback(frameData?.celexNumber),
    citationDate: dateCallback(frameData?.citationDate),
    completeCitation: stringCallback(frameData?.completeCitation),
    definition: stringCallback(frameData?.definition),
    digitalAnnouncementDate: dateCallback(frameData?.digitalAnnouncementDate),
    digitalAnnouncementArea: stringCallback(frameData?.digitalAnnouncementArea),
    digitalAnnouncementAreaNumber: stringCallback(
      frameData?.digitalAnnouncementAreaNumber
    ),
    digitalAnnouncementEdition: stringCallback(
      frameData?.digitalAnnouncementEdition
    ),
    digitalAnnouncementExplanations: stringCallback(
      frameData?.digitalAnnouncementExplanations
    ),
    digitalAnnouncementInfo: stringCallback(frameData?.digitalAnnouncementInfo),
    digitalAnnouncementMedium: stringCallback(
      frameData?.digitalAnnouncementMedium
    ),
    digitalAnnouncementPage: stringCallback(frameData?.digitalAnnouncementPage),
    digitalAnnouncementYear: stringCallback(frameData?.digitalAnnouncementYear),
    digitalEvidenceAppendix: stringCallback(frameData?.digitalEvidenceAppendix),
    digitalEvidenceExternalDataNote: stringCallback(
      frameData?.digitalEvidenceExternalDataNote
    ),
    digitalEvidenceLink: stringCallback(frameData?.digitalEvidenceLink),
    digitalEvidenceRelatedData: stringCallback(
      frameData?.digitalEvidenceRelatedData
    ),
    divergentDocumentNumber: stringCallback(frameData?.divergentDocumentNumber),
    divergentEntryIntoForceDate: dateCallback(
      frameData?.divergentEntryIntoForceDate
    ),
    divergentEntryIntoForceDateState: stringCallback(
      frameData?.divergentEntryIntoForceDateState
    ),
    divergentExpirationDate: dateCallback(frameData?.divergentExpirationDate),
    divergentExpirationDateState: stringCallback(
      frameData?.divergentExpirationDateState
    ),
    documentCategory: stringCallback(frameData?.documentCategory),
    documentNormCategory: stringCallback(frameData?.documentNormCategory),
    documentNumber: stringCallback(frameData?.documentNumber),
    documentStatusDate: dateCallback(frameData?.documentStatusDate),
    documentStatusDescription: stringCallback(
      frameData?.documentStatusDescription
    ),
    documentStatusEntryIntoForceDate: dateCallback(
      frameData?.documentStatusEntryIntoForceDate
    ),
    documentStatusProof: stringCallback(frameData?.documentStatusProof),
    documentStatusReference: stringCallback(frameData?.documentStatusReference),
    documentStatusWorkNote: stringCallback(frameData?.documentStatusWorkNote),
    documentTextProof: stringCallback(frameData?.documentTextProof),
    documentTypeName: stringCallback(frameData?.documentTypeName),
    entryIntoForceDate: dateCallback(frameData?.entryIntoForceDate),
    entryIntoForceDateState: stringCallback(frameData?.entryIntoForceDateState),
    entryIntoForceNormCategory: stringCallback(
      frameData?.entryIntoForceNormCategory
    ),
    euAnnouncementExplanations: stringCallback(
      frameData?.euAnnouncementExplanations
    ),
    euAnnouncementGazette: stringCallback(frameData?.euAnnouncementGazette),
    euAnnouncementInfo: stringCallback(frameData?.euAnnouncementInfo),
    euAnnouncementNumber: stringCallback(frameData?.euAnnouncementNumber),
    euAnnouncementPage: stringCallback(frameData?.euAnnouncementPage),
    euAnnouncementSeries: stringCallback(frameData?.euAnnouncementSeries),
    euAnnouncementYear: stringCallback(frameData?.euAnnouncementYear),
    eli: stringCallback(frameData?.eli),
    expirationDate: dateCallback(frameData?.expirationDate),
    expirationDateState: stringCallback(frameData?.expirationDateState),
    expirationNormCategory: stringCallback(frameData?.expirationNormCategory),
    frameKeywords: stringCallback(frameData?.frameKeywords),
    isExpirationDateTemp: booleanCallback(frameData?.isExpirationDateTemp),
    leadJurisdiction: stringCallback(frameData?.leadJurisdiction),
    officialAbbreviation: stringCallback(frameData?.officialAbbreviation),
    officialLongTitle: stringCallback(frameData?.officialLongTitle) ?? "",
    officialShortTitle: stringCallback(frameData?.officialShortTitle),
    otherDocumentNote: stringCallback(frameData?.otherDocumentNote),
    otherFootnote: stringCallback(frameData?.otherFootnote),
    footnoteChange: stringCallback(frameData?.footnoteChange),
    footnoteComment: stringCallback(frameData?.footnoteComment),
    footnoteDecision: stringCallback(frameData?.footnoteDecision),
    footnoteStateLaw: stringCallback(frameData?.footnoteStateLaw),
    footnoteEuLaw: stringCallback(frameData?.footnoteEuLaw),
    otherOfficialAnnouncement: stringCallback(
      frameData?.otherOfficialAnnouncement
    ),
    otherStatusNote: stringCallback(frameData?.otherStatusNote),
    participationType: stringCallback(frameData?.participationType),
    principleEntryIntoForceDate: dateCallback(
      frameData?.principleEntryIntoForceDate
    ),
    principleEntryIntoForceDateState: stringCallback(
      frameData?.principleEntryIntoForceDateState
    ),
    principleExpirationDate: dateCallback(frameData?.principleExpirationDate),
    principleExpirationDateState: stringCallback(
      frameData?.principleExpirationDateState
    ),
    printAnnouncementExplanations: stringCallback(
      frameData?.printAnnouncementExplanations
    ),
    printAnnouncementGazette: stringCallback(
      frameData?.printAnnouncementGazette
    ),
    printAnnouncementInfo: stringCallback(frameData?.printAnnouncementInfo),
    printAnnouncementNumber: stringCallback(frameData?.printAnnouncementNumber),
    printAnnouncementPage: stringCallback(frameData?.printAnnouncementPage),
    printAnnouncementYear: stringCallback(frameData?.printAnnouncementYear),
    providerEntity: stringCallback(frameData?.providerEntity),
    providerDecidingBody: stringCallback(frameData?.providerDecidingBody),
    providerIsResolutionMajority: booleanCallback(
      frameData?.providerIsResolutionMajority
    ),
    publicationDate: dateCallback(frameData?.publicationDate),
    referenceNumber: stringCallback(frameData?.referenceNumber),
    reissueArticle: stringCallback(frameData?.reissueArticle),
    reissueDate: dateCallback(frameData?.reissueDate),
    reissueNote: stringCallback(frameData?.reissueNote),
    reissueReference: stringCallback(frameData?.reissueReference),
    repealArticle: stringCallback(frameData?.repealArticle),
    repealDate: dateCallback(frameData?.repealDate),
    repealNote: stringCallback(frameData?.repealNote),
    repealReferences: stringCallback(frameData?.repealReferences),
    risAbbreviation: stringCallback(frameData?.risAbbreviation),
    risAbbreviationInternationalLaw: stringCallback(
      frameData?.risAbbreviationInternationalLaw
    ),
    statusDate: dateCallback(frameData?.statusDate),
    statusDescription: stringCallback(frameData?.statusDescription),
    statusNote: stringCallback(frameData?.statusNote),
    statusReference: stringCallback(frameData?.statusReference),
    subjectFna: stringCallback(frameData?.subjectFna),
    subjectGesta: stringCallback(frameData?.subjectGesta),
    subjectPreviousFna: stringCallback(frameData?.subjectPreviousFna),
    text: stringCallback(frameData?.text),
    unofficialAbbreviation: stringCallback(frameData?.unofficialAbbreviation),
    unofficialLongTitle: stringCallback(frameData?.unofficialLongTitle),
    unofficialReference: stringCallback(frameData?.unofficialReference),
    unofficialShortTitle: stringCallback(frameData?.unofficialShortTitle),
    validityRule: stringCallback(frameData?.validityRule),
  }
}
