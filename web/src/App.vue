<script lang="ts" setup>
import { ref } from "vue"
import { getVersion } from "./api"
import DocUnitList from "./components/doc-unit-list/DocUnitList.vue"
import HelloWorld from "./components/hello-world/HelloWorld.vue"
import RisButton from "./components/ris-button/RisButton.vue"
import RisStammDaten from "./components/ris-stammdaten/RisStammDaten.vue"

const version = ref({ version: "🤷‍♂️", commitSHA: "🤷‍♀️" })

const updateVersion = async () => {
  version.value = await getVersion()
}
</script>

<template>
  <v-app>
    <v-app-bar color="grey-lighten-2"></v-app-bar>
    <v-navigation-drawer color="grey-darken-2" permanent></v-navigation-drawer>
    <v-main>
      <v-row>
        <v-col class="mb-4">
          <HelloWorld />
        </v-col>
      </v-row>
      <v-row>
        <v-col class="mb-4">
          <h2>RandomNumber123</h2>
        </v-col>
      </v-row>
      <RisStammDaten />
      <v-row class="text-center">
        <v-col class="mb-4">
          <em>version:</em>{{ version.version }}, <em>commit:</em
          >{{ version.commitSHA }}
        </v-col>
      </v-row>
      <v-row class="text-center">
        <v-col class="mb-4">
          <RisButton
            label="update API version"
            color="blue800"
            @click="updateVersion"
          />
        </v-col>
      </v-row>
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
