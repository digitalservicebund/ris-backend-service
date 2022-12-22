<script lang="ts" setup>
import { useRoute } from "vue-router"
import Logo from "../assets/neuRIS-logo.svg"

const route = useRoute()
</script>

<template>
  <nav
    class="border-gray-400 border-y flex items-center justify-between px-16 py-24"
  >
    <div class="flex gap-44 items-center">
      <div class="flex items-center">
        <img alt="Neuris Logo" :src="Logo" />
        <span class="leading-20 px-[1rem] text-16">
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
  </nav>
</template>
