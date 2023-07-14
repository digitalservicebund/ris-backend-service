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
    class="flex h-max w-full grow flex-row flex-wrap justify-end gap-[2rem] pb-4"
  >
    <div class="flex min-h-[300px] flex-col">
      <OriginalFileSidePanel
        id="odoc-panel-element"
        class="w-full grow bg-white p-[2rem] pl-40"
        file="This is some file content"
        :has-file="hasFile"
        :open="isOpen"
      />
    </div>
    <div class="flex min-h-[300px] flex-col">
      <OriginalFileSidePanel
        id="odoc-panel-element"
        class="w-full grow bg-white p-[2rem] pl-40"
        file="This is some file content"
        :has-file="!hasFile"
        :open="isOpen"
      />
    </div>
  </div>
</template>
