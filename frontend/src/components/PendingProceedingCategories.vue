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
import PendingProceedingTexts from "@/components/texts/PendingProceedingTexts.vue"
import { useInternalUser } from "@/composables/useInternalUser"
import { useScroll } from "@/composables/useScroll"
import { Kind } from "@/domain/documentUnit"
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

const coreData = computed({
  get: () => store.documentUnit!.coreData,
  set: async (newValues) => {
    store.documentUnit!.coreData = newValues
    courtTypeRef.value = store.documentUnit!.coreData.court?.type ?? ""
  },
})

watch(
  routeHash,
  async () => {
    await scrollIntoViewportById(routeHash.value.replace(/^#/, ""))
  },
  { immediate: true },
)

const isInternalUser = useInternalUser()
</script>

<template>
  <FlexItem class="w-full flex-1 grow flex-col gap-24 p-24">
    <DocumentUnitCoreData
      v-if="isInternalUser"
      :id="DocumentUnitCategoriesEnum.CORE_DATA"
      v-model="coreData"
      :kind="Kind.PENDING_PROCEEDING"
    />
    <ProceedingDecisions
      :id="DocumentUnitCategoriesEnum.PROCEEDINGS_DECISIONS"
    />
    <DocumentUnitContentRelatedIndexing
      :id="DocumentUnitCategoriesEnum.CONTENT_RELATED_INDEXING"
    />
    <PendingProceedingTexts
      v-bind="{ registerTextEditorRef }"
      :id="DocumentUnitCategoriesEnum.TEXTS"
      :text-editor-refs="registerTextEditorRef"
    />
  </FlexItem>
</template>
