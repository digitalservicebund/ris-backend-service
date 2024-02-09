<script lang="ts" setup>
import { ref } from "vue"
import ChipsDateInput from "@/components/input/ChipsDateInput.vue"
import ChipsInput from "@/components/input/ChipsInput.vue"
import InputField from "@/components/input/InputField.vue"
import ChipsInputBottom from "@/components/input/KeywordsChipsInput.vue"
import type { ChipsInputModelType } from "@/components/input/types"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"

const inlineChips = ref<ChipsInputModelType>(["one", "two"])
const stackedChips = ref<ChipsInputModelType>(["one", "two"])
const dateChips = ref<ChipsInputModelType>(["2023-07-20", "2023-07-21"])
</script>

<template>
  <KitchensinkPage name="Chips">
    <KitchensinkStory class="w-384" name="Inline">
      <InputField id="inlineChipsInput" v-slot="{ id }" label="Inline chips">
        <ChipsInput
          :id="id"
          v-model="inlineChips"
          aria-label="Inline chips input"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory class="w-384" name="Stacked">
      <InputField id="stackedChipsInput" v-slot="{ id }" label="Stacked chips">
        <ChipsInputBottom
          :id="id"
          v-model="stackedChips"
          aria-label="Stacked chips input"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory class="w-384" name="With date">
      <InputField id="dateChipsInput" v-slot="{ id }" label="Date chips">
        <ChipsDateInput
          :id="id"
          v-model="dateChips"
          aria-label="Date chips input"
        />
      </InputField>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
