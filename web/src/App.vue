<script lang="ts" setup>
import { ref } from "vue"
import { getVersion, getAllDocUnits } from "./api"
import HelloWorld from "./components/HelloWorld.vue"
import RisButton from "./components/RisButton.vue"

const version = ref({ version: "🤷‍♂️", commitSHA: "🤷‍♀️" })
// const docUnits = ref([])

const updateVersion = async () => {
  version.value = await getVersion()
}

const updateDocUnits = async () => {
  let docUnits = await getAllDocUnits()
  console.log("docUnits", docUnits)
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
          <RisButton
            label="fetch all doc units"
            color="blue800"
            @click="updateDocUnits"
          >
          </RisButton>
        </v-col>
        <v-col>
          <RisButton label="button large" size="large" color="blue800">
          </RisButton>
        </v-col>
        <v-col>
          <RisButton label="button small" size="small" color="blue800">
          </RisButton>
        </v-col>
      </v-row>
    </v-main>
  </v-app>
</template>

<style lang="scss">
body {
  font-size: $font-size-root;
}
</style>
