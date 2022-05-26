<script lang="ts" setup>
import { onMounted } from "vue"
import { store } from "../../store"

onMounted(() => {
  store.getAllDocUnits()
})
</script>

<template>
  <ul v-if="store.hasDocUnits()">
    <li v-for="docUnit in store.getDocUnits()" :key="docUnit.id">
      {{ docUnit.id }}, {{ docUnit.s3path }}, {{ docUnit.filetype }}
    </li>
  </ul>
  <span v-else>No doc units found</span>
</template>
