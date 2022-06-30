<script lang="ts" setup>
import { ref } from "vue"
import { useRouter } from "vue-router"
import DocUnitCoreData from "../components/DocUnitCoreData.vue"
import EditorVmodel from "../components/EditorVmodel.vue"
import RouteHelper from "../components/RouteHelper.vue"
import TextInput from "../components/TextInput.vue"
import { useDocUnitsStore } from "../store"

const store = useDocUnitsStore()
const router = useRouter()

const showOdocPanel = ref<boolean>(false) // odoc as in original document

const onOdocOpenClick = () => {
  if (!store.selectedHasFileAttached()) {
    router.push({
      name: "Dokumente",
      params: { id: store.getSelectedSafe().documentnumber },
    })
    return
  }
  store.fetchOriginalFileAsHTML()
  showOdocPanel.value = true
}

const onOdocCloseClick = () => {
  showOdocPanel.value = false
}
</script>

<template>
  <RouteHelper />
  <span v-if="store.hasSelected()">
    <v-row>
      <v-col :cols="showOdocPanel ? 7 : 9">
        <DocUnitCoreData id="stammdaten" />
        <TextInput id="kurzUndLangtexte" />
      </v-col>
      <v-col v-if="!showOdocPanel" cols="3" align="right">
        <div class="odoc-open" :onclick="onOdocOpenClick">
          <div class="odoc-open-text">Originaldokument</div>
          <div class="odoc-open-icon-background">
            <v-icon class="odoc-open-icon"> arrow_back_ios_new </v-icon>
          </div>
        </div>
      </v-col>
      <v-col v-else cols="5">
        <h3 class="odoc-editor-header">
          <div class="odoc-close-icon-background" :onclick="onOdocCloseClick">
            <v-icon class="odoc-close-icon"> close </v-icon>
          </div>
          Originaldokument
        </h3>
        <div v-if="!store.getSelectedSafe().originalFileAsHTML">Loading...</div>
        <div v-else class="odoc-editor-wrapper">
          <EditorVmodel
            v-model="store.getSelectedSafe().originalFileAsHTML"
            field-size="100percent"
            :editable="false"
          />
        </div>
      </v-col>
    </v-row>
  </span>
</template>

<style lang="scss">
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
  border-radius: 50%;
  width: 40px;
  height: 40px;
  transform: translate(-40px, 10px);
}
.odoc-close-icon {
  color: white;
  margin-left: 8px;
  margin-top: 8px;
}
.odoc-editor-header {
  padding: 40px 0 15px 20px; // top right bottom left
}
.odoc-editor-wrapper {
  border: 1px solid $gray400;
}
</style>
