<script lang="ts" setup>
import { onMounted, ref } from "vue"
import { useRoute } from "vue-router"
import RisStammDaten from "../components/ris-stammdaten/RisStammDaten.vue"

const route = useRoute()
const docUnitId = ref()

onMounted(() => {
  docUnitId.value = route.params.id
})
</script>

<template>
  DocUnit {{ docUnitId }}
  <v-row>
    <v-col class="mb-4">
      <h2>RandomNumber123</h2>
    </v-col>
  </v-row>
  <RisStammDaten />
</template>
