<script setup lang="ts">
import { ref } from "vue"
import { useRoute } from "vue-router"
import DocumentUnitCategories from "@/components/DocumentUnitCategories.vue"
import DocumentUnitPreview from "@/components/DocumentUnitPreview.vue"
import ErrorPage from "@/components/ErrorPage.vue"
import DocumentUnit from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"
import { ResponseError } from "@/services/httpClient"

const props = defineProps<{ documentNumber: string }>()

const route = useRoute()

const { data: documentUnit, error } =
  await documentUnitService.getByDocumentNumber(props.documentNumber)
</script>
<template>
  <DocumentUnitPreview
    v-if="documentUnit"
    :document-unit="documentUnit"
    :show-navigation-panel="
      route.query.showNavigationPanel
        ? route.query.showNavigationPanel === 'true'
        : true
    "
  ></DocumentUnitPreview>
  <ErrorPage v-else :error="error" :title="error?.title" />
</template>
