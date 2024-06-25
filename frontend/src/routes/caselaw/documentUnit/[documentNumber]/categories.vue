<script setup lang="ts">
import { useHead } from "@unhead/vue"
import { storeToRefs } from "pinia"
import { onMounted, ref } from "vue"
import { useRoute } from "vue-router"
import DocumentUnitCategories from "@/components/DocumentUnitCategories.vue"
import ErrorPage from "@/components/ErrorPage.vue"
import DocumentUnit from "@/domain/documentUnit"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const props = defineProps<{
  documentNumber: string
}>()

useHead({
  title: props.documentNumber + " · NeuRIS Rechtsinformationssystem",
})

const route = useRoute()
const responseError = ref()

const store = useDocumentUnitStore()
const { documentUnit } = storeToRefs(store)

onMounted(async () => {
  const { error } = await store.loadDocumentUnit(props.documentNumber)
  responseError.value = error
})
</script>

<template>
  <DocumentUnitCategories
    v-if="documentUnit"
    :document-unit="documentUnit as DocumentUnit"
    :show-navigation-panel="
      route.query.showNavigationPanel
        ? route.query.showNavigationPanel === 'true'
        : true
    "
  />
  <ErrorPage v-else :error="responseError" :title="responseError?.title" />
</template>
