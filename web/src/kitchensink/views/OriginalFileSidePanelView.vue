<script lang="ts" setup>
import { ref } from "vue"
import { useRoute } from "vue-router"
import OriginalFileSidePanel from "@/components/OriginalFileSidePanel.vue"

const route = useRoute()
const hasFile = ref(true)
const isOpen = ref(true)
route.params["documentNumber"] = "AABB2022000057"
</script>

<template>
  <div
    class="flex flex-row flex-wrap gap-[2rem] grow h-max justify-end pb-4 w-full"
  >
    <div class="flex flex-col min-h-[300px]">
      <OriginalFileSidePanel
        id="odoc-panel-element"
        class="bg-white grow p-[2rem] pl-40 w-full"
        file="This is some file content"
        :has-file="hasFile"
        :open="isOpen"
      />
    </div>
    <div class="flex flex-col min-h-[300px]">
      <OriginalFileSidePanel
        id="odoc-panel-element"
        class="bg-white grow p-[2rem] pl-40 w-full"
        file="This is some file content"
        :has-file="!hasFile"
        :open="isOpen"
      />
    </div>
  </div>
</template>
