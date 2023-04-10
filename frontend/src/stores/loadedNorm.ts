import { defineStore } from "pinia"
import { ref } from "vue"
import { Norm } from "@/domain/Norm"
import { ServiceResponse } from "@/services/httpClient"
import { editNormFrame, getNormByGuid } from "@/services/norms"
import {
  getFrameDataFromNorm,
  getNormFromNormResponse,
} from "@/utilities/normUtilities"

export const useLoadedNormStore = defineStore("loaded-norm", () => {
  const loadedNorm = ref<Norm | undefined>(undefined)

  async function load(guid: string): Promise<void> {
    const response = await getNormByGuid(guid)
    loadedNorm.value =
      response.data != undefined
        ? getNormFromNormResponse(response.data)
        : undefined
  }

  async function update(): Promise<ServiceResponse<void>> {
    if (loadedNorm.value) {
      return editNormFrame(
        loadedNorm.value.guid,
        getFrameDataFromNorm(loadedNorm.value)
      )
    }

    return { status: 404, data: undefined }
  }

  return { loadedNorm, load, update }
})
