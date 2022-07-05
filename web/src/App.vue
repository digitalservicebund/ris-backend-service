<script lang="ts" setup>
import { onMounted, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import DocInfoPanel from "./components/DocUnitInfoPanel.vue"
import NavbarSide from "./components/NavbarSide.vue"
import NavbarTop from "./components/NavbarTop.vue"
import { useDocUnitsStore, useLayoutStateStore } from "./store"

const store = useDocUnitsStore()
const layoutStore = useLayoutStateStore()
const router = useRouter()
const route = useRoute()

const toggleSidebar = () => {
  layoutStore.showSidebar = !layoutStore.showSidebar
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

/*const updateLayout = () => {
  layoutStore.showSidebar = window.innerWidth > 1280
  layoutStore.sidebarAsOverlay = window.innerWidth <= 1280
  layoutStore.showOdocPanel =
    window.innerWidth > 1024 && store.canShowOdocPanel()
  layoutStore.odocPanelAsOverlay = window.innerWidth <= 1024
}*/

onMounted(async () => {
  // updateLayout()
  // window.addEventListener("resize", updateLayout)
  await router.isReady()
  if (route.query.showSidebar) {
    layoutStore.showSidebar = route.query.showSidebar === "true"
  }
  routerReady = true
  tryDecideShowingOdocPanel()
})
// onUnmounted(() => window.removeEventListener("resize", updateLayout))

let routerReady = false
let storeReady = false

// Is there a more elegant way to do this?
// We have to wait for the router to be ready, otherwise we can't get the query params.
// And we have to wait for "selected" to be loaded, otherwise we don't know if it has an original file attached.
const tryDecideShowingOdocPanel = () => {
  if (!routerReady || !storeReady || !route.query.showOdocPanel) return
  layoutStore.showOdocPanel = route.query.showOdocPanel === "true"
  if (layoutStore.showOdocPanel) store.fetchOdocInBackgroundIfExistent()
  routerReady = false // close door to this function
}

watch(
  () => store.selected,
  () => {
    if (!store.selected) return
    storeReady = true
    tryDecideShowingOdocPanel()
  }
)
</script>

<template>
  <v-app>
    <NavbarTop />
    <v-main>
      <v-container fluid>
        <v-row>
          <v-col v-if="layoutStore.sidebarAsOverlay" cols="1"></v-col>
          <v-col
            v-if="store.hasSelected()"
            :cols="layoutStore.showSidebar ? 2 : 1"
            :class="{ 'sidebar-as-overlay': layoutStore.sidebarAsOverlay }"
          >
            <span v-if="layoutStore.showSidebar">
              <div
                class="sidebar-close-icon-background"
                :onclick="toggleSidebar"
              >
                <v-icon class="sidebar-close-icon"> close </v-icon>
              </div>
              <NavbarSide />
            </span>
            <div v-else class="sidebar-open" :onclick="toggleSidebar">
              <div class="sidebar-open-text">Menü</div>
              <div class="sidebar-open-icon-background">
                <v-icon class="sidebar-open-icon"> arrow_forward_ios </v-icon>
              </div>
            </div>
          </v-col>
          <v-col
            :cols="
              store.hasSelected() ? (layoutStore.showSidebar ? 10 : 11) : 12
            "
            class="panel_and_main_area"
          >
            <DocInfoPanel />
            <router-view />
          </v-col>
        </v-row>
      </v-container>
    </v-main>
  </v-app>
</template>

<style lang="scss">
body {
  font-size: $font-size-root;
  font-family: $font-main;
}
a {
  color: black;
}
.panel_and_main_area {
  background-color: $white;
}
.sidebar-close-icon-background {
  background-color: $blue800;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  float: right;
  transform: translateY(60px);
}
.sidebar-close-icon {
  color: white;
  margin-left: 8px;
  margin-top: 8px;
}
.sidebar-open {
  background-color: $yellow500;
  border-radius: 10px;
  border: 3px solid $blue800;
  width: 100px;
  height: 65px;
  display: flex;
  justify-content: center; // align horizontal
  align-items: center; // align vertical
  margin-left: 6px;
  transform: rotate(-90deg) translateX(-165px);
  transform-origin: left;
}
.sidebar-open-text {
  margin-left: 40px;
}
.sidebar-open-icon-background {
  background-color: $blue800;
  border-radius: 50%;
  min-width: 40px;
  height: 40px;
  transform: rotate(90deg) translateX(3px) translateY(-10px);
}
.sidebar-open-icon {
  color: white;
  margin-left: 9px;
  margin-top: 8px;
}
.sidebar-as-overlay {
  position: fixed;
  z-index: 1;
  background-color: rgba(255, 255, 255, 0.85);
  // height: 100%;
}
</style>
