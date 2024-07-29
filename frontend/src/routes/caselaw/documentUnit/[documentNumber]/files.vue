<script lang="ts" setup>
import { useRoute } from "vue-router"
import DocumentUnitAttachments from "@/components/DocumentUnitAttachments.vue"

const emit = defineEmits<{
  attachmentsUploaded: [boolean]
  attachmentIndexSelected: [number]
  attachmentIndexDeleted: [number]
}>()
const route = useRoute()

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
  <DocumentUnitAttachments
    :show-navigation-panel="
      route.query.showNavigationPanel
        ? route.query.showNavigationPanel === 'true'
        : true
    "
    @attachment-index-deleted="attachmentIndexDeleted"
    @attachment-index-selected="attachmentIndexSelected"
    @attachments-uploaded="attachmentsUploaded"
  />
</template>
