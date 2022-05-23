<script lang="ts" setup>
import { ref, Ref } from "vue"
import { getVersion, getAllDocUnits } from "./api"
import HelloWorld from "./components/HelloWorld.vue"
import RisButton from "./components/RisButton.vue"

const version = ref({ version: "🤷‍♂️", commitSHA: "🤷‍♀️" })

type DocUnit = {
  id: number
  s3path: string
  filetype: string
}

const docUnits: Ref<DocUnit[]> = ref([])
const updateVersion = async () => {
  version.value = await getVersion()
}

const updateDocUnits = async () => {
  docUnits.value = await getAllDocUnits()
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
      <v-row v-for="docUnit in docUnits" :key="docUnit.id" class="text-center">
        <v-col class="mb-4">
          {{ docUnit.id }}, {{ docUnit.s3path }}, {{ docUnit.filetype }}
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
