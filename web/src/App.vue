<script lang="ts" setup>
import { ref } from "vue"
import { getVersion } from "./api"
import DocUnitList from "./components/doc-unit-list/DocUnitList.vue"
import HelloWorld from "./components/hello-world/HelloWorld.vue"
import RisButton from "./components/ris-button/RisButton.vue"

const version = ref({ version: "🤷‍♂️", commitSHA: "🤷‍♀️" })

const updateVersion = async () => {
  version.value = await getVersion()
}
</script>

<template>
  <v-app>
    <v-main>
      <HelloWorld />
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
          >
          </RisButton>
        </v-col>
        <v-col>
          <RisButton label="button large" size="large" color="blue800">
          </RisButton>
        </v-col>
        <v-col>
          <RisButton size="small" color="blue800"> </RisButton>
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
