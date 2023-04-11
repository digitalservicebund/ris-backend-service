import {
  decodeNorm,
  encodeMetadataSections,
  encodeFlatMetadata,
} from "./conversions"
import { NormListResponseSchema, NormResponseSchema } from "./schemas"
import { FlatMetadata, MetadataSections, Norm } from "@/domain/Norm"
import httpClient, { ServiceResponse } from "@/services/httpClient"

const errorMessages = {
  FILE_TOO_BIG: {
    title: "Die Datei überschreitet die maximale Dateigröße von 20 MB.",
  },
  NO_INTERNET_CONNECTION: {
    title: "Der Upload ist fehlgeschlagen.",
    description:
      "Bitte prüfen Sie Ihre Internetverbindung und versuchen Sie es erneut.",
  },
  WRONG_FILE_FORMAT: {
    title: "Das Dateiformat wird nicht unterstützt.",
    description: "Bitte verwenden Sie eine Zip-Datei.",
  },
  IMPORT_ERROR: {
    title: "Datei konnte nicht importiert werden.",
  },
  GENERATION_ERROR: {
    title: "Zip-Datei konnte nicht generiert werden.",
  },
  LOADING_ERROR: {
    title: "Dokumentationseinheit konnte nicht geladen werden.",
  },
  EDIT_ERROR: {
    title: "Dokumentationseinheit konnte nicht bearbeitet werden.",
  },
}

export async function getAllNorms(): Promise<
  ServiceResponse<NormListResponseSchema>
> {
  const { data, status, error } = await httpClient.get<{
    data: NormListResponseSchema
  }>("norms")

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
  const { status, data, error } = await httpClient.get<NormResponseSchema>(
    `norms/${guid}`
  )
  if (status >= 300 || error || data == undefined) {
    return {
      status: status,
      error: errorMessages.LOADING_ERROR,
    }
  } else {
    return {
      status,
      data: decodeNorm(data),
    }
  }
}

export async function editNormFrame(
  guid: string,
  metadataSections: MetadataSections,
  flatMetadata: FlatMetadata
): Promise<ServiceResponse<void>> {
  const headers = {
    Accept: "application/json",
    "Content-Type": "application/json",
  }

  const body = {
    ...encodeFlatMetadata(flatMetadata),
    metadataSections: encodeMetadataSections(metadataSections) ?? [],
  }

  const { status, error } = await httpClient.put(
    `norms/${guid}`,
    { headers },
    body
  )

  if (status >= 300 || error) {
    return {
      status: status,
      error: errorMessages.EDIT_ERROR,
    }
  } else {
    return {
      status,
      data: undefined,
    }
  }
}

export async function importNorm(file: File): Promise<ServiceResponse<string>> {
  const isOnLine = navigator.onLine
  if (!isOnLine) {
    return {
      status: 503,
      error: errorMessages.NO_INTERNET_CONNECTION,
    }
  }

  const headers = { "Content-Type": "application/zip", "X-Filename": file.name }

  const { status, error, data } = await httpClient.post<
    unknown,
    { guid: string }
  >("norms", { headers }, file)

  if (status >= 400 || error) {
    if (status === 413) {
      return {
        status,
        error: errorMessages.FILE_TOO_BIG,
      }
    } else {
      return {
        status,
        error: errorMessages.IMPORT_ERROR,
      }
    }
  } else {
    return {
      status,
      data: data?.guid,
    }
  }
}

export function getFileUrl(guid: string, hash: string): string {
  return `/api/v1/norms/${guid}/files/${hash}`
}

export async function triggerFileGeneration(
  guid: string
): Promise<ServiceResponse<string>> {
  const { status, error } = await httpClient.post(`norms/${guid}/files`)
  if (status >= 400 || error) {
    return {
      status,
      error: errorMessages.GENERATION_ERROR,
    }
  } else {
    return {
      status,
      data: "Datei wurde erstellt.",
    }
  }
}
