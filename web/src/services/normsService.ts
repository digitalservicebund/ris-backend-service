import httpClient, { ServiceResponse } from "./httpClient"
import { Norm } from "@/domain/Norm"

type NormList = { longTitle: string; guid: string }[]

type frameDataType = {
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
}

function dateStringOrNull(dateString?: string): string | null {
  if (dateString === undefined || dateString.length == 0) {
    return null
  } else {
    return dateString
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
  frameData: frameDataType
): Promise<ServiceResponse<void>> {
  frameData.publicationDate = dateStringOrNull(frameData.publicationDate)
  frameData.announcementDate = dateStringOrNull(frameData.announcementDate)
  frameData.citationDate = dateStringOrNull(frameData.citationDate)
  const { status, error } = await httpClient.put(
    `norms/${guid}`,
    {
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
    },
    frameData
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
