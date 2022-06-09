<script lang="ts" setup>
import DocUnitList from "../components/doc-unit-list/DocUnitList.vue"
import RisButton from "../components/ris-button/RisButton.vue"
</script>

<template>
  <v-row>
    <v-col></v-col>
  </v-row>
  <v-row class="text-center">
    <v-col>
      <RisButton label="Neue Dokumentationseinheit" />
    </v-col>
  </v-row>
  <v-row class="text-center">
    <v-col class="mb-4">
      <DocUnitList />
    </v-col>
  </v-row>
</template>
