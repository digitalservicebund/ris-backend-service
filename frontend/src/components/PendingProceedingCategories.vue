<script setup lang="ts">
import { storeToRefs } from "pinia"
import type { Component } from "vue"
import { computed, ref, toRefs, watch } from "vue"
import { useRoute } from "vue-router"
import DocumentUnitCoreData from "@/components/DocumentUnitCoreData.vue"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import FlexItem from "@/components/FlexItem.vue"

import PendingProceedingTexts from "@/components/texts/PendingProceedingTexts.vue"
import { useProvideCourtType } from "@/composables/useCourtType"
import { useInternalUser } from "@/composables/useInternalUser"
import { useScroll } from "@/composables/useScroll"
import { usePendingProceedingStore } from "@/stores/pendingProceedingStore"

defineProps<{
  registerTextEditorRef: (key: string, el: Component | null) => void
}>()

const route = useRoute()
const { hash: routeHash } = toRefs(route)
const store = usePendingProceedingStore()
const { pendingProceeding } = storeToRefs(store)
const courtTypeRef = ref<string>(
  pendingProceeding.value!.coreData.court?.type ?? "",
)
const { scrollIntoViewportById } = useScroll()

const coreData = computed({
  get: () => store.pendingProceeding!.coreData,
  set: async (newValues) => {
    store.pendingProceeding!.coreData = newValues
    courtTypeRef.value = store.pendingProceeding!.coreData.court?.type ?? ""
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
      is-pending-proceeding
    />
    <!--&lt;!&ndash;    <PendingProceedingProceedingDecisions-->
    <!--      :id="DocumentUnitCategoriesEnum.PROCEEDINGS_DECISIONS"-->
    <!--    />&ndash;&gt;-->
    <!--&lt;!&ndash;    <DocumentUnitContentRelatedIndexing-->
    <!--      :id="DocumentUnitCategoriesEnum.CONTENT_RELATED_INDEXING"-->
    <!--    />&ndash;&gt;-->
    <PendingProceedingTexts
      v-bind="{ registerTextEditorRef }"
      :id="DocumentUnitCategoriesEnum.TEXTS"
      :text-editor-refs="registerTextEditorRef"
    />
  </FlexItem>
</template>
