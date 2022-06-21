<script lang="ts" setup>
import DocUnitCoreData from "../components/DocUnitCoreData.vue"
import RouteHelper from "../components/RouteHelper.vue"
import TextInput from "../components/TextInput.vue"
import { useDocUnitsStore } from "../store"

const store = useDocUnitsStore()
</script>

<template>
  <RouteHelper />
  <span v-if="store.hasSelected()">
    <DocUnitCoreData id="stammdaten" />
    <TextInput id="kurzUndLangtexte" />
  </span>
</template>
