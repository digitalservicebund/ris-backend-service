<script setup lang="ts">
import { useRoute } from "vue-router"
import TextEditor from "@/components/TextEditor.vue"

defineProps<{ open: boolean; hasFile: boolean; file?: string }>()
defineEmits<{ (e: "togglePanel"): void }>()

const route = useRoute()
</script>

<template>
  <v-col v-if="!open" align="right" cols="3">
    <button
      id="odoc-open-element"
      aria-label="Originaldokument öffnen"
      class="bg-yellow-500 border-3 border-blue-800 border-solid odoc-open"
      @click="$emit('togglePanel')"
    >
      <div class="odoc-open-text">Originaldokument</div>
      <div class="bg-blue-800 odoc-open-icon-background">
        <span class="material-icons odoc-open-icon text-white">
          arrow_back_ios_new
        </span>
      </div>
    </button>
  </v-col>
  <v-col v-else cols="5">
    <div id="odoc-panel-element" class="odoc-panel">
      <h3 class="odoc-editor-header">
        <button
          aria-label="Originaldokument schließen"
          class="bg-blue-800 odoc-close-icon-background text-white"
          @click="$emit('togglePanel')"
        >
          <span class="material-icons odoc-close-icon"> close </span>
        </button>
        Originaldokument
      </h3>
      <div v-if="!hasFile">
        <span class="material-icons mb-4 odoc-upload-icon text-blue-800"
          >cloud_upload</span
        >
        <div class="odoc-upload-note">
          Es wurde noch kein Originaldokument hochgeladen.
        </div>
        <span class="flex gap-x-4">
          <span class="material-icons text-blue-800"> arrow_forward </span>
          <router-link
            class="text-blue-800"
            :to="{
              name: 'jurisdiction-documentUnit-:documentNumber-files',
              params: { documentNumber: $route.params.documentNumber },
              query: route.query,
            }"
            >Zum Upload</router-link
          >
        </span>
      </div>
      <div v-else-if="!file">Dokument wird geladen</div>
      <div v-else class="border-1 border-gray-400 border-solid">
        <TextEditor element-id="odoc" field-size="max" :value="file" />
      </div>
    </div>
  </v-col>
</template>

<style lang="scss" scoped>
.odoc-open {
  display: flex;
  height: 65px;
  align-items: center; // align vertical
  justify-content: center; // align horizontal
  margin-right: 6px;
  border-radius: 10px;
  transform: rotate(-90deg);
  transform-origin: right;
}

.odoc-open-text {
  margin-left: 30px;
}

.odoc-open-icon-background {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  transform: rotate(90deg) translateX(-25px) translateY(-25px);
}

.odoc-open-icon {
  margin-top: 8px;
  margin-right: 9px;
}

.odoc-close-icon-background {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  transform: translate(-40px, 10px);
}

.odoc-editor-header {
  padding: 40px 0 15px 20px; // top right bottom left
}

.odoc-panel {
  position: fixed;
}

.odoc-upload-note {
  margin-bottom: 15px;
}

.odoc-upload-icon {
  font-size: 50px;
}
</style>
