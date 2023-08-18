import * as Sentry from "@sentry/vue"
import {
  decodeNorm,
  encodeMetadataSections,
  encodeFlatMetadata,
} from "./conversions"
import { NormListResponseSchema, NormResponseSchema } from "./schemas"
import { FlatMetadata, MetadataSections, Norm } from "@/domain/Norm"
import httpClient, { ServiceResponse } from "@/services/httpClient"
import { ValidationError } from "@/shared/components/input/types"
import errorMessages from "@/shared/i18n/errors.json"

export async function getAllNorms(): Promise<
  ServiceResponse<NormListResponseSchema>
> {
  const { data, status, error } = await httpClient.get<{
    data: NormListResponseSchema
  }>("norms", { params: { eGesetzgebung: "false" } })

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
  guid: string,
): Promise<ServiceResponse<Norm>> {
  const { status, data, error } = await httpClient.get<NormResponseSchema>(
    `norms/${guid}`,
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
  flatMetadata: FlatMetadata,
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
    body,
  )

  if (status >= 300 || error) {
    Sentry.captureException(error, {
      tags: {
        type: "save_failed",
      },
    })
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
  guid: string,
): Promise<ServiceResponse<string>> {
  const { status, error } = await httpClient.post(`norms/${guid}/files`)
  if (status >= 400 || error) {
    Sentry.captureException(error, {
      tags: {
        type: "zip_creation_failed",
      },
    })
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

export async function validateNormFrame(
  metadataSections?: MetadataSections,
): Promise<ServiceResponse<ValidationError[]>> {
  const headers = {
    Accept: "application/json",
    "Content-Type": "application/json",
  }

  const body = {
    metadataSections: encodeMetadataSections(metadataSections ?? {}) ?? [],
  }

  try {
    const { status, error, data } = await httpClient.post<
      unknown,
      ValidationError[]
    >("norms/norm/validation", { headers }, body)

    if (status === 200) {
      return {
        status,
        data: data ?? [],
      }
    } else {
      return {
        status,
        error: error ?? errorMessages.NORM_COULD_NOT_BE_VALIDATED,
      }
    }
  } catch (error) {
    return {
      status: 500,
      error: errorMessages.SERVER_ERROR,
    }
  }
}
