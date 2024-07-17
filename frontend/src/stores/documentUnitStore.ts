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
    ServiceResponse<RisJsonPatch | FailedValidationServerResponse>
  > {
    if (!documentUnit.value || !originalDocumentUnit.value) {
      return {
        status: 404,
        data: undefined,
        error: errorMessages.DOCUMENT_UNIT_COULD_NOT_BE_LOADED,
      }
    }

    // Create JSON Patch
    const patch = jsonpatch.compare(
      originalDocumentUnit.value,
      documentUnit.value,
    )

    // If there are no updates in the client, get the current version from backend
    if (patch.length === 0 && documentUnit.value.documentNumber) {
      const response = await loadDocumentUnit(documentUnit.value.documentNumber)
      if (response.data) {
        return {
          status: response.status,
          data: {
            documentationUnitVersion: response.data.version,
            patch: [],
            errorPaths: [],
          },
        }
      } else {
        return {
          status: 404,
          data: undefined,
          error: errorMessages.DOCUMENT_UNIT_COULD_NOT_BE_LOADED,
        }
      }
    }

    const response = await documentUnitService.update(documentUnit.value.uuid, {
      documentationUnitVersion: documentUnit.value.version,
      patch,
      errorPaths: [],
    })

    if (response.status === 200) {
      //Apply backend patch to original documentunit reference, with updated version
      const backendPatch = response.data as RisJsonPatch
      jsonpatch.applyPatch(originalDocumentUnit.value, backendPatch.patch)
      originalDocumentUnit.value.version = backendPatch.documentationUnitVersion

      // Deep copy
      documentUnit.value = new DocumentUnit(originalDocumentUnit.value.uuid, {
        ...JSON.parse(JSON.stringify(originalDocumentUnit.value)),
      })

      // Todo: which status?
      // Todo: retrieve error message from errorMessages.json
      if (
        backendPatch.errorPaths != undefined &&
        backendPatch.errorPaths.length > 0
      ) {
        response.error = {
          title: "Fehler beim Patchen",
          description: backendPatch.errorPaths,
        }
      }
    } else {
      return {
        status: response.status,
        data: undefined,
        error: errorMessages.DOCUMENT_UNIT_UPDATE_FAILED,
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
