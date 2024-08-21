import { UUID } from "crypto"
import { defineStore } from "pinia"
import { ref } from "vue"
import { useRoute } from "vue-router"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import { ServiceResponse } from "@/services/httpClient"
import LegalPeriodicalEditionService from "@/services/legalPeriodicalEditionService"

export const useReferenceStore = defineStore("referencesStore", () => {
  const edition = ref<LegalPeriodicalEdition | undefined>(undefined)
  const route = useRoute()

  async function loadEdition(): Promise<
    ServiceResponse<LegalPeriodicalEdition>
  > {
    const uuid = route.params.uuid
    const response = await LegalPeriodicalEditionService.get(
      uuid.toString() as UUID,
    )

    if (response.data) {
      edition.value = response.data
    }

    return response
  }

  // async function updateEdition(): Promise<
  //   ServiceResponse<LegalPeriodicalEdition>
  // > {
  //   // here comes the code to edit the edition infos
  // }

  return {
    edition,
    loadEdition,
  }
})
