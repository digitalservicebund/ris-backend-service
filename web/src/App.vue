<script lang="ts" setup>
import { useDocUnitsStore } from "./store"

const docUnitsStore = useDocUnitsStore()
</script>

<template>
  <v-app>
    <v-app-bar color="blue800"></v-app-bar>
    <v-navigation-drawer
      v-if="docUnitsStore.hasSelected()"
      color="grey-lighten-2"
      permanent
    >
      <router-link
        :to="{
          name: 'Rubriken',
          params: { id: docUnitsStore.getSelected()?.id },
        }"
      >
        RUBRIKEN
      </router-link>
      <ul>
        <li>- Stammdaten</li>
        <li>- Rubriken</li>
      </ul>
      <router-link
        :to="{
          name: 'Dokumente',
          params: { id: docUnitsStore.getSelected()?.id },
        }"
        >DOKUMENTE</router-link
      >
      <br />
      BEARBEITUNGSSTAND
      <br />
      <br />
      <router-link to="/docx">docx -> html</router-link>
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
