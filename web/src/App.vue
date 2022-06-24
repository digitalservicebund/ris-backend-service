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
</style>
