<script setup lang="ts">
import DocumentUnitPreview from "@/components/preview/DocumentUnitPreview.vue"
import DocumentUnit from "@/domain/documentUnit"

defineProps<{ documentUnit: DocumentUnit }>()
</script>
<template>
  <DocumentUnitPreview />
</template>
