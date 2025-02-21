<script setup lang="ts">
import PendingProceedingPreview from "@/components/preview/PendingProceedingPreview.vue"
import { useRoute } from "vue-router"

const route = useRoute()
const documentNumber = route.params.documentNumber as string
</script>
<template>
  <PendingProceedingPreview :document-number="documentNumber" />
</template>
