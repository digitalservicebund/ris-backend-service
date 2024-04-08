<script lang="ts" setup>
import { ref } from "vue"
import { useRoute } from "vue-router"
import OriginalFileSidePanel from "@/components/FilePreview.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"

const route = useRoute()
const hasFile = ref(true)
const isOpen = ref(true)
route.params["documentNumber"] = "AABB2022000057"
</script>

<template>
  <KitchensinkPage name="Original file side panel">
    <KitchensinkStory name="Has file">
      <OriginalFileSidePanel
        id="odoc-panel-element"
        class="w-full grow bg-white p-[2rem] pl-40"
        content="This is some file content"
        :has-file="hasFile"
        :open="isOpen"
      />
    </KitchensinkStory>

    <KitchensinkStory name="Doesn't have file">
      <OriginalFileSidePanel
        id="odoc-panel-element"
        class="w-full grow bg-white p-[2rem] pl-40"
        content="This is some file content"
        :has-file="!hasFile"
        :open="isOpen"
      />
    </KitchensinkStory>
  </KitchensinkPage>
</template>
