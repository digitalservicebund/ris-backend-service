<script lang="ts" setup>
import { onMounted } from "vue"
import { useRouter } from "vue-router"
import { createNewDocUnit } from "../api"
import DocUnitList from "../components/DocUnitList.vue"
import SimpleButton from "../components/SimpleButton.vue"
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
      <v-col class="rechtsprechung-header"><h1>Rechtsprechung</h1></v-col>
    </v-row>
    <v-row class="text-right">
      <v-col>
        <SimpleButton label="Neue Dokumentationseinheit" @click="onSubmit" />
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

<style lang="scss"></style>
