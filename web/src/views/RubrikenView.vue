<script lang="ts" setup>
import RisStammDaten from "../components/ris-stammdaten/RisStammDaten.vue"
import RouteHelper from "../components/RouteHelper.vue"
import { useDocUnitsStore } from "../store"

const store = useDocUnitsStore()
</script>

<template>
  <RouteHelper />
  <span v-if="store.hasSelected()">
    <v-row>
      <v-col class="mb-4">
        <h2>DocUnit {{ store.getSelectedSafe().id }}</h2>
      </v-col>
    </v-row>
    <RisStammDaten />
  </span>
</template>
