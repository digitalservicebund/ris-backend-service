<script lang="ts" setup>
import { onMounted, ref } from "vue"
import { useRoute } from "vue-router"
import DocumentUnitPublication from "@/components/DocumentUnitPublication.vue"
import RouteErrorDisplay from "@/components/RouteErrorDisplay.vue"
import DocumentUnit from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"
import { ResponseError } from "@/services/httpClient"

const props = defineProps<{
  documentNumber: string
  showNavigationPanel?: boolean
}>()

const documentUnit = ref<DocumentUnit>()
const error = ref<ResponseError>()
const route = useRoute()

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
  <DocumentUnitPublication
    v-if="documentUnit"
    :document-unit="documentUnit as DocumentUnit"
    :show-navigation-panel="
      route.query.showNavigationPanel
        ? route.query.showNavigationPanel === 'true'
        : true
    "
    @update-document-unit="loadDocumentUnit"
  />
  <RouteErrorDisplay v-else :error="error" />
</template>
