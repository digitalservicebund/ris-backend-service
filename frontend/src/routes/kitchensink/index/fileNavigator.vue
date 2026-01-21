<script setup lang="ts">
import { computed, ref } from "vue"
import FileNavigator from "@/components/FileNavigator.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"

const fileIndex = ref(0)

const files = computed(() => {
  return [
    {
      id: "123",
      name: "first.docx",
      format: "docx",
      uploadTimestamp: "2024-06-01T12:00:00Z",
    },
    {
      id: "456",
      name: "second.docx",
      format: "docx",
      uploadTimestamp: "2024-06-01T12:00:00Z",
    },
  ]
})

const handleOnSelect = (index: number) => {
  fileIndex.value = index
}
</script>

<template>
  <KitchensinkPage name="File Navigator">
    <FileNavigator
      :attachments="files"
      :current-index="fileIndex"
      @select="handleOnSelect"
    ></FileNavigator>
  </KitchensinkPage>
</template>
