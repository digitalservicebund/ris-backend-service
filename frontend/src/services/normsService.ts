import dayjs from "dayjs"
import timezone from "dayjs/plugin/timezone"
import utc from "dayjs/plugin/utc"
import httpClient, { ServiceResponse } from "./httpClient"
import { FrameData, Norm } from "@/domain/Norm"
import {
  applyToFrameData,
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

export async function importNorm(file: File): Promise<ServiceResponse<string>> {
  const { status, error, data } = await httpClient.post<unknown, string>(
    "norms",
    { headers: { "Content-Type": "application/zip" } },
    file
  )

  if (status >= 400 || error) {
    return {
      status,
      error: {
        title: "Datei konnte nicht importiert werden.",
      },
    }
  } else {
    return {
      status,
      data,
    }
  }
}
