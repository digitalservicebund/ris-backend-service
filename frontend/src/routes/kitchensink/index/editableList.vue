<script lang="ts" setup>
import { ref } from "vue"
import EditableList from "@/components/EditableListCaselaw.vue"
import EditableListItem from "@/domain/editableListItem"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

const listWithEntries = ref<EditableListItem[]>([
  {
    isReadOnly: false,
    hasForeignSource: false,
    isEmpty: false,
    renderDecision: "Foo",
    hasMissingRequiredFields: false,
    missingRequiredFields: [],
    showSummaryOnEdit: false,
  },
  {
    isReadOnly: false,
    hasForeignSource: false,
    isEmpty: false,
    renderDecision: "Foo",
    hasMissingRequiredFields: false,
    missingRequiredFields: [],
    showSummaryOnEdit: false,
  },
])
const emptyList = ref([])
</script>

<template>
  <KitchensinkPage name="Edtiable list">
    <KitchensinkStory name="With entries">
      <EditableList
        v-model="listWithEntries"
        :default-value="listWithEntries[0]"
        :edit-component="TextInput"
      />
    </KitchensinkStory>

    <KitchensinkStory name="Without entries">
      <EditableList
        v-model="emptyList"
        :default-value="listWithEntries[0]"
        :edit-component="TextInput"
        transformer=""
      />
    </KitchensinkStory>
  </KitchensinkPage>
</template>
