<script lang="ts" setup>
import KitchensinkNavbar from "@/kitchensink/components/KitchensinkNavbar.vue"
import type { NormComponent } from "@/kitchensink/types/types"

defineProps<{
  components: NormComponent[]
}>()
</script>

<template>
  <!-- Header -->
  <div
    class="border-b-4 flex flex-nowrap flex-row items-center justify-center w-full"
  >
    <div
      class="flex flex-col flex-nowrap gap-y-10 justify-start overflow-hidden py-10 relative w-2/3"
    >
      <!-- Title -->
      <h1 class="font-bold text-64">Kitchensink</h1>
      <!-- Navbar -->
      <KitchensinkNavbar class="relative" :components="components" />
    </div>
  </div>
</template>
