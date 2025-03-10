<script setup lang="ts">
import { storeToRefs } from "pinia"
import type { Component } from "vue"
import { computed, ref, toRefs, watch } from "vue"
import { useRoute } from "vue-router"
import DocumentUnitContentRelatedIndexing from "@/components/DocumentUnitContentRelatedIndexing.vue"
import DocumentUnitCoreData from "@/components/DocumentUnitCoreData.vue"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import FlexItem from "@/components/FlexItem.vue"
import ProceedingDecisions from "@/components/ProceedingDecisions.vue"
import DocumentUnitTexts from "@/components/texts/DocumentUnitTexts.vue"

import { useProvideCourtType } from "@/composables/useCourtType"
import { useInternalUser } from "@/composables/useInternalUser"
import { useScroll } from "@/composables/useScroll"
import constitutionalCourtTypes from "@/data/constitutionalCourtTypes.json"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

defineProps<{
  registerTextEditorRef: (key: string, el: Component | null) => void
}>()

const route = useRoute()
const { hash: routeHash } = toRefs(route)
const store = useDocumentUnitStore()
const { documentUnit } = storeToRefs(store)
const courtTypeRef = ref<string>(documentUnit.value!.coreData.court?.type ?? "")
const { scrollIntoViewportById } = useScroll()

/**
 * Determines whether legal forces should be deleted based on the court type and presence of a selected court.
 * @returns boolean
 */
const shouldDeleteLegalForces = computed(() => {
  return (
    !constitutionalCourtTypes.items.includes(courtTypeRef.value) ||
    !documentUnit.value!.coreData.court
  )
})

/**
 * Deletes the legal forces from all single norms in the norms of the updated document unit.
 */
function deleteLegalForces() {
  const norms = documentUnit.value!.contentRelatedIndexing.norms

  norms?.forEach((norm) => {
    norm.singleNorms = norm.singleNorms?.filter((singleNorm) => {
      if (singleNorm.legalForce) {
        singleNorm.legalForce = undefined
      }
      return !singleNorm.isEmpty
    })
  })
}

const coreData = computed({
  get: () => store.documentUnit!.coreData,
  set: async (newValues) => {
    store.documentUnit!.coreData = newValues
    courtTypeRef.value = store.documentUnit!.coreData.court?.type ?? ""
    // When the user changes the court to one that doesn't allow "Gesetzeskraft" all existing legal forces are deleted
    if (shouldDeleteLegalForces.value) {
      deleteLegalForces()
    }
  },
})

watch(
  routeHash,
  async () => {
    await scrollIntoViewportById(routeHash.value.replace(/^#/, ""))
  },
  { immediate: true },
)

useProvideCourtType(courtTypeRef)

const isInternalUser = useInternalUser()
</script>

<template>
  <FlexItem class="w-full flex-1 grow flex-col gap-24 p-24">
    <DocumentUnitCoreData
      v-if="isInternalUser"
      :id="DocumentUnitCategoriesEnum.CORE_DATA"
      v-model="coreData"
    />
    <ProceedingDecisions
      :id="DocumentUnitCategoriesEnum.PROCEEDINGS_DECISIONS"
    />
    <DocumentUnitContentRelatedIndexing
      :id="DocumentUnitCategoriesEnum.CONTENT_RELATED_INDEXING"
    />
    <DocumentUnitTexts
      v-bind="{ registerTextEditorRef }"
      :id="DocumentUnitCategoriesEnum.TEXTS"
      :text-editor-refs="registerTextEditorRef"
    />
  </FlexItem>
</template>
