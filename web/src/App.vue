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
