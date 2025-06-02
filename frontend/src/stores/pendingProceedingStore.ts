// stores/usePendingProceedingStore.ts
import { defineStore } from "pinia"
import { ref } from "vue"
import PendingProceeding from "@/domain/pendingProceeding"
import documentUnitService from "@/services/documentUnitService"
import { ServiceResponse } from "@/services/httpClient"

export const usePendingProceedingStore = defineStore(
  "pendingProceedingStore",
  () => {
    const pendingProceeding = ref<PendingProceeding | undefined>(undefined)

    async function loadPendingProceeding(
      documentNumber: string,
    ): Promise<ServiceResponse<PendingProceeding>> {
      const response =
        await documentUnitService.getPendingProceedingByDocumentNumber(
          documentNumber,
        )
      if (response.data) {
        pendingProceeding.value = response.data
      } else {
        pendingProceeding.value = undefined
      }
      return response
    }

    return {
      pendingProceeding,
      loadPendingProceeding,
    }
  },
)
