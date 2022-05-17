<script setup lang="ts">
import { ref } from "vue"
import { VaButton } from "vuestic-ui"
import { getVersion } from "./api"

const version = ref({ version: "🤷‍♂️", commitSHA: "🤷‍♀️" })

const updateVersion = async () => {
  version.value = await getVersion()
}
</script>

<template>
  <h1 class="text-2xl text-center text-blue-800 m-4">Hello DigitalService!</h1>

  <div class="m-20">
    <va-button color="danger" @click="updateVersion"
      >update API version</va-button
    >
    <div class="mt-10">
      <em>version:</em>{{ version.version }}, <em>commit:</em
      >{{ version.commitSHA }}
    </div>
  </div>
</template>
