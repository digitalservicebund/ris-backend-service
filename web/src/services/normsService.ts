import dayjs from "dayjs"
import timezone from "dayjs/plugin/timezone"
import utc from "dayjs/plugin/utc"
import httpClient, { ServiceResponse } from "./httpClient"
import { Norm } from "@/domain/Norm"

type NormList = { officialLongTitle: string; guid: string }[]
dayjs.extend(utc)
dayjs.extend(timezone)
type FrameData = {
  officialLongTitle: string
  officialShortTitle?: string
  officialAbbreviation?: string
  referenceNumber?: string
  publicationDate?: string
  announcementDate?: string
  citationDate?: string
  frameKeywords?: string
  providerEntity?: string
  providerDecidingBody?: string
  providerIsResolutionMajority?: boolean
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
  unofficialLongTitle?: string
  unofficialShortTitle?: string
  unofficialAbbreviation?: string
  risAbbreviation?: string
}

function encodeString(data?: string): string | null {
  return data && data.length > 0 ? data : null
}

function encodeBoolean(data?: boolean): boolean | null {
  return data ?? null
}

// Makes the assumption that we currently get a date string in the following
// format: `2022-11-14T23:00:00.000Z`. To comply with the expected date format
// of the API, we only take the first 10 characters.
//
// TODO: Improve by working with enriched date type.
function encodeDate(data?: string): string | null {
  return data && data.length > 0
    ? dayjs(data).tz("Europe/Berlin").format("YYYY-MM-DD")
    : null
}

function encodeFrameData(data: FrameData) {
  return {
    officialLongTitle: encodeString(data.officialLongTitle),
    officialShortTitle: encodeString(data.officialShortTitle),
    officialAbbreviation: encodeString(data.officialAbbreviation),
    referenceNumber: encodeString(data.referenceNumber),
    publicationDate: encodeDate(data.publicationDate),
    announcementDate: encodeDate(data.announcementDate),
    citationDate: encodeDate(data.citationDate),
    frameKeywords: encodeString(data.frameKeywords),
    providerEntity: encodeString(data.providerEntity),
    providerDecidingBody: encodeString(data.providerDecidingBody),
    providerIsResolutionMajority: encodeBoolean(
      data.providerIsResolutionMajority
    ),
    leadJurisdiction: encodeString(data.leadJurisdiction),
    leadUnit: encodeString(data.leadUnit),
    participationType: encodeString(data.participationType),
    participationInstitution: encodeString(data.participationInstitution),
    documentTypeName: encodeString(data.documentTypeName),
    documentNormCategory: encodeString(data.documentNormCategory),
    documentTemplateName: encodeString(data.documentTemplateName),
    subjectFna: encodeString(data.subjectFna),
    subjectPreviousFna: encodeString(data.subjectPreviousFna),
    subjectGesta: encodeString(data.subjectGesta),
    subjectBgb3: encodeString(data.subjectBgb3),
    unofficialLongTitle: encodeString(data.unofficialLongTitle),
    unofficialShortTitle: encodeString(data.unofficialShortTitle),
    unofficialAbbreviation: encodeString(data.unofficialAbbreviation),
    risAbbreviation: encodeString(data.risAbbreviation),
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
