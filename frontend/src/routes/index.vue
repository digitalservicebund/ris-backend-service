<script lang="ts" setup>
import { onBeforeMount } from "vue"
import { useRouter } from "vue-router"

const router = useRouter()
onBeforeMount(async () => {
  await router.push({
    name: "caselaw-search",
  })
})
</script>

<template><div></div></template>
