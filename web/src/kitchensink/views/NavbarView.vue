<script lang="ts" setup>
import { ref } from "vue"
import NavbarSide from "@/components/NavbarSide.vue"
import Navbar from "@/components/NavbarTop.vue"

const showNavBar = ref(false)
const handleToggleNavBar = () => {
  showNavBar.value = !showNavBar.value
}
</script>

<template>
  <div class="flex flex-col gap-y-20 items-start">
    <h1 class="font-bold text-32">Navbar Top</h1>
    <div class="min-w-full">
      <Navbar></Navbar>
    </div>
    <h1 class="font-bold text-32">Navbar Side</h1>
    <div class="min-h-[200px] pb-4">
      <NavbarSide
        document-number="AABB20220051"
        :visible="showNavBar"
        @toggle-navbar="handleToggleNavBar"
      />
    </div>
  </div>
</template>
