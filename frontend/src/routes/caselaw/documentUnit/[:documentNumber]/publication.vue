<script lang="ts" setup>
import { onMounted, ref } from "vue"
import DocumentUnitPublication from "@/components/DocumentUnitPublication.vue"
import RouteErrorDisplay from "@/components/RouteErrorDisplay.vue"
import DocumentUnit from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"
import { ResponseError } from "@/services/httpClient"

const props = defineProps<{ documentNumber: string }>()

const documentUnit = ref<DocumentUnit>()
const error = ref<ResponseError>()

async function loadDocumentUnit() {
  const response = await documentUnitService.getByDocumentNumber(
    props.documentNumber
  )

  documentUnit.value = response.data
  error.value = response.error
}

onMounted(() => loadDocumentUnit())
</script>

<template>
  <DocumentUnitPublication
    v-if="documentUnit"
    :document-unit="(documentUnit as DocumentUnit)"
    @update-document-unit="loadDocumentUnit"
  />
  <RouteErrorDisplay v-else :error="error" />
</template>
