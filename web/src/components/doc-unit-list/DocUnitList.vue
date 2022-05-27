<script lang="ts" setup>
import { onMounted } from "vue"
import { useDocUnitsStore } from "../../store"

const docUnitsStore = useDocUnitsStore()

onMounted(() => {
  docUnitsStore.fetchAll()
})
</script>

<template>
  <ul v-if="!docUnitsStore.isEmpty()">
    <li v-for="docUnit in docUnitsStore.getAll()" :key="docUnit.id">
      {{ docUnit.id }}, {{ docUnit.s3path }}, {{ docUnit.filetype }}
    </li>
  </ul>
  <span v-else>No doc units found</span>
</template>
