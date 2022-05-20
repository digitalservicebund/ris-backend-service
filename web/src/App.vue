<script lang="ts" setup>
import { ref } from "vue"
import { getVersion, getAllDocUnits } from "./api"
import HelloWorld from "./components/HelloWorld.vue"

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
          <v-btn
            :ripple="false"
            :flat="true"
            color="blue800"
            @click="updateVersion"
          >
            update API version
          </v-btn>
          <v-btn
            :ripple="false"
            :flat="true"
            color="blue800"
            @click="updateDocUnits"
          >
            fetch all doc units
          </v-btn>
        </v-col>
      </v-row>
    </v-main>
  </v-app>
</template>

<style scoped lang="scss">
.v-btn {
  border-radius: $btn-border-radius;
  margin: 0 10px 0 10px;
}
</style>
