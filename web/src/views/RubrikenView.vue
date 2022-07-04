<script lang="ts" setup>
import { onMounted, onUnmounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import DocUnitCoreData from "../components/DocUnitCoreData.vue"
import EditorVmodel from "../components/EditorVmodel.vue"
import RouteHelper from "../components/RouteHelper.vue"
import TextInput from "../components/TextInput.vue"
import { useDocUnitsStore, useLayoutStateStore } from "../store"

const store = useDocUnitsStore()
const layoutStore = useLayoutStateStore()
const router = useRouter()
const route = useRoute()

const toggleOdocPanel = () => {
  if (!store.selectedHasFileAttached()) {
    router.push({
      name: "Dokumente",
      params: { id: store.getSelectedSafe().documentnumber },
    })
    return
  }
  if (!layoutStore.showOdocPanel) {
    store.fetchOriginalFileAsHTML()
  }
  layoutStore.showOdocPanel = !layoutStore.showOdocPanel
  const url = new URL(route.path, window.location.origin)
  url.searchParams.set(
    "showOdocPanel",
    layoutStore.showOdocPanel ? "true" : "false"
  )
  url.searchParams.set(
    "showSidebar",
    layoutStore.showSidebar ? "true" : "false"
  )
  history.pushState({}, "", url)
}

const originalOdocPanelYPos = 169 // read this dynamically, see onUpdated() TODO

/*onUpdated(() => {
  let element = document.getElementById("odoc-panel-element")
  // this one is around 200 and not 170, that means in the case of loading the
  // page with the odoc panel closed and then opening the odoc panel, it will
  // jump a bit while scrolling, that's not ok TODO
  if (!element) element = document.getElementById("odoc-open-element")
  if (element) originalOdocPanelYPos = element.getBoundingClientRect().y
})*/

const handleScroll = () => {
  const element = document.getElementById("odoc-panel-element")
  if (!element) return
  const pos = originalOdocPanelYPos - window.scrollY
  const threshold = -40 // this should also not be hardwired TODO
  element.style.top = (pos < threshold ? threshold : pos) + "px"
}

onMounted(() => window.addEventListener("scroll", handleScroll))
onUnmounted(() => window.removeEventListener("scroll", handleScroll))
</script>

<template>
  <RouteHelper />
  <span v-if="store.hasSelected()">
    <v-row>
      <v-col
        :cols="
          layoutStore.showOdocPanel
            ? layoutStore.odocPanelAsOverlay
              ? 12
              : 7
            : 9
        "
      >
        <DocUnitCoreData id="stammdaten" />
        <TextInput id="kurzUndLangtexte" />
      </v-col>
      <v-col v-if="!layoutStore.showOdocPanel" cols="3" align="right">
        <div
          id="odoc-open-element"
          class="odoc-open"
          :onclick="toggleOdocPanel"
        >
          <div class="odoc-open-text">Originaldokument</div>
          <div class="odoc-open-icon-background">
            <v-icon class="odoc-open-icon"> arrow_back_ios_new </v-icon>
          </div>
        </div>
      </v-col>
      <v-col
        v-else
        cols="5"
        :class="{ 'odoc-as-overlay': layoutStore.odocPanelAsOverlay }"
      >
        <div id="odoc-panel-element" class="odoc-panel">
          <h3 class="odoc-editor-header">
            <div class="odoc-close-icon-background" :onclick="toggleOdocPanel">
              <v-icon class="odoc-close-icon"> close </v-icon>
            </div>
            Originaldokument
          </h3>
          <div v-if="!store.getSelectedSafe().originalFileAsHTML">
            Loading...
          </div>
          <div v-else class="odoc-editor-wrapper">
            <EditorVmodel
              v-model="store.getSelectedSafe().originalFileAsHTML"
              field-size="max"
              :editable="false"
            />
          </div>
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
.odoc-panel {
  position: fixed;
}
.odoc-as-overlay {
  position: fixed;
  right: 0;
  z-index: 1;
  background-color: rgba(255, 255, 255, 0.85);
}
</style>
