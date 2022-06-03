<script lang="ts" setup>
import { useDocUnitsStore } from "./store"

const docUnitsStore = useDocUnitsStore()
</script>

<template>
  <v-app>
    <v-app-bar color="blue800"></v-app-bar>
    <v-navigation-drawer color="grey-lighten-2" permanent>
      <router-link to="/">Home</router-link>
      <br />
      <router-link to="/upload">Upload</router-link>
      <span v-if="docUnitsStore.getSelected() !== null">
        <br /><br />
        DocUnit {{ docUnitsStore.getSelected().id }}
        <br />
        <router-link
          :to="{
            name: 'Stammdaten',
            params: { id: docUnitsStore.getSelected().id },
          }"
          >Stammdaten</router-link
        >
      </span>
    </v-navigation-drawer>
    <v-main>
      <router-view></router-view>
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
</style>
