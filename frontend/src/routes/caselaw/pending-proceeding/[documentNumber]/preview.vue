<script setup lang="ts">
import { useRoute } from "vue-router"
import PendingProceedingPreview from "@/components/preview/PendingProceedingPreview.vue"

const route = useRoute()
const documentNumber = route.params.documentNumber as string
</script>
<template>
  <PendingProceedingPreview :document-number="documentNumber" />
</template>
