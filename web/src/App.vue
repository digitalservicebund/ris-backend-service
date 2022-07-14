<script lang="ts" setup>
import Navbar from "./components/NavbarTop.vue"
</script>

<template>
  <v-app>
    <Navbar />
    <Suspense>
      <router-view />
    </Suspense>
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
