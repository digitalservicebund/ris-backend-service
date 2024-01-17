<script lang="ts" setup>
import { onMounted, ref } from "vue"
import httpClient from "@/services/httpClient"

const msg = ref<string | undefined>("Loading...")

onMounted(async () => {
  const response = await httpClient.get<string>(`search`)
  console.log(response)
  msg.value = response.data
})
</script>

<template>
  <div class="flex flex-col gap-16 p-16">
    <div class="flex flex-col">
      <h1 class="ds-heading-02-reg">Suche</h1>
      <p>{{ msg }}</p>
    </div>
  </div>
</template>
