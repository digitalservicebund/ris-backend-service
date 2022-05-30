<script lang="ts" setup>
import { onMounted, ref } from "vue"
import { useRoute } from "vue-router"
import RisStammDaten from "../components/ris-stammdaten/RisStammDaten.vue"
import { useDocUnitsStore } from "../store"
import { DocUnit } from "../types/DocUnit"

const docUnitsStore = useDocUnitsStore()
const route = useRoute()
const docUnitRef = ref<DocUnit>()

onMounted(() => {
  docUnitsStore.getDocUnit(Number(route.params.id)).then((du) => {
    docUnitRef.value = du
  })
})
</script>

<template>
  <span v-if="docUnitRef">
    <v-row>
      <v-col class="mb-4">
        <h2>DocUnit {{ docUnitRef.id }}</h2>
      </v-col>
    </v-row>
    <RisStammDaten :doc-unit="docUnitRef" />
  </span>
  <span v-else> Dokumentationseinheit wird geladen... </span>
</template>
