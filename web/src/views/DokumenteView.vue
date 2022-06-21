<script lang="ts" setup>
import FileUpload from "../components/FileUpload.vue"
import FileViewer from "../components/FileViewer.vue"
import RouteHelper from "../components/RouteHelper.vue"
import { useDocUnitsStore } from "../store"

const store = useDocUnitsStore()
</script>

<template>
  <RouteHelper />
  <v-container>
    <v-row>
      <v-col><h2>Dokumente</h2></v-col>
    </v-row>
  </v-container>
  <span v-if="store.hasSelected()">
    <FileViewer v-if="store.selectedHasFileAttached()" />
    <FileUpload v-else />
  </span>
</template>
