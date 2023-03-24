import {
  FrameData,
  MetaDatum,
  MetaDatumType,
  Norm,
  NormResponse,
  NullableFrameData,
  NullableNormEditRequest,
} from "@/domain/Norm"

type StringCallback = (value: NullableString) => NullableString
type BooleanCallback = (value: NullableBoolean) => NullableBoolean
export type NullableString = string | undefined | null
export type NullableBoolean = boolean | undefined | null

export function getFrameDataFromNorm(norm: Norm): FrameData {
  const { guid, articles, files, ...frameData } = norm
  return frameData
}

export function getNormFromNormResponse(norm: NormResponse): Norm {
  const { metadata, ...data } = norm
  return {
    ...data,
    frameKeywords: getMetadatumFromMetadata(
      norm.metadata ?? [],
      MetaDatumType.KEYWORD
    ),
    validityRule: getMetadatumFromMetadata(
      norm.metadata ?? [],
      MetaDatumType.VALIDITY_RULE
    ),
    unofficialShortTitle: getMetadatumFromMetadata(
      norm.metadata ?? [],
      MetaDatumType.UNOFFICIAL_SHORT_TITLE
    ),
    unofficialReference: getMetadatumFromMetadata(
      norm.metadata ?? [],
      MetaDatumType.UNOFFICIAL_REFERENCE
    ),
    unofficialLongTitle: getMetadatumFromMetadata(
      norm.metadata ?? [],
      MetaDatumType.UNOFFICIAL_LONG_TITLE
    ),
    unofficialAbbreviation: getMetadatumFromMetadata(
      norm.metadata ?? [],
      MetaDatumType.UNOFFICIAL_ABBREVIATION
    ),
    risAbbreviationInternationalLaw: getMetadatumFromMetadata(
      norm.metadata ?? [],
      MetaDatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW
    ),
    referenceNumber: getMetadatumFromMetadata(
      norm.metadata ?? [],
      MetaDatumType.REFERENCE_NUMBER
    ),
    definition: getMetadatumFromMetadata(
      norm.metadata ?? [],
      MetaDatumType.DEFINITION
    ),
    ageOfMajorityIndication: getMetadatumFromMetadata(
      norm.metadata ?? [],
      MetaDatumType.AGE_OF_MAJORITY_INDICATION
    ),
    divergentDocumentNumber: getMetadatumFromMetadata(
      norm.metadata ?? [],
      MetaDatumType.DIVERGENT_DOCUMENT_NUMBER
    ),
  }
}
export function getNormEditRequestFromFrameData(
  frameData: NullableFrameData
): NullableNormEditRequest {
  const {
    frameKeywords,
    validityRule,
    unofficialShortTitle,
    unofficialReference,
    unofficialLongTitle,
    unofficialAbbreviation,
    risAbbreviationInternationalLaw,
    referenceNumber,
    definition,
    ageOfMajorityIndication,
    divergentDocumentNumber,
    ...data
  } = frameData
  const metadata: MetaDatum[] = []
  addMetadata(metadata, MetaDatumType.KEYWORD, frameKeywords)
  addMetadata(metadata, MetaDatumType.VALIDITY_RULE, validityRule)
  addMetadata(
    metadata,
    MetaDatumType.UNOFFICIAL_SHORT_TITLE,
    unofficialShortTitle
  )
  addMetadata(metadata, MetaDatumType.UNOFFICIAL_REFERENCE, unofficialReference)
  addMetadata(
    metadata,
    MetaDatumType.UNOFFICIAL_LONG_TITLE,
    unofficialLongTitle
  )
  addMetadata(
    metadata,
    MetaDatumType.UNOFFICIAL_ABBREVIATION,
    unofficialAbbreviation
  )
  addMetadata(
    metadata,
    MetaDatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW,
    risAbbreviationInternationalLaw
  )
  addMetadata(metadata, MetaDatumType.REFERENCE_NUMBER, referenceNumber)
  addMetadata(metadata, MetaDatumType.DEFINITION, definition)
  addMetadata(
    metadata,
    MetaDatumType.AGE_OF_MAJORITY_INDICATION,
    ageOfMajorityIndication
  )
  addMetadata(
    metadata,
    MetaDatumType.DIVERGENT_DOCUMENT_NUMBER,
    divergentDocumentNumber
  )
  return {
    ...data,
    metadata: metadata,
  }
}
export function addMetadata(
  metadata: MetaDatum[],
  type: MetaDatumType,
  data?: string[] | null
) {
  data?.forEach((value, index) => {
    metadata.push({
      value: value,
      type: type,
      order: index + 1,
    })
  })
}
export function getMetadatumFromMetadata(
  metadata: MetaDatum[],
  type: MetaDatumType
): string[] {
  return metadata
    ?.filter((metaDatum) => metaDatum.type === type)
    ?.map((metaDatum) => metaDatum.value)
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
    ageOfMajorityIndication: frameData?.ageOfMajorityIndication,
    announcementDate: dateCallback(frameData?.announcementDate),
    applicationScopeArea: stringCallback(frameData?.applicationScopeArea),
    applicationScopeEndDate: dateCallback(frameData?.applicationScopeEndDate),
    applicationScopeStartDate: dateCallback(
      frameData?.applicationScopeStartDate
    ),
    categorizedReference: stringCallback(frameData?.categorizedReference),
    celexNumber: stringCallback(frameData?.celexNumber),
    citationDate: dateCallback(frameData?.citationDate),
    citationYear: stringCallback(frameData?.citationYear),
    completeCitation: stringCallback(frameData?.completeCitation),
    definition: frameData?.definition,
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
    divergentDocumentNumber: frameData?.divergentDocumentNumber,
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
    frameKeywords: frameData?.frameKeywords,
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
    referenceNumber: frameData?.referenceNumber,
    reissueArticle: stringCallback(frameData?.reissueArticle),
    reissueDate: dateCallback(frameData?.reissueDate),
    reissueNote: stringCallback(frameData?.reissueNote),
    reissueReference: stringCallback(frameData?.reissueReference),
    repealArticle: stringCallback(frameData?.repealArticle),
    repealDate: dateCallback(frameData?.repealDate),
    repealNote: stringCallback(frameData?.repealNote),
    repealReferences: stringCallback(frameData?.repealReferences),
    risAbbreviation: stringCallback(frameData?.risAbbreviation),
    risAbbreviationInternationalLaw: frameData?.risAbbreviationInternationalLaw,
    statusDate: dateCallback(frameData?.statusDate),
    statusDescription: stringCallback(frameData?.statusDescription),
    statusNote: stringCallback(frameData?.statusNote),
    statusReference: stringCallback(frameData?.statusReference),
    subjectFna: stringCallback(frameData?.subjectFna),
    subjectGesta: stringCallback(frameData?.subjectGesta),
    subjectPreviousFna: stringCallback(frameData?.subjectPreviousFna),
    text: stringCallback(frameData?.text),
    unofficialAbbreviation: frameData?.unofficialAbbreviation,
    unofficialLongTitle: frameData?.unofficialLongTitle,
    unofficialReference: frameData?.unofficialReference,
    unofficialShortTitle: frameData?.unofficialShortTitle,
    validityRule: frameData?.validityRule,
  }
}
