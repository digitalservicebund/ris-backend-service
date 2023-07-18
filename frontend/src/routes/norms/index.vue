<script lang="ts" setup>
import { ref } from "vue"
import { useRouter } from "vue-router"
import NormsList from "@/components/NormsList.vue"
import { getAllNorms } from "@/services/norms"
import TextButton from "@/shared/components/input/TextButton.vue"

const norms = ref((await getAllNorms()).data)
const router = useRouter()
</script>

<template>
  <div class="flex flex-col gap-16 bg-gray-100">
    <div class="flex justify-between p-16 pl-64">
      <h1 class="ds-heading-02-reg">Dokumentationseinheiten</h1>
      <TextButton
        label="Neue Dokumentationseinheit"
        @click="router.push({ name: 'norms-import' })"
      />
    </div>
    <NormsList
      v-if="norms && norms.length !== 0"
      class="max-w-screen-lg grow"
      :norms="norms"
    />
    <span v-else class="pl-64 pt-[3.5rem]">Keine Normen gefunden</span>
  </div>
</template>
