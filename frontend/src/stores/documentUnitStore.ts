import * as jsonpatch from "fast-json-patch"
import { Operation } from "fast-json-patch"
import { defineStore } from "pinia"
import { ref } from "vue"
import fields from "@/data/fieldNames.json"
import { Decision } from "@/domain/decision"
import { DocumentationUnit } from "@/domain/documentationUnit"
import PendingProceeding from "@/domain/pendingProceeding"
import { RisJsonPatch } from "@/domain/risJsonPatch"
import errorMessages from "@/i18n/errors.json"
import documentUnitService from "@/services/documentUnitService"
import {
  FailedValidationServerResponse,
  ServiceResponse,
} from "@/services/httpClient"
import { Match } from "@/types/textCheck"
import { isDecision, isPendingProceeding } from "@/utils/typeGuards"

export const useDocumentUnitStore = defineStore("docunitStore", () => {
  const documentUnit = ref<DocumentationUnit | undefined>(undefined)
  const originalDocumentUnit = ref<DocumentationUnit | undefined>(undefined)
  const matches = ref<Map<string, Match[]>>(new Map())

  async function loadDocumentUnit(
    documentNumber: string,
  ): Promise<ServiceResponse<DocumentationUnit>> {
    matches.value.clear()

    const response =
      await documentUnitService.getByDocumentNumber(documentNumber)
    if (response.data) {
      documentUnit.value = response.data
      originalDocumentUnit.value = JSON.parse(JSON.stringify(response.data)) // Deep copy for tracking changes
    } else {
      documentUnit.value = undefined
    }
    return response
  }

  async function unloadDocumentUnit(): Promise<void> {
    documentUnit.value = undefined
    matches.value.clear()
  }

  // prettier-ignore
  async function updateDocumentUnit(): Promise< // NOSONAR: needs definitely refactoring
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
        const frontendPatch = sanitizePatch(
          jsonpatch.compare(
            originalDocumentUnit.value,
            documentUnit.value,
          ),
        )

        const response = await documentUnitService.update(documentUnit.value.uuid, {
            documentationUnitVersion: documentUnit.value.version,
            patch: frontendPatch,
            errorPaths: [],
        })

        if (response.status === 200) {
            //Apply backend patch to original documentunit reference, with updated version
            const backendPatch = response.data as RisJsonPatch

            // Here, the patch from the frontend was successfully applied in the backend database.
            try {
                // We apply the changes from the backend response to our local docUnit
                documentUnit.value = getPatchApplyResult(
                    instantiateDocUnitClass(documentUnit.value as DocumentationUnit),
                    backendPatch.patch,
                )
                // We apply the local changes that were successfully saved in the backend on our docUnit backend representation
                originalDocumentUnit.value = getPatchApplyResult(
                    instantiateDocUnitClass(originalDocumentUnit.value as DocumentationUnit),
                    frontendPatch,
                )
                // We apply the backend response changes to our backend docUnit representation.
                originalDocumentUnit.value = getPatchApplyResult(
                    instantiateDocUnitClass(originalDocumentUnit.value as DocumentationUnit),
                    backendPatch.patch,
                )

            } catch {
                if (documentUnit.value.documentNumber) {
                    const response = await loadDocumentUnit(
                        documentUnit.value.documentNumber,
                    )
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
            }
            documentUnit.value.version = backendPatch.documentationUnitVersion

            if (
                backendPatch.errorPaths != undefined &&
                backendPatch.errorPaths.length > 0
            ) {
                // ^\/ matches a leading slash.
                // \/$ matches a trailing slash.
                // \/\d+$ matches a trailing slash followed by one or more digits at the end of the string, to collect list items.
                const path = backendPatch.errorPaths[0].replace(/(^\/|\/$|\/\d+$)/g, "")

                const mappedValue = fields[path as keyof typeof fields]

                return {
                    status: 207,
                    data: undefined,
                    error: {
                        title: mappedValue,
                    },
                }
            }
        } else {
            return {
                status: response.status,
                data: undefined,
                error:
                    response.status === 403
                        ? errorMessages.NOT_ALLOWED
                        : errorMessages.DOCUMENT_UNIT_UPDATE_FAILED,
            }
        }
        return response
    }

  function instantiateDocUnitClass(docUnit?: DocumentationUnit) {
    if (isDecision(docUnit)) {
      return new Decision(docUnit.uuid, {
        ...JSON.parse(JSON.stringify(docUnit)),
      })
    } else if (isPendingProceeding(docUnit)) {
      return new PendingProceeding(docUnit.uuid, {
        ...JSON.parse(JSON.stringify(docUnit)),
      })
    } else {
      throw Error("Unsupported doc type: " + docUnit)
    }
  }

  function sanitizePatch(operations: Operation[]): Operation[] {
    return operations.filter((op) => !op.path.split("/").includes("localId"))
  }

  function getPatchApplyResult(
    docUnit: DocumentationUnit,
    backendPatch: Operation[],
  ) {
    if (!documentUnit.value?.uuid) {
      throw new Error("Can't apply patch on an empty uuid")
    }
    jsonpatch.applyPatch(docUnit, backendPatch)
    return instantiateDocUnitClass(docUnit)
  }

  return {
    documentUnit,
    originalDocumentUnit,
    loadDocumentUnit,
    unloadDocumentUnit,
    updateDocumentUnit,
    matches,
  }
})
