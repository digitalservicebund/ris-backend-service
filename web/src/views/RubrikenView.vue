<script lang="ts" setup>
import RisKurzUndLangtexte from "../components/ris-kurzundlangtexte/RisKurzUndLangtexte.vue"
import RisStammDaten from "../components/ris-stammdaten/RisStammDaten.vue"
import RouteHelper from "../components/RouteHelper.vue"
import { useDocUnitsStore } from "../store"

const store = useDocUnitsStore()
</script>

<template>
  <RouteHelper />
  <span v-if="store.hasSelected()">
    <RisStammDaten id="stammdaten" />
    <RisKurzUndLangtexte id="kurzUndLangtexte" />
  </span>
</template>
