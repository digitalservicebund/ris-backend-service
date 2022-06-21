<script lang="ts" setup>
import { onMounted } from "vue"
import { useRouter } from "vue-router"

const router = useRouter()

onMounted(() => {
  router.push({ name: "Rechtsprechung" })
})
</script>

<template>
  <v-row class="text-center">
    <v-col>
      <router-link :to="{ name: 'Rechtsprechung' }">Rechtsprechung</router-link>
    </v-col>
  </v-row>
</template>
