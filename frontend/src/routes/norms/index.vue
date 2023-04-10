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
  <div class="bg-gray-100 flex flex-col gap-16">
    <div class="flex justify-between p-16 pl-64">
      <h1 class="heading-02-regular">Dokumentationseinheiten</h1>
      <TextButton
        label="Neue Dokumentationseinheit"
        @click="router.push({ name: 'norms-import' })"
      />
    </div>
    <NormsList
      v-if="norms && norms.length !== 0"
      class="grow max-w-screen-lg"
      :norms="norms"
    />
    <span v-else class="pl-64 pt-[3.5rem]">Keine Normen gefunden</span>
  </div>
</template>
