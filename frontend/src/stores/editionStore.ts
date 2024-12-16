import { UUID } from "crypto"
import { defineStore } from "pinia"
import { ref } from "vue"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import { ServiceResponse } from "@/services/httpClient"
import LegalPeriodicalEditionService from "@/services/legalPeriodicalEditionService"

export const useEditionStore = defineStore("editionStore", () => {
  const edition = ref<LegalPeriodicalEdition | undefined>(undefined)

  async function loadEdition(
    editionId: UUID,
  ): Promise<ServiceResponse<LegalPeriodicalEdition>> {
    const response = await LegalPeriodicalEditionService.get(editionId)

    if (response.data) {
      edition.value = new LegalPeriodicalEdition({
        ...response.data,
      })
    }
    return response
  }

  async function saveEdition(): Promise<
    ServiceResponse<LegalPeriodicalEdition>
  > {
    return await LegalPeriodicalEditionService.save(
      edition.value as LegalPeriodicalEdition,
    )
  }

  return {
    edition,
    loadEdition,
    saveEdition,
  }
})
