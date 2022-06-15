<script lang="ts" setup>
import { onMounted } from "vue"
import { useRouter } from "vue-router"
import HelloWorld from "../components/hello-world/HelloWorld.vue"

const router = useRouter()

onMounted(() => {
  router.push({ name: "Rechtsprechung" })
})
</script>

<template>
  <v-row>
    <HelloWorld></HelloWorld>
  </v-row>
  <v-row class="text-center">
    <v-col>
      <router-link :to="{ name: 'Rechtsprechung' }">Rechtsprechung</router-link>
    </v-col>
  </v-row>
</template>
