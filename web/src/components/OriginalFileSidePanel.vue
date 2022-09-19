<script setup lang="ts">
import { useRoute } from "vue-router"
import TextEditor from "@/components/TextEditor.vue"

defineProps<{ open: boolean; hasFile: boolean; file?: string }>()
defineEmits<{ (e: "togglePanel"): void }>()

const route = useRoute()
</script>

<template>
  <v-col v-if="!open" cols="3" align="right">
    <button
      id="odoc-open-element"
      class="odoc-open"
      aria-label="Originaldokument öffnen"
      @click="$emit('togglePanel')"
    >
      <div class="odoc-open-text">Originaldokument</div>
      <div class="odoc-open-icon-background">
        <span class="odoc-open-icon material-icons"> arrow_back_ios_new </span>
      </div>
    </button>
  </v-col>
  <v-col v-else cols="5">
    <div id="odoc-panel-element" class="odoc-panel">
      <h3 class="odoc-editor-header">
        <button
          class="odoc-close-icon-background"
          aria-label="Originaldokument schließen"
          @click="$emit('togglePanel')"
        >
          <span class="odoc-close-icon material-icons"> close </span>
        </button>
        Originaldokument
      </h3>
      <div v-if="!hasFile">
        <span class="odoc-upload-icon material-icons">cloud_upload</span>
        <div class="odoc-upload-note">
          Es wurde noch kein Originaldokument hochgeladen.
        </div>
        <router-link
          class="link-to-upload"
          :to="{
            name: 'jurisdiction-documentUnit-:documentNumber-files',
            params: { documentNumber: $route.params.documentNumber },
            query: route.query,
          }"
        >
          <span class="material-icons link-to-upload__icon">
            arrow_forward
          </span>
          Zum Upload
        </router-link>
      </div>
      <div v-else-if="!file">Dokument wird geladen</div>
      <div v-else class="odoc-editor-wrapper">
        <TextEditor
          :value="file"
          field-size="max"
          :editable="false"
          element-id="odoc"
        />
      </div>
    </div>
  </v-col>
</template>

<style lang="scss" scoped>
@import "@/styles/variables";

.odoc-open {
  background-color: $yellow500;
  border-radius: 10px;
  border: 3px solid $blue800;
  width: 200px;
  height: 65px;
  display: flex;
  justify-content: center; // align horizontal
  align-items: center; // align vertical
  margin-right: 6px;
  transform: rotate(-90deg);
  transform-origin: right;
}

.odoc-open-text {
  margin-left: 30px;
}

.odoc-open-icon-background {
  background-color: $blue800;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  transform: rotate(90deg) translateX(-2px) translateY(-25px);
}

.odoc-open-icon {
  color: white;
  margin-right: 9px;
  margin-top: 8px;
}

.odoc-close-icon-background {
  background-color: $blue800;
  color: $white;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  transform: translate(-40px, 10px);
}

.odoc-editor-header {
  padding: 40px 0 15px 20px; // top right bottom left
}

.odoc-editor-wrapper {
  border: 1px solid $gray400;
}

.odoc-panel {
  position: fixed;
}

.odoc-upload-icon {
  margin-bottom: 15px;
  font-size: 50px;
}

.odoc-upload-note {
  margin-bottom: 15px;
}

.link-to-upload {
  color: $blue800;
  display: flex;
  flex-flow: row nowrap;
}
</style>
