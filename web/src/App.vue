<script lang="ts" setup>
import DocInfoPanel from "./components/DocUnitInfoPanel.vue"
import NavbarSide from "./components/NavbarSide.vue"
import NavbarTop from "./components/NavbarTop.vue"
import { useDocUnitsStore } from "./store"

const store = useDocUnitsStore()
</script>

<template>
  <v-app>
    <NavbarTop />
    <v-main>
      <v-container fluid>
        <v-row>
          <v-col v-if="store.hasSelected()" cols="2">
            <NavbarSide />
          </v-col>
          <v-col
            :cols="store.hasSelected() ? 10 : 12"
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
</style>
