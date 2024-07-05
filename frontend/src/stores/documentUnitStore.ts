import * as jsonpatch from "fast-json-patch"
import { defineStore } from "pinia"
import { ref } from "vue"
import DocumentUnit from "@/domain/documentUnit"
import { RisJsonPatch } from "@/domain/risJsonPatch"
import documentUnitService from "@/services/documentUnitService"
import { ServiceResponse } from "@/services/httpClient"

export const useDocumentUnitStore = defineStore("docunitStore", () => {
  const documentUnit = ref<DocumentUnit | undefined>(undefined)
  const originalDocumentUnit = ref<DocumentUnit | undefined>(undefined)

  async function loadDocumentUnit(documentNumber: string) {
    const response =
      await documentUnitService.getByDocumentNumber(documentNumber)
    documentUnit.value = response.data
    originalDocumentUnit.value = JSON.parse(JSON.stringify(response.data)) // Deep copy for tracking changes
    return response as ServiceResponse<void>
  }

  async function updateDocumentUnit(): Promise<ServiceResponse<unknown>> {
    if (!documentUnit.value || !originalDocumentUnit.value) {
      return { status: 404, data: undefined }
    }

    // Generate the JSON Patch document
    const patch = jsonpatch.compare(
      originalDocumentUnit.value,
      documentUnit.value,
    )

    if (patch.length === 0) {
      return { status: 304, data: undefined } // No changes to update
    }

    const response = await documentUnitService.update(documentUnit.value.uuid, {
      documentationUnitVersion: documentUnit.value.version,
      patch,
      errorPaths: [],
    })

    if (response.status === 200) {
      const newPatch = response.data as RisJsonPatch
      jsonpatch.applyPatch(documentUnit.value, newPatch.patch)
      documentUnit.value.version = newPatch.documentationUnitVersion
      response.error = {
        title: "Fehler beim Patchen",
        description: newPatch.errorPaths,
      }
      originalDocumentUnit.value = JSON.parse(
        JSON.stringify(documentUnit.value),
      ) // Update the original copy
    }

    return response as ServiceResponse<unknown>
  }

  return { documentUnit, loadDocumentUnit, updateDocumentUnit }
})
