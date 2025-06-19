<script lang="ts" setup>
import DocumentViewer from "@/components/DocumentViewer.vue"

import { Kind } from "@/domain/documentationUnitKind"

defineProps<{
  documentNumber: string
}>()
</script>

<template>
  <DocumentViewer
    :document-number="documentNumber"
    :extra-content-side-panel-props="{
      hidePanelModeBar: true,
      hidePreviewInNewTab: true,
      sidePanelMode: 'preview',
    }"
    :kind="Kind.PENDING_PROCEEDING"
  >
    <template #default="{ registerTextEditorRef, jumpToMatch: jumpToMatch }">
      <router-view v-bind="{ registerTextEditorRef, jumpToMatch: jumpToMatch }">
      </router-view>
    </template>
  </DocumentViewer>
</template>
