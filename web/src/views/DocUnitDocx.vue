<script lang="ts" setup>
import { ref } from "vue"
import { getAllDocxFiles, getDocxFileAsHtml } from "../api/docUnitService"
import EditorVmodel from "../components/EditorVmodel.vue"

const fileNames = ref()
const htmlContent = ref()

getAllDocxFiles().then((list) => {
  fileNames.value = list
})

const fileName = ref()
function getHtml(name: string) {
  fileName.value = name

  getDocxFileAsHtml(name).then((html) => {
    htmlContent.value = html.content
  })
}
</script>

<template>
  <div v-if="!!fileName">
    <a @click="fileName = null" @keyup="fileName = null">zurück zur Liste</a>
    <br />
    <EditorVmodel v-model="htmlContent" field-size="max" :editable="false" />
  </div>
  <div v-if="!fileName">
    <div v-for="file in fileNames" :key="file">
      <a @click="getHtml(file)" @keyup="getHtml(file)">{{ file }}</a>
    </div>
  </div>
</template>
