import httpClient, { ServiceResponse } from "./httpClient"
import { Norm } from "@/domain/Norm"

type NormList = { longTitle: string; guid: string }[]

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
  longTitle: string
): Promise<ServiceResponse<void>> {
  const { status, error } = await httpClient.patch<{ longTitle: string }, void>(
    `norms/${guid}`,
    undefined,
    { longTitle }
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
