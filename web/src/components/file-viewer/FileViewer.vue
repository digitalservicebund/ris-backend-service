<script lang="ts" setup>
import { deleteFile } from "../../api"
import { useDocUnitsStore } from "../../store"
import RisButton from "../ris-button/RisButton.vue"

const store = useDocUnitsStore()

const onSubmit = async () => {
  let docUnit = await deleteFile(store.getSelected()?.id)
  console.log("file delete from doc unit, response:", docUnit)
  store.update(docUnit)
}
</script>

<template>
  <v-container>
    <v-row>
      <v-col>
        Dateiname: {{ store.getSelectedSafe().filename }}, Format:
        {{ store.getSelectedSafe().filetype.toUpperCase() }}
      </v-col>
    </v-row>
    <v-row>
      <v-col>
        <ris-button label="Datei löschen" @click="onSubmit" />
      </v-col>
    </v-row>
    <v-row>
      <v-col> TODO: Dokument anzeigen </v-col>
    </v-row>
  </v-container>
</template>
