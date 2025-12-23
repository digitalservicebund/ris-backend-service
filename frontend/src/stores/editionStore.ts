import { UUID } from "crypto"
import { defineStore } from "pinia"
import { ref } from "vue"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import Reference from "@/domain/reference"
import { ServiceResponse } from "@/services/httpClient"
import LegalPeriodicalEditionService from "@/services/legalPeriodicalEditionService"

export const useEditionStore = defineStore("editionStore", () => {
  const edition = ref<LegalPeriodicalEdition | undefined>(undefined)

  // Hilfsfunktion, um Backend-Daten in Klassen-Instanzen umzuwandeln
  // und dabei vorhandene localIds zu erhalten
  function updateEditionFromResponse(data: LegalPeriodicalEdition) {
    // Wir merken uns die aktuellen localIds (falls vorhanden)
    const localIdMap = new Map<string, string>()
    edition.value?.references?.forEach((ref) => {
      if (ref.id) localIdMap.set(ref.id, ref.localId)
    })
    const newEdition = new LegalPeriodicalEdition({
      ...data,
      references: data.references?.map((refData) => {
        return {
          ...refData,
          localId: refData.id ? localIdMap.get(refData.id) : undefined,
        } as Reference
      }),
    })

    edition.value = newEdition
  }

  async function loadEdition(
    editionId: UUID,
  ): Promise<ServiceResponse<LegalPeriodicalEdition>> {
    const response = await LegalPeriodicalEditionService.get(editionId)

    if (response.data) {
      updateEditionFromResponse(response.data)
    }
    return response
  }

  async function saveEdition(): Promise<
    ServiceResponse<LegalPeriodicalEdition>
  > {
    const response = await LegalPeriodicalEditionService.save(
      edition.value as LegalPeriodicalEdition,
    )

    if (response.data) {
      updateEditionFromResponse(response.data)
    }

    return response
  }

  return {
    edition,
    loadEdition,
    saveEdition,
  }
})
