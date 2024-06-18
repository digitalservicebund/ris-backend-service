<script lang="ts" setup>
import { useHead } from "@unhead/vue"
import { onMounted, ref } from "vue"
import { useRoute } from "vue-router"
import DocumentUnitAttachments from "@/components/DocumentUnitAttachments.vue"
import RouteErrorDisplay from "@/components/RouteErrorDisplay.vue"
import DocumentUnit from "@/domain/documentUnit"
import { ResponseError } from "@/services/httpClient"

const props = defineProps<{ documentUnit: DocumentUnit }>()

const emit = defineEmits<{
  updateDocumentUnit: [void]
}>()
useHead({
  title:
    props.documentUnit.documentNumber + " · NeuRIS Rechtsinformationssystem",
})
const route = useRoute()

const error = ref<ResponseError>()

async function loadDocumentUnit() {
  emit("updateDocumentUnit")
}

onMounted(() => loadDocumentUnit())
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
    @update-document-unit="loadDocumentUnit"
  />
  <RouteErrorDisplay v-else :error="error" />
</template>
