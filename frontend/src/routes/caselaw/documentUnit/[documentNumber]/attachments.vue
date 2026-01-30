<script lang="ts" setup>
import DecisionOriginalDocumentAttachments from "@/components/DecisionOriginalDocumentAttachments.vue"

const emit = defineEmits<{
  attachmentsUploaded: [boolean]
  attachmentIndexSelected: [number]
  attachmentIndexDeleted: [number]
}>()

async function attachmentsUploaded(anySuccessful: boolean) {
  emit("attachmentsUploaded", anySuccessful)
}

async function attachmentIndexSelected(index: number) {
  emit("attachmentIndexSelected", index)
}

async function attachmentIndexDeleted(index: number) {
  emit("attachmentIndexDeleted", index)
}
</script>

<template>
  <DecisionOriginalDocumentAttachments
    @attachment-index-deleted="attachmentIndexDeleted"
    @attachment-index-selected="attachmentIndexSelected"
    @attachments-uploaded="attachmentsUploaded"
  />
</template>
