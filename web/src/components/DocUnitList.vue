<script lang="ts" setup>
import { onMounted, Ref, ref } from "vue"
import { getAllDocUnits } from "../api"

type DocUnit = {
  id: number
  s3path: string
  filetype: string
}

const docUnits: Ref<DocUnit[]> = ref([])

onMounted(async () => {
  docUnits.value = await getAllDocUnits()
})
</script>

<template>
  <ul v-if="docUnits.length">
    <li v-for="docUnit in docUnits" :key="docUnit.id">
      {{ docUnit.id }}, {{ docUnit.s3path }}, {{ docUnit.filetype }}
    </li>
  </ul>
  <span v-else>No doc units found</span>
</template>
