<script lang="ts" setup>
import { RisChipsInput } from "@digitalservicebund/ris-ui/components"
import { ref } from "vue"
import InputField from "@/components/input/InputField.vue"
import type { ChipsInputModelType } from "@/components/input/types"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"

const inlineChips = ref<ChipsInputModelType>(["one", "two"])
const dateChips = ref<ChipsInputModelType>(["20.07.2023", "21.07.2023"])
const yearChips = ref<ChipsInputModelType>(["2022", "2023"])
</script>

<template>
  <KitchensinkPage name="Chips Inputs">
    <KitchensinkStory class="w-384" name="Inline">
      <InputField id="inlineChipsInput" v-slot="{ id }" label="Inline chips">
        <RisChipsInput
          :id="id"
          v-model="inlineChips"
          aria-label="Inline chips input"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory class="w-384" name="With date">
      <InputField id="dateChipsInput" v-slot="{ id }" label="Date chips">
        <RisChipsInput
          :id="id"
          v-model="dateChips"
          aria-label="Date chips input"
          mask="99.99.9999"
          placeholder="TT.MM.JJJJ"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory class="w-384" name="With year">
      <InputField id="yearChipsInput" v-slot="{ id }" label="Year chips">
        <RisChipsInput
          :id="id"
          v-model="yearChips"
          aria-label="Year chips input"
          mask="9999"
          placeholder="JJJJ"
        />
      </InputField>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
