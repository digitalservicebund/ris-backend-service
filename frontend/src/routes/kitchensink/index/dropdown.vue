<script lang="ts" setup>
import { ref } from "vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
import DropdownInput from "@/shared/components/input/DropdownInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import type { DropdownItem } from "@/shared/components/input/types"

const emptyDropdown = ref<string>()
const preselectedDropdown = ref<string>("I1")

const dropdownItems: DropdownItem[] = [
  { label: "Dropdown item 1", value: "I1" },
  { label: "Dropdown item 2", value: "I2" },
  { label: "Dropdown item 3", value: "I3" },
]
</script>

<template>
  <KitchensinkPage name="Dropdown">
    <KitchensinkStory name="Empty">
      <InputField id="emptyDropdown" v-slot="{ id }" label="Dropdown">
        <DropdownInput
          :id="id"
          v-model="emptyDropdown"
          aria-label="dropdown input"
          :items="dropdownItems"
          placeholder="Bitte auswählen"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Pre-selected">
      <InputField id="preselectedDropdown" v-slot="{ id }" label="Dropdown">
        <DropdownInput
          :id="id"
          v-model="preselectedDropdown"
          aria-label="dropdown input"
          :items="dropdownItems"
          placeholder="Bitte auswählen"
        />
      </InputField>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
