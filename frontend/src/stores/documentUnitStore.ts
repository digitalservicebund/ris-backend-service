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

  async function updateDocumentUnit(): Promise<
    ServiceResponse<
      RisJsonPatch | FailedValidationServerResponse | DocumentUnit | undefined
    >
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

    if (patch.length === 0 && documentUnit.value.documentNumber) {
      // Even though there are no updates in the client, get the current version from backend
      const docUnitFromBackend = await loadDocumentUnit(
        documentUnit.value.documentNumber,
      )
      return docUnitFromBackend
    }

    const response = await documentUnitService.update(documentUnit.value.uuid, {
      documentationUnitVersion: documentUnit.value.version,
      patch,
      errorPaths: [],
    })

    if (response.status === 200) {
      const newPatch = response.data as RisJsonPatch
      jsonpatch.applyPatch(originalDocumentUnit.value, newPatch.patch)

      const parsedDocumentUnit = JSON.parse(
        JSON.stringify(originalDocumentUnit.value),
      )
      documentUnit.value = new DocumentUnit(originalDocumentUnit.value.uuid, {
        ...parsedDocumentUnit,
      })

      if (newPatch.errorPaths != undefined && newPatch.errorPaths.length > 0) {
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
  }
})
