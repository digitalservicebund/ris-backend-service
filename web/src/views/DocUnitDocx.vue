<script lang="ts" setup>
import { Document } from "@tiptap/extension-document"
import { Paragraph } from "@tiptap/extension-paragraph"
import { Text } from "@tiptap/extension-text"
import { EditorContent, Editor } from "@tiptap/vue-3"
import { ref } from "vue"
import { getAllDocxFiles, getDocxFileAsHtml } from "../api"
import {
  Randnummer,
  DocUnitParagraphExtension,
} from "../editor/DocUnitExtension"

var fileNames = ref()
getAllDocxFiles().then((list) => {
  fileNames.value = list
})

var fileName = ref()
function getHtml(name: string) {
  fileName.value = name

  getDocxFileAsHtml(name).then((html) => {
    editor.commands.setContent(html.content)
  })
}

const editor = new Editor({
  content: "<p>I’m running Tiptap with Vue.js. 🎉</p>",
  extensions: [
    Document,
    Paragraph,
    Text,
    Randnummer,
    DocUnitParagraphExtension,
  ],
})
</script>

<template>
  <div v-if="!!fileName">
    <a @click="fileName = null" @keyup="fileName = null">zurück zur Liste</a>
    <br />
    <div v-if="editor">
      <editor-content :editor="editor" />
    </div>
  </div>
  <div v-if="!fileName">
    <div v-for="file in fileNames" :key="file">
      <a @click="getHtml(file)" @keyup="getHtml(file)">{{ file }}</a>
    </div>
  </div>
</template>
