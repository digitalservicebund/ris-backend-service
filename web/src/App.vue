<script lang="ts" setup>
import DocInfoPanel from "./components/DocUnitInfoPanel.vue"
import NavbarSide from "./components/NavbarSide.vue"
import NavbarTop from "./components/NavbarTop.vue"
import { useDocUnitsStore, useLayoutStateStore } from "./store"

const store = useDocUnitsStore()
const layoutStore = useLayoutStateStore()

const toggleSidebar = () => {
  layoutStore.showSidebar = !layoutStore.showSidebar
}
</script>

<template>
  <v-app>
    <NavbarTop />
    <v-main>
      <v-container fluid>
        <v-row>
          <v-col
            v-if="store.hasSelected()"
            :cols="layoutStore.showSidebar ? 2 : 1"
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
</style>
