import dayjs from "dayjs"
import timezone from "dayjs/plugin/timezone"
import utc from "dayjs/plugin/utc"
import { FrameData, NormResponse } from "@/domain/Norm"
import httpClient, { ServiceResponse } from "@/services/httpClient"
import {
  applyToFrameData,
  getNormEditRequestFromFrameData,
  NullableBoolean,
  NullableString,
} from "@/utilities/normUtilities"

type NormList = { officialLongTitle: string; guid: string }[]
dayjs.extend(utc)
dayjs.extend(timezone)

function encodeString(data: NullableString): NullableString {
  return data && data.length > 0 ? data : null
}

function encodeBoolean(data: NullableBoolean): NullableBoolean {
  return data ?? null
}

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

// Makes the assumption that we currently get a date string in the following
// format: `2022-11-14T23:00:00.000Z`. To comply with the expected date format
// of the API, we only take the first 10 characters.
//
// TODO: Improve by working with enriched date type.
function encodeDate(data?: string | null): string | null {
  return data && data.length > 0
    ? dayjs(data).tz("Europe/Berlin").format("YYYY-MM-DD")
    : null
}

function encodeFrameData(data: FrameData) {
  return applyToFrameData(data, encodeString, encodeBoolean, encodeDate)
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
): Promise<ServiceResponse<NormResponse>> {
  const { status, data, error } = await httpClient.get<NormResponse>(
    `norms/${guid}`
  )
  if (status >= 300 || error) {
    return {
      status: status,
      error: errorMessages.LOADING_ERROR,
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
  const body = getNormEditRequestFromFrameData(encodeFrameData(frameData))
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

  const { status, error, data } = await httpClient.post<
    unknown,
    { guid: string }
  >(
    "norms",
    { headers: { "Content-Type": "application/zip", "X-Filename": file.name } },
    file
  )
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
