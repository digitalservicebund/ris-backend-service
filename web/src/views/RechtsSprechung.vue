<script lang="ts" setup>
import { onMounted } from "vue"
import { createNewDocUnit } from "../api"
import DocUnitList from "../components/doc-unit-list/DocUnitList.vue"
import RisButton from "../components/ris-button/RisButton.vue"
import { useDocUnitsStore } from "../store"

const store = useDocUnitsStore()

const onSubmit = () => {
  createNewDocUnit().then((docUnit) => {
    store.add(docUnit)
  })
}

onMounted(() => {
  store.clearSelected()
})
</script>

<template>
  <v-row>
    <v-col></v-col>
  </v-row>
  <v-row class="text-center">
    <v-col>
      <RisButton label="Neue Dokumentationseinheit" @click="onSubmit" />
    </v-col>
  </v-row>
  <v-row class="text-center">
    <v-col class="mb-4">
      <DocUnitList />
    </v-col>
  </v-row>
</template>
