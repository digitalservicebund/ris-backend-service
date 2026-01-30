<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { Ref } from "vue"
import DocumentViewer from "@/components/DocumentViewer.vue"
import { Decision } from "@/domain/decision"
import { Kind } from "@/domain/documentationUnitKind"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { useExtraContentSidePanelStore } from "@/stores/extraContentSidePanelStore"

defineProps<{
  documentNumber: string
}>()

const extraContentSidePanelStore = useExtraContentSidePanelStore()
const documentUnitStore = useDocumentUnitStore()

const { documentUnit } = storeToRefs(documentUnitStore) as {
  documentUnit: Ref<Decision | undefined>
}

async function attachmentIndexSelected(index: number) {
  extraContentSidePanelStore.togglePanel(true)
  extraContentSidePanelStore.selectAttachments(index)
}

async function attachmentIndexDeleted(
  index: number,
  requestDocumentUnitFromServer: () => Promise<void>,
) {
  await requestDocumentUnitFromServer()
  extraContentSidePanelStore.onAttachmentDeleted(
    index,
    documentUnit.value
      ? documentUnit.value.originalDocumentAttachments.length
      : 0,
  )
}

async function attachmentsUploaded(
  anySuccessful: boolean,
  requestDocumentUnitFromServer: () => Promise<void>,
) {
  if (anySuccessful) {
    await requestDocumentUnitFromServer()
    extraContentSidePanelStore.togglePanel(true)
    extraContentSidePanelStore.selectAttachments(
      documentUnit.value
        ? documentUnit.value.originalDocumentAttachments.length - 1
        : 0,
    )
  }
}
</script>

<template>
  <DocumentViewer :document-number="documentNumber" :kind="Kind.DECISION">
    <template
      #default="{
        registerTextEditorRef,
        requestDocumentUnitFromServer,
        jumpToMatch: jumpToMatch,
      }"
    >
      <router-view
        v-bind="{ registerTextEditorRef, jumpToMatch: jumpToMatch }"
        @attachment-index-deleted="
          ($event: number) =>
            attachmentIndexDeleted($event, requestDocumentUnitFromServer)
        "
        @attachment-index-selected="attachmentIndexSelected"
        @attachments-uploaded="
          ($event: boolean) =>
            attachmentsUploaded($event, requestDocumentUnitFromServer)
        "
      >
      </router-view>
    </template>
  </DocumentViewer>
</template>
