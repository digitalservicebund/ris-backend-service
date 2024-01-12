<script lang="ts" setup>
import { onMounted, ref } from "vue"
import { axiosInstance } from "@/services/httpClient"

const msg = ref<string>("Loading...")
const RIS_SEARCH_BASE_URL = "" // TODO

onMounted(async () => {
  const baseUrl = window.location.host.includes("localhost")
    ? "http://localhost:8090"
    : RIS_SEARCH_BASE_URL
  const response = await axiosInstance.request({
    method: "GET",
    url: `${baseUrl}/v1/search`,
  })
  console.log(response.data)
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
