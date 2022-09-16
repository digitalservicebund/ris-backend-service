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
      class="odoc-open"
      @click="$emit('togglePanel')"
    >
      <div class="odoc-open-text">Originaldokument</div>
      <div class="odoc-open-icon-background">
        <v-icon class="odoc-open-icon"> arrow_back_ios_new </v-icon>
      </div>
    </button>
  </v-col>
  <v-col v-else cols="5">
    <div id="odoc-panel-element" class="odoc-panel">
      <h3 class="odoc-editor-header">
        <button
          aria-label="Originaldokument schließen"
          class="odoc-close-icon-background"
          @click="$emit('togglePanel')"
        >
          <v-icon class="odoc-close-icon"> close </v-icon>
        </button>
        Originaldokument
      </h3>
      <div v-if="!hasFile">
        <v-icon class="odoc-upload-icon" size="50px">cloud_upload</v-icon>
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
          <v-icon> arrow_forward </v-icon>
          Zum Upload
        </router-link>
      </div>
      <div v-else-if="!file">Dokument wird geladen</div>
      <div v-else class="odoc-editor-wrapper">
        <TextEditor element-id="odoc" field-size="max" :value="file" />
      </div>
    </div>
  </v-col>
</template>

<style lang="scss" scoped>
@import "@/styles/variables";

.odoc-open {
  display: flex;
  width: 200px;
  height: 65px;
  align-items: center; // align vertical
  justify-content: center; // align horizontal
  border: 3px solid $blue800;
  margin-right: 6px;
  background-color: $yellow500;
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
  background-color: $blue800;
  border-radius: 50%;
  transform: rotate(90deg) translateX(-2px) translateY(-25px);
}

.odoc-open-icon {
  margin-top: 8px;
  margin-right: 9px;
  color: white;
}

.odoc-close-icon-background {
  width: 40px;
  height: 40px;
  background-color: $blue800;
  border-radius: 50%;
  color: $white;
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
}

.odoc-upload-note {
  margin-bottom: 15px;
}

.link-to-upload {
  color: $blue800;
}
</style>
