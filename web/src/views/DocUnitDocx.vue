<script lang="ts" setup>
  import { getAllDocxFiles, getDocxFileAsHtml } from '../api'
  import { ref } from 'vue';
  import { EditorContent, Editor } from '@tiptap/vue-3'
  import Document from '@tiptap/extension-document'
  import Paragraph from '@tiptap/extension-paragraph'
  import Text from '@tiptap/extension-text'
  import { Randnummer, DocUnitParagraphExtension } from '../editor/DocUnitExtension'

var fileNames = ref()
  getAllDocxFiles().then((list) => {
    fileNames.value = list
  })

  var fileName =ref()
  function getHtml(name: string) {
    getDocxFileAsHtml(name).then((html) => {
      editor.commands.setContent(html.content)
    });
  }

  const editor = new Editor({
    content: '<p>I’m running Tiptap with Vue.js. 🎉</p>',
    extensions: [
        Document,
        Paragraph,
        Text,
        Randnummer,
        DocUnitParagraphExtension,
    ],
  })
  console.log(editor.schema)
</script>

<template>
  <div v-if="!!fileName">
    <a @click="fileName = null">zurück zur Liste</a>
    <br />
    <editor-content :editor="editor" />
  </div>
  <div v-if="!fileName">
    <div v-for="file in fileNames">
      <a @click="fileName = file; getHtml(file);">{{ file }}</a>
    </div>
  </div>
</template>
