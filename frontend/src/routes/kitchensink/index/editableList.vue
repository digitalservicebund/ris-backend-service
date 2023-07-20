<script lang="ts" setup>
import { ref } from "vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
import EditableList from "@/shared/components/EditableList.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

const listWithEntries = ref(["list entry 1", "list entry 2"])
const emptyList = ref([])
const defaultValue = ""
</script>

<template>
  <KitchensinkPage name="Edtiable list">
    <KitchensinkStory name="With entries">
      <EditableList
        v-model="listWithEntries"
        :default-value="defaultValue"
        :edit-component="TextInput"
      />
    </KitchensinkStory>

    <KitchensinkStory name="Without entries">
      <EditableList
        v-model="emptyList"
        :default-value="defaultValue"
        :edit-component="TextInput"
        transformer=""
      />
    </KitchensinkStory>
  </KitchensinkPage>
</template>
