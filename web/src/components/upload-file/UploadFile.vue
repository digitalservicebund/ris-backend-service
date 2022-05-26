<script lang="ts" setup>
import { ref } from "vue"
import { uploadDocUnit } from "../../api"
import { store } from "../../store"

const file = ref()

const upload = async (file: File) => {
  let docUnit = await uploadDocUnit(file)
  console.log("file uploaded, response:", docUnit)
  store.addDocUnit(docUnit)
}
</script>

<template>
  <v-file-input
    v-model="file"
    label="Select .docx file"
    @change="upload"
  ></v-file-input>
</template>
