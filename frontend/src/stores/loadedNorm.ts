import { defineStore } from "pinia"
import { ref, computed, MaybeRefOrGetter, toValue } from "vue"
import { Documentation, isDocumentSection, Norm } from "@/domain/norm"
import { ServiceResponse } from "@/services/httpClient"
import { editNormFrame, getNormByGuid } from "@/services/norms"

function findDocumentationRecursive(
  documentation: Documentation[] | undefined,
  guid: string | undefined,
): Documentation | undefined {
  if (!documentation || !guid) return undefined

  for (const entry of documentation) {
    if (entry.guid === guid) return entry
    if (isDocumentSection(entry)) {
      const nestedDoc = findDocumentationRecursive(entry.documentation, guid)
      if (nestedDoc) return nestedDoc
    }
  }
  return undefined
}

export const useLoadedNormStore = defineStore("loaded-norm", () => {
  const loadedNorm = ref<Norm | undefined>(undefined)

  async function load(guid: string): Promise<void> {
    const response = await getNormByGuid(guid)
    loadedNorm.value = response.data
  }

  function findDocumentation(
    documentationGuid: MaybeRefOrGetter<string | undefined>,
  ) {
    return computed(() =>
      findDocumentationRecursive(
        loadedNorm.value?.documentation,
        toValue(documentationGuid),
      ),
    )
  }

  async function update(): Promise<ServiceResponse<void>> {
    if (loadedNorm.value) {
      const { metadataSections } = loadedNorm.value
      return editNormFrame(loadedNorm.value.guid, metadataSections ?? {})
    } else {
      return { status: 404, data: undefined }
    }
  }

  return { loadedNorm, load, update, findDocumentation }
})
