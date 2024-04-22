<script lang="ts" setup>
import { onMounted, ref } from "vue"
import { useRoute } from "vue-router"
import DocumentUnitFiles from "@/components/DocumentUnitAttachments.vue"
import RouteErrorDisplay from "@/components/RouteErrorDisplay.vue"
import DocumentUnit from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"
import { ResponseError } from "@/services/httpClient"

const props = defineProps<{ documentNumber: string }>()
const documentUnit = ref<DocumentUnit>()

const route = useRoute()

const error = ref<ResponseError>()

async function loadDocumentUnit() {
  const response = await documentUnitService.getByDocumentNumber(
    props.documentNumber,
  )
  documentUnit.value = response.data
  error.value = response.error
}

onMounted(() => loadDocumentUnit())
</script>

<template>
  <DocumentUnitFiles
    v-if="documentUnit"
    :document-unit="documentUnit as DocumentUnit"
    :show-attachment-panel="route.query.showAttachmentPanel === 'true'"
    @update-document-unit="loadDocumentUnit"
  />
  <RouteErrorDisplay v-else :error="error" />
</template>
