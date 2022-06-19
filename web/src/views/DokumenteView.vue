<script lang="ts" setup>
import FileViewer from "../components/file-viewer/FileViewer.vue"
import RouteHelper from "../components/RouteHelper.vue"
import UploadFile from "../components/upload-file/UploadFile.vue"
import { useDocUnitsStore } from "../store"

const store = useDocUnitsStore()
</script>

<template>
  <RouteHelper />
  <v-container>
    <v-row>
      <v-col><h1>Dokumente</h1></v-col>
    </v-row>
  </v-container>
  <span v-if="store.hasSelected()">
    <FileViewer v-if="store.selectedHasFileAttached()" />
    <UploadFile v-else />
  </span>
</template>
