<script lang="ts" setup>
import { onMounted, ref } from "vue"
import { useRoute } from "vue-router"
import RisStammDaten from "../components/ris-stammdaten/RisStammDaten.vue"
import { useDocUnitsStore } from "../store"
import { DocUnit } from "../types/DocUnit"

const docUnitsStore = useDocUnitsStore()
const route = useRoute()
const docUnit = ref<DocUnit>()

onMounted(() => {
  docUnitsStore.getDocUnit(Number(route.params.id)).then((du) => {
    docUnit.value = du
  })
})
</script>

<template>
  <span v-if="docUnit">
    <v-row>
      <v-col class="mb-4">
        <h2>DocUnit {{ docUnit.id }}</h2>
      </v-col>
    </v-row>
    <RisStammDaten />
  </span>
  <span v-else> Dokumentationseinheit wird geladen... </span>
</template>
