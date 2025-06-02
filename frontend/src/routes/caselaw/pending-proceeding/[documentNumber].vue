<script lang="ts" setup>
import { computed } from "vue"
import { LocationQuery, useRoute } from "vue-router"
import DocumentInfoPanel from "@/components/DocumentInfoPanel.vue"
import DocumentViewer from "@/components/DocumentViewer.vue"
import { usePendingProceedingMenuItems } from "@/composables/usePendingProceedingMenuItems"
import PendingProceeding from "@/domain/pendingProceeding"
import { usePendingProceedingStore } from "@/stores/pendingProceedingStore"

const props = defineProps<{
  documentNumber: string
}>()

const store = usePendingProceedingStore()
const route = useRoute()

const loadPendingProceeding = async (documentNumber: string) => {
  return await store.loadPendingProceeding(documentNumber)
}

const getHeading = (doc: PendingProceeding) => doc.documentNumber || ""

const getMenuItems = (documentNumber: string, query: LocationQuery) => {
  return usePendingProceedingMenuItems(documentNumber, query)
}

const hasPendingDuplicateWarning = computed(
  () => false,
  // Todo
  //  store.pendingProceeding &&
  //  (store.pendingProceeding.managementData.duplicateRelations ?? []).some(
  //      (warning) => warning.status === "PENDING",
  //  ),
)

const managementDataRoute = computed(() => {
  if (!store.pendingProceeding?.documentNumber) return undefined
  return {
    name: "caselaw-documentUnit-documentNumber-managementdata",
    params: { documentNumber: store.pendingProceeding.documentNumber },
  }
})

const isRouteWithSaveButton = computed(
  () =>
    route.path.includes("categories") ||
    route.path.includes("attachments") ||
    route.path.includes("references") ||
    route.path.includes("managementdata"),
)

const handleDocumentUnitSave = async () => {
  // Todo
}
</script>

<template>
  <DocumentViewer
    :document-number="props.documentNumber"
    :get-document-heading="getHeading"
    :get-menu-items="getMenuItems"
    :load-document="loadPendingProceeding"
  >
    <template
      #main-content="{
        registerTextEditorRef,
        attachmentIndexDeleted,
        attachmentIndexSelected,
        attachmentsUploaded,
        document,
      }"
    >
      <router-view
        v-bind="{ registerTextEditorRef, document }"
        @attachment-index-deleted="attachmentIndexDeleted"
        @attachment-index-selected="attachmentIndexSelected"
        @attachments-uploaded="attachmentsUploaded"
      >
      </router-view>
    </template>

    <template #info-panel="{ document }">
      <DocumentInfoPanel
        v-if="document && !route.path.includes('preview')"
        :document="document as PendingProceeding"
        :duplicate-management-route="managementDataRoute"
        :has-pending-duplicate-warning="hasPendingDuplicateWarning"
        :heading="getHeading(document as PendingProceeding)"
        :on-save="handleDocumentUnitSave"
        :show-save-button="isRouteWithSaveButton"
      />
    </template>
  </DocumentViewer>
</template>
