<script lang="ts" setup>
import { useRoute } from "vue-router"

const route = useRoute()
</script>

<template>
  <div
    class="border-gray-400 border-y flex items-center justify-between px-[1.3rem] py-24"
    role="navigation"
  >
    <div class="flex gap-44 items-center">
      <div class="flex items-center">
        <div class="bg-yellow-500 h-56 rounded-full w-56" />

        <span class="leading-20 px-[1.333rem] text-16">
          <span class="font-bold"> Rechtsinformationen</span><br />
          des Bundes
        </span>
      </div>

      <router-link
        class="hover:bg-yellow-500 hover:underline p-8"
        :class="{ underline: route.path.includes('caselaw') }"
        :to="{ name: 'caselaw' }"
        >Rechtsprechung</router-link
      >
      <router-link
        class="hover:bg-yellow-500 hover:underline p-8"
        :class="{ underline: route.path.includes('norms') }"
        :to="{ name: 'norms' }"
        >Normen</router-link
      >
    </div>

    <div class="flex font-bold gap-8 items-center">
      <span class="material-icons"> perm_identity </span>
      User Name
    </div>
  </div>
</template>
