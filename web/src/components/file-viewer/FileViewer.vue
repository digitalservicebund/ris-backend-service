<script lang="ts" setup>
import dayjs from "dayjs"
import { onMounted } from "vue"
import { deleteFile, getDocxFileAsHtml } from "../../api"
import { useDocUnitsStore } from "../../store"
import EditorVmodel from "../EditorVmodel.vue"
import RisButton from "../ris-button/RisButton.vue"

const store = useDocUnitsStore()

const onSubmit = async () => {
  const docUnit = await deleteFile(store.getSelected()?.id)
  console.log("file delete from doc unit, response:", docUnit)
  store.update(docUnit)
}

onMounted(() => {
  if (!store.hasSelected() || !store.selectedHasFileAttached()) {
    return
  }
  getDocxFileAsHtml(store.getSelectedSafe().s3path).then((response) => {
    store.setHTMLOnSelected(response.content)
  })
})
</script>

<template>
  <v-container v-if="store.hasSelected()">
    <v-row>
      <v-col>
        Dateiname: {{ store.getSelected()?.filename }}, Hochgeladen am
        {{
          dayjs(store.getSelected()?.fileuploadtimestamp).format("DD.MM.YYYY")
        }}, Format: {{ store.getSelected()?.filetype }}
      </v-col>
    </v-row>
    <v-row>
      <v-col>
        <ris-button label="Datei löschen" @click="onSubmit" />
      </v-col>
    </v-row>
    <v-row>
      <v-col>
        <EditorVmodel
          v-model="store.getSelectedSafe().originalFileAsHTML"
          field-size="max"
          :editable="false"
        />
      </v-col>
    </v-row>
  </v-container>
</template>
