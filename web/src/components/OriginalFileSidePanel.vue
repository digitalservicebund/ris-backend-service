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
}

.odoc-upload-note {
  margin-bottom: 15px;
}

.link-to-upload {
  color: $blue800;
}
</style>
