import * as jsonpatch from "fast-json-patch"
import { defineStore } from "pinia"
import { ref } from "vue"
import DocumentUnit from "@/domain/documentUnit"
import { RisJsonPatch } from "@/domain/risJsonPatch"
import errorMessages from "@/i18n/errors.json"

import documentUnitService from "@/services/documentUnitService"
import {
  FailedValidationServerResponse,
  ServiceResponse,
} from "@/services/httpClient"

export const useDocumentUnitStore = defineStore("docunitStore", () => {
  const documentUnit = ref<DocumentUnit | undefined>(undefined)
  const originalDocumentUnit = ref<DocumentUnit | undefined>(undefined)

  async function loadDocumentUnit(
    documentNumber: string,
  ): Promise<ServiceResponse<DocumentUnit>> {
    const response =
      await documentUnitService.getByDocumentNumber(documentNumber)
    if (response.data) {
      documentUnit.value = response.data
      originalDocumentUnit.value = JSON.parse(JSON.stringify(response.data)) // Deep copy for tracking changes
    } else {
      documentUnit.value = undefined
    }
    return response as ServiceResponse<DocumentUnit>
  }

  async function reloadDocumentUnit(): Promise<
    ServiceResponse<DocumentUnit | undefined>
  > {
    if (documentUnit.value && documentUnit.value.documentNumber) {
      return loadDocumentUnit(documentUnit.value.documentNumber)
    }
    return Promise.reject("Could not load empty document unit")
  }

  async function updateDocumentUnit(): Promise<
    ServiceResponse<RisJsonPatch | FailedValidationServerResponse | undefined>
  > {
    if (!documentUnit.value || !originalDocumentUnit.value) {
      return {
        status: 404,
        data: undefined,
        error: errorMessages.DOCUMENT_UNIT_COULD_NOT_BE_LOADED,
      }
    }

    // Generate the JSON Patch document
    const patch = jsonpatch.compare(
      originalDocumentUnit.value,
      documentUnit.value,
    )

    if (patch.length === 0) {
      const response = await reloadDocumentUnit()
      return { status: response.status, data: undefined } //  No changes to update
    }
    console.log("not skipping update document unit")

    const response = await documentUnitService.update(documentUnit.value.uuid, {
      documentationUnitVersion: documentUnit.value.version,
      patch,
      errorPaths: [],
    })

    if (response.status === 200) {
      const newPatch = response.data as RisJsonPatch
      jsonpatch.applyPatch(originalDocumentUnit.value, newPatch.patch)

      documentUnit.value = JSON.parse(
        JSON.stringify(originalDocumentUnit.value),
      ) // Update the original copy

      if (newPatch.errorPaths.length > 0) {
        response.error = {
          title: "Fehler beim Patchen",
          description: newPatch.errorPaths,
        }
      }
    }

    return response
  }

  return {
    documentUnit,
    loadDocumentUnit,
    updateDocumentUnit,
    reloadDocumentUnit,
  }
})
