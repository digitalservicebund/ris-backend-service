<script lang="ts" setup>
import { ref } from "vue"
import NormsList from "@/components/NormsList.vue"
import { getAllNorms } from "@/services/normsService"

const norms = ref((await getAllNorms()).data)
</script>

<template>
  <div class="bg-gray-100 flex flex-col gap-16">
    <NormsList
      v-if="norms && norms.length !== 0"
      class="grow max-w-screen-lg"
      :norms="norms"
    />
    <span v-else class="pl-64 pt-[3.5rem]">Keine Normen gefunden</span>
  </div>
</template>
