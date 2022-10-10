<script lang="ts" setup>
import { useRoute } from "vue-router"

const route = useRoute()
</script>

<template>
  <div class="flex items-center justify-between p-24" role="navigation">
    <div class="flex gap-44 items-center">
      <div class="flex items-center">
        <div class="bg-yellow-500 h-56 rounded-full w-56" />

        <span class="font-bold leading-20 px-[1.333rem] text-16">
          Rechtsinformationssystem<br />
          des Bundes [Platzhalter]
        </span>
      </div>

      <router-link
        class="hover:bg-yellow-500 hover:underline p-8"
        :class="{ underline: route.path.includes('jurisdiction') }"
        :to="{ name: 'jurisdiction' }"
        >Rechtsprechung</router-link
      >
    </div>

    <div class="flex font-bold gap-8 items-center">
      <span class="material-icons"> perm_identity </span>
      User Name
    </div>
  </div>
</template>
