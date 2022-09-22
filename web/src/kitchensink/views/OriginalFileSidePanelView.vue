<script lang="ts" setup>
import { ref } from "vue"
import { useRoute } from "vue-router"
import OriginalFileSidePanel from "@/components/OriginalFileSidePanel.vue"

const showDocPanel1 = ref(false)
const showDocPanel2 = ref(false)
const showDocPanel3 = ref(false)
const hasFile = ref(true)
const route = useRoute()
route.params["documentNumber"] = "AABB2022000057"
const handleToggleFilePanel1 = () => {
  showDocPanel1.value = !showDocPanel1.value
}
const handleToggleFilePanel2 = () => {
  showDocPanel2.value = !showDocPanel2.value
}
const handleToggleFilePanel3 = () => {
  showDocPanel3.value = !showDocPanel3.value
}
</script>

<template>
  <div class="flex flex-row flex-wrap h-max justify-between pb-4 w-full">
    <div class="flex flex-col">
      <span class="text-20">File has been loaded</span>
      <OriginalFileSidePanel
        file="This is file content"
        :has-file="hasFile"
        :open="showDocPanel1"
        @toggle-panel="handleToggleFilePanel1"
      />
    </div>
    <div class="flex flex-col min-h-[300px]">
      <span class="text-20">File is loading</span>
      <OriginalFileSidePanel
        :has-file="hasFile"
        :open="showDocPanel2"
        @toggle-panel="handleToggleFilePanel2"
      />
    </div>
    <div class="flex flex-col min-h-[300px]">
      <span class="text-20">File has not been uploaded yet</span>
      <OriginalFileSidePanel
        :has-file="!hasFile"
        :open="showDocPanel3"
        @toggle-panel="handleToggleFilePanel3"
      />
    </div>
  </div>
</template>
