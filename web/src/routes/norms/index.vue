<script lang="ts" setup>
import { ref } from "vue"
import NormsList from "@/components/NormsList.vue"
import { getAllNorms } from "@/services/normsService"

const norms = ref((await getAllNorms()).data)
</script>

<template>
  <div class="flex flex-col gap-16">
    <NormsList v-if="norms && norms.length !== 0" class="grow" :norms="norms" />
    <span v-else>Keine Normen gefunden</span>
  </div>
</template>
