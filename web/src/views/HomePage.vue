<script lang="ts" setup>
import HelloWorld from "../components/hello-world/HelloWorld.vue"
</script>

<template>
  <v-row>
    <HelloWorld></HelloWorld>
  </v-row>
  <v-row class="text-center">
    <v-col>
      <router-link :to="{ name: 'Rechtssprechung' }"
        >Rechtssprechung</router-link
      >
    </v-col>
  </v-row>
</template>
