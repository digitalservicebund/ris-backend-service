<script setup lang="ts">
import { useHead } from "@unhead/vue"
import { useRoute } from "vue-router"
import DocumentUnitCategories from "@/components/DocumentUnitCategories.vue"
import ErrorPage from "@/components/ErrorPage.vue"
import documentUnitService from "@/services/documentUnitService"

const props = defineProps<{
  documentNumber: string
}>()

useHead({
  title: props.documentNumber + " · NeuRIS Rechtsinformationssystem",
})

const route = useRoute()

const { data: documentUnit, error } =
  await documentUnitService.getByDocumentNumber(props.documentNumber)
</script>

<template>
  <DocumentUnitCategories
    v-if="documentUnit"
    :document-unit="documentUnit"
    :show-navigation-panel="
      route.query.showNavigationPanel
        ? route.query.showNavigationPanel === 'true'
        : true
    "
  />
  <ErrorPage v-else :error="error" :title="error?.title" />
</template>
