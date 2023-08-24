import { defineStore } from "pinia"
import { ref } from "vue"
import { Norm } from "@/domain/norm"
import { ServiceResponse } from "@/services/httpClient"
import { editNormFrame, getNormByGuid } from "@/services/norms"

export const useLoadedNormStore = defineStore("loaded-norm", () => {
  const loadedNorm = ref<Norm | undefined>(undefined)

  async function load(guid: string): Promise<void> {
    const response = await getNormByGuid(guid)
    loadedNorm.value = response.data
  }

  async function update(): Promise<ServiceResponse<void>> {
    if (loadedNorm.value) {
      const { metadataSections } = loadedNorm.value
      return editNormFrame(loadedNorm.value.guid, metadataSections ?? {}, {
        eli: loadedNorm.value.eli,
      })
    } else {
      return { status: 404, data: undefined }
    }
  }

  return { loadedNorm, load, update }
})
