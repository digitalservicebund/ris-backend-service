<script lang="ts" setup>
import { ref } from "vue"
import TextEditor from "@/components/TextEditor.vue"
import fileService from "@/services/fileService"

const fileNames = ref(await fileService.getAllDocxFiles())
const fileName = ref()

const htmlContent = ref()

const getHtml = async (name: string) => {
  fileName.value = name
  htmlContent.value = await fileService.getDocxFileAsHtml(name)
}
</script>

<template>
  <div v-if="!!fileName">
    <a @click="fileName = null" @keyup="fileName = null">zurück zur Liste</a>
    <br />
    <TextEditor v-model="htmlContent" field-size="max" :editable="false" />
  </div>
  <div v-if="!fileName">
    <div v-for="file in fileNames" :key="file">
      <a @click="getHtml(file)" @keyup="getHtml(file)">{{ file }}</a>
    </div>
  </div>
</template>
