import httpClient, { ServiceResponse } from "./httpClient"
import { Norm } from "@/domain/Norm"

type NormList = { longTitle: string; guid: string }[]

type FrameData = {
  longTitle: string
  officialShortTitle?: string
  officialAbbreviation?: string
  referenceNumber?: string
  publicationDate?: string
  announcementDate?: string
  citationDate?: string
  frameKeywords?: string
  authorEntity?: string
  authorDecidingBody?: string
  authorIsResolutionMajority?: boolean
  leadJurisdiction?: string
  leadUnit?: string
  participationType?: string
  participationInstitution?: string
  documentTypeName?: string
  documentNormCategory?: string
  documentTemplateName?: string
  subjectFna?: string
  subjectPreviousFna?: string
  subjectGesta?: string
  subjectBgb3?: string
  unofficialTitle?: string
  unofficialShortTitle?: string
  unofficialAbbreviation?: string
  risAbbreviation?: string
}

function noBlankStringOrNull(data?: string): string | null {
  if (data === undefined || data.length == 0) {
    return null
  } else {
    return data
  }
}

function encodeFrameData(data: FrameData) {
  return {
    longTitle: noBlankStringOrNull(data.longTitle),
    officialShortTitle: noBlankStringOrNull(data.officialShortTitle),
    officialAbbreviation: noBlankStringOrNull(data.officialAbbreviation),
    referenceNumber: noBlankStringOrNull(data.referenceNumber),
    publicationDate: noBlankStringOrNull(data.publicationDate),
    announcementDate: noBlankStringOrNull(data.announcementDate),
    citationDate: noBlankStringOrNull(data.citationDate),
    frameKeywords: noBlankStringOrNull(data.frameKeywords),
    authorEntity: noBlankStringOrNull(data.authorEntity),
    authorDecidingBody: noBlankStringOrNull(data.authorDecidingBody),
    authorIsResolutionMajority: data.authorIsResolutionMajority ?? null,
    leadJurisdiction: noBlankStringOrNull(data.leadJurisdiction),
    leadUnit: noBlankStringOrNull(data.leadUnit),
    participationType: noBlankStringOrNull(data.participationType),
    participationInstitution: noBlankStringOrNull(
      data.participationInstitution
    ),
    documentTypeName: noBlankStringOrNull(data.documentTypeName),
    documentNormCategory: noBlankStringOrNull(data.documentNormCategory),
    documentTemplateName: noBlankStringOrNull(data.documentTemplateName),
    subjectFna: noBlankStringOrNull(data.subjectFna),
    subjectPreviousFna: noBlankStringOrNull(data.subjectPreviousFna),
    subjectGesta: noBlankStringOrNull(data.subjectGesta),
    subjectBgb3: noBlankStringOrNull(data.subjectBgb3),
    unofficialTitle: noBlankStringOrNull(data.unofficialTitle),
    unofficialShortTitle: noBlankStringOrNull(data.unofficialShortTitle),
    unofficialAbbreviation: noBlankStringOrNull(data.unofficialAbbreviation),
    risAbbreviation: noBlankStringOrNull(data.risAbbreviation),
  }
}

export async function getAllNorms(): Promise<ServiceResponse<NormList>> {
  const { data, status, error } = await httpClient.get<{ data: NormList }>(
    "norms"
  )

  if (status >= 300 || error) {
    return {
      status: status,
      error: {
        title: "Dokumentationseinheiten konnten nicht geladen werden.",
      },
    }
  } else {
    return {
      status: status,
      data: data.data,
    }
  }
}

export async function getNormByGuid(
  guid: string
): Promise<ServiceResponse<Norm>> {
  const { status, data, error } = await httpClient.get<Norm>(`norms/${guid}`)
  if (status >= 300 || error) {
    return {
      status: status,
      error: {
        title: "Dokumentationseinheit konnte nicht geladen werden.",
      },
    }
  } else {
    return {
      status,
      data,
    }
  }
}

export async function editNormFrame(
  guid: string,
  frameData: FrameData
): Promise<ServiceResponse<void>> {
  const body = encodeFrameData(frameData)
  const { status, error } = await httpClient.put(
    `norms/${guid}`,
    {
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
    },
    body
  )

  if (status >= 300 || error) {
    return {
      status: status,
      error: {
        title: "Dokumentationseinheit konnte nicht bearbeitet werden.",
      },
    }
  } else {
    return {
      status,
      data: undefined,
    }
  }
}
