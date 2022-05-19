<script lang="ts" setup>
import { ref } from "vue"
import { getVersion } from "./api"
import HelloWorld from "./components/HelloWorld.vue"

const version = ref({ version: "🤷‍♂️", commitSHA: "🤷‍♀️" })
const label = ref("update API version")

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
          <v-btn
            :ripple="false"
            :flat="true"
            color="blue800"
            @click="updateVersion"
          >
            {{ label }}
          </v-btn>
        </v-col>
      </v-row>
    </v-main>
  </v-app>
</template>

<style scoped lang="scss">
.v-btn {
  border-radius: $btn-border-radius;
}
</style>
