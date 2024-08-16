<script lang="ts" setup>
import { useRoute } from "vue-router"
import EditionEvaluation from "@/components/legalperiodical/EditionEvaluation.vue"

const route = useRoute()
</script>

<template>
  <EditionEvaluation
    :legal-periodical-edition-id="route.params.uuid.toString()"
  />
</template>
