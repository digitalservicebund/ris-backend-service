<script lang="ts" setup>
import DocUnitList from "./components/doc-unit-list/DocUnitList.vue"
import HelloWorld from "./components/hello-world/HelloWorld.vue"
import RisStammDaten from "./components/ris-stammdaten/RisStammDaten.vue"
</script>

<template>
  <v-app>
    <v-app-bar color="grey-lighten-2"></v-app-bar>
    <v-navigation-drawer color="grey-darken-2" permanent></v-navigation-drawer>
    <v-main>
      <v-row><HelloWorld></HelloWorld></v-row>
      <v-row>
        <v-col class="mb-4">
          <h2>RandomNumber123</h2>
        </v-col>
      </v-row>
      <RisStammDaten />
      <DocUnitList />
    </v-main>
  </v-app>
</template>

<style lang="scss">
body {
  font-size: $font-size-root;
  font-family: $font-main;
}
</style>
