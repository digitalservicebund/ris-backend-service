<script lang="ts" setup>
import { ref, onMounted } from "vue"
import { useRoute } from "vue-router"

const route = useRoute()
const docUnitId = ref()

onMounted(() => {
  docUnitId.value = route.params.id
})
</script>

<template>DocUnit {{ docUnitId }}</template>
