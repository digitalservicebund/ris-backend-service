import * as jsonpatch from "fast-json-patch"
import { defineStore } from "pinia"
import { ref } from "vue"
import DocumentUnit from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"
import { ServiceResponse } from "@/services/httpClient"

export const useDocumentUnitStore = defineStore("document-unit", () => {
  const documentUnit = ref<DocumentUnit | undefined>(undefined)
  const originalDocumentUnit = ref<DocumentUnit | undefined>(undefined)

  async function loadDocumentUnit(
    documentNumber: string,
  ): Promise<ServiceResponse<void>> {
    const response =
      await documentUnitService.getByDocumentNumber(documentNumber)
    documentUnit.value = response.data
    originalDocumentUnit.value = JSON.parse(JSON.stringify(response.data)) // Deep copy for tracking changes
    return response as ServiceResponse<void>
  }

  async function updateDocumentUnit(): Promise<ServiceResponse<void>> {
    if (!documentUnit.value || !originalDocumentUnit.value) {
      return { status: 404, data: undefined }
    }

    // Generate the JSON Patch document
    const patch = jsonpatch.compare(
      originalDocumentUnit.value,
      documentUnit.value,
    )

    // console.log("patch", patch)

    if (patch.length === 0) {
      return { status: 304, data: undefined } // No changes to update
    }

    const response = await documentUnitService.updatePartial(
      documentUnit.value.uuid,
      patch,
    )

    if (response.status === 200) {
      originalDocumentUnit.value = JSON.parse(
        JSON.stringify(documentUnit.value),
      ) // Update the original copy
    }

    return response as ServiceResponse<void>
  }

  return { documentUnit, loadDocumentUnit, updateDocumentUnit }
})
