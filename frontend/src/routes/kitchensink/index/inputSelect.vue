<script lang="ts" setup>
import InputSelect from "primevue/select"
import { ref } from "vue"
import InputField from "@/components/input/InputField.vue"
import type { DropdownItem } from "@/components/input/types"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"

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
        <InputSelect
          :id="id"
          v-model="emptyDropdown"
          aria-label="dropdown input"
          option-label="label"
          option-value="value"
          :options="dropdownItems"
          placeholder="Bitte auswählen"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Pre-selected">
      <InputField id="preselectedDropdown" v-slot="{ id }" label="Dropdown">
        <InputSelect
          :id="id"
          v-model="preselectedDropdown"
          aria-label="dropdown input"
          option-label="label"
          option-value="value"
          :options="dropdownItems"
          placeholder="Bitte auswählen"
        />
      </InputField>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
