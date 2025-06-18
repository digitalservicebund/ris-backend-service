<script lang="ts" setup>
import DecisionAttachments from "@/components/DecisionAttachments.vue"

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
  <DecisionAttachments
    @attachment-index-deleted="attachmentIndexDeleted"
    @attachment-index-selected="attachmentIndexSelected"
    @attachments-uploaded="attachmentsUploaded"
  />
</template>
