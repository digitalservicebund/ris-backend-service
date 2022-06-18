<script lang="ts" setup>
import DocInfoPanel from "./components/doc-info-panel/DocInfoPanel.vue"
import SideNavbar from "./components/side-navbar/SideNavbar.vue"
import TopNavbar from "./components/top-navbar/TopNavbar.vue"
</script>

<template>
  <v-app>
    <TopNavbar />
    <v-main>
      <v-container>
        <v-row>
          <v-col cols="12" class="panel_and_main_area">
            <DocInfoPanel />
          </v-col>
        </v-row>
        <v-row>
          <v-col cols="2">
            <SideNavbar />
          </v-col>
          <v-col cols="10" class="panel_and_main_area">
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
  background-color: $panelAndMainAreaBackground;
}
</style>
