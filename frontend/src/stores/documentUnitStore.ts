import { defineStore } from "pinia"
import { ref } from "vue"
import DocumentUnit from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"
import { ServiceResponse } from "@/services/httpClient"

export const useDocumentUnitStore = defineStore("document-unit", () => {
  const documentUnit = ref<DocumentUnit | undefined>(undefined)

  async function loadDocumentUnit(
    documentNumber: string,
  ): Promise<ServiceResponse<void>> {
    const response =
      await documentUnitService.getByDocumentNumber(documentNumber)
    documentUnit.value = response.data
    return response as ServiceResponse<void>
  }

  async function updateDocumentUnit(): Promise<ServiceResponse<void>> {
    if (documentUnit.value) {
      const response = await documentUnitService.update(
        documentUnit.value as DocumentUnit,
      )
      return response as ServiceResponse<void>
    } else {
      return { status: 404, data: undefined }
    }
  }

  async function updatePartial(): Promise<ServiceResponse<void>> {
    const response = await documentUnitService.updatePartial(
      documentUnit.value?.coreData as Partial<DocumentUnit>,
    )
    return response as ServiceResponse<void>
  }

  return { documentUnit, loadDocumentUnit, updateDocumentUnit, updatePartial }
})
