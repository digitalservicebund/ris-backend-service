<script lang="ts" setup>
import { ref } from "vue"
import { useRoute } from "vue-router"
import DocumentUnitAttachments from "@/components/DocumentUnitAttachments.vue"
import RouteErrorDisplay from "@/components/RouteErrorDisplay.vue"
import DocumentUnit from "@/domain/documentUnit"
import { ResponseError } from "@/services/httpClient"

defineProps<{ documentUnit: DocumentUnit }>()

const emit = defineEmits<{
  attachmentsUploaded: [boolean]
  attachmentIndexSelected: [number]
  attachmentIndexDeleted: [number]
}>()
const route = useRoute()

const error = ref<ResponseError>()

async function attachmentsUploaded(anySuccessful: boolean) {
  emit("attachmentsUploaded", anySuccessful)
}

async function attachmentIndexSelected(index: number) {
  console.log("emit from files")
  emit("attachmentIndexSelected", index)
}

async function attachmentIndexDeleted(index: number) {
  emit("attachmentIndexDeleted", index)
}
</script>

<template>
  <DocumentUnitAttachments
    v-if="documentUnit"
    :document-unit="documentUnit as DocumentUnit"
    :show-navigation-panel="
      route.query.showNavigationPanel
        ? route.query.showNavigationPanel === 'true'
        : true
    "
    @attachment-index-deleted="attachmentIndexDeleted"
    @attachment-index-selected="attachmentIndexSelected"
    @attachments-uploaded="attachmentsUploaded"
  />
  <RouteErrorDisplay v-else :error="error" />
</template>
