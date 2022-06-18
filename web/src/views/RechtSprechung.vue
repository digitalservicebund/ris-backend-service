<script lang="ts" setup>
import { onMounted } from "vue"
import { useRouter } from "vue-router"
import { createNewDocUnit } from "../api"
import DocUnitList from "../components/doc-unit-list/DocUnitList.vue"
import RisButton from "../components/ris-button/RisButton.vue"
import { useDocUnitsStore } from "../store"

const store = useDocUnitsStore()
const router = useRouter()

const onSubmit = () => {
  createNewDocUnit().then((docUnit) => {
    store.add(docUnit)
    router.push({ name: "Dokumente", params: { id: docUnit.id } })
  })
}

onMounted(() => {
  store.clearSelected()
})
</script>

<template>
  <v-container fluid class="rechtsprechung-main">
    <v-row>
      <v-col> </v-col>
    </v-row>
    <v-row>
      <v-col class="rechtsprechung-header"> Rechtsprechung </v-col>
    </v-row>
    <v-row class="text-right">
      <v-col>
        <RisButton label="Neue Dokumentationseinheit" @click="onSubmit" />
      </v-col>
    </v-row>
    <v-row>
      <v-col> </v-col>
    </v-row>
    <v-row class="text-center">
      <v-col class="mb-4">
        <DocUnitList />
      </v-col>
    </v-row>
  </v-container>
</template>

<style lang="scss">
.rechtsprechung-header {
  font-size: x-large;
}
</style>
