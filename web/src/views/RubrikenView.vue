<script lang="ts" setup>
import { useRoute } from "vue-router"
import RisStammDaten from "../components/ris-stammdaten/RisStammDaten.vue"

const route = useRoute()
</script>

<template>
  <v-row>
    <v-col class="mb-4">
      <h2>DocUnit {{ route.params.id }}</h2>
    </v-col>
  </v-row>
  <RisStammDaten :doc-unit-id="Number(route.params.id)" />
</template>
