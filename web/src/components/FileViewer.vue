<script lang="ts" setup>
import dayjs from "dayjs"
import { onMounted } from "vue"
import { deleteFile } from "../api/docUnitService"
import { useDocUnitsStore } from "../store"
import EditorVmodel from "./EditorVmodel.vue"
import SimpleButton from "./SimpleButton.vue"

const store = useDocUnitsStore()

const onSubmit = async () => {
  const docUnit = await deleteFile(store.getSelected()?.uuid)
  console.log("file delete from doc unit, response:", docUnit)
  store.update(docUnit)
}

onMounted(() => store.fetchOriginalFileAsHTML())
</script>

<template>
  <span v-if="store.hasSelected()">
    <v-container class="fileviewer-info-panel">
      <v-row>
        <v-col sm="6">
          Dateiname
          <div class="fileviewer-info-panel-value">
            {{ store.getSelected()?.filename }}
          </div>
        </v-col>
        <v-col sm="3" md="2">
          Hochgeladen am
          <div class="fileviewer-info-panel-value">
            {{
              dayjs(store.getSelected()?.fileuploadtimestamp).format(
                "DD.MM.YYYY"
              )
            }}
          </div>
        </v-col>
        <v-col sm="3" md="2">
          Format
          <div class="fileviewer-info-panel-value">
            {{ store.getSelected()?.filetype }}
          </div>
        </v-col>
        <v-col sm="3" md="2">
          Von
          <div class="fileviewer-info-panel-value">USER NAME</div>
        </v-col>
        <v-col cols="4" />
      </v-row>
      <v-row class="fileviewer-info-panel">
        <v-col cols="12">
          <SimpleButton icon="delete" label="Datei löschen" @click="onSubmit" />
        </v-col>
      </v-row>
    </v-container>
    <v-container>
      <v-row>
        <v-col cols="12">
          <EditorVmodel
            v-model="store.getSelectedSafe().originalFileAsHTML"
            field-size="max"
            :editable="false"
          />
        </v-col>
      </v-row>
    </v-container>
  </span>
</template>

<style lang="scss">
.fileviewer-info-panel {
  background-color: $white;
}

.fileviewer-info-panel-value {
  color: $gray900;
  font-weight: bold;
}
</style>
