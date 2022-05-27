<script lang="ts" setup>
import { ref } from "vue"
import { uploadDocUnit } from "../../api"
import { useDocUnitsStore } from "../../store"

const docUnitsStore = useDocUnitsStore()
const file = ref()

const upload = async (file: File) => {
  let docUnit = await uploadDocUnit(file)
  console.log("file uploaded, response:", docUnit)
  docUnitsStore.add(docUnit)
}
</script>

<template>
  <v-file-input
    v-model="file"
    label="Select .docx file"
    @change="upload(file)"
  ></v-file-input>
</template>
