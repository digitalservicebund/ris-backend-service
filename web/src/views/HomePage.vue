<script lang="ts" setup>
import DocUnitList from "../components/doc-unit-list/DocUnitList.vue"
import HelloWorld from "../components/hello-world/HelloWorld.vue"
</script>

<template>
  <v-row>
    <HelloWorld></HelloWorld>
  </v-row>
  <v-row class="text-center">
    <v-col class="mb-4">
      <DocUnitList />
    </v-col>
  </v-row>
</template>
