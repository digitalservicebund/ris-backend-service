<script lang="ts" setup>
import { ref } from "vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
import ChipsDateInput from "@/shared/components/input/ChipsDateInput.vue"
import ChipsInput from "@/shared/components/input/ChipsInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import ChipsInputBottom from "@/shared/components/input/KeywordsChipsInput.vue"
import type { ChipsInputModelType } from "@/shared/components/input/types"

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
          aria-label="Inline chips input"
          :model-value="inlineChips"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory class="w-384" name="Stacked">
      <InputField id="stackedChipsInput" v-slot="{ id }" label="Stacked chips">
        <ChipsInputBottom
          :id="id"
          aria-label="Stacked chips input"
          :model-value="stackedChips"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory class="w-384" name="With date">
      <InputField id="dateChipsInput" v-slot="{ id }" label="Date chips">
        <ChipsDateInput
          :id="id"
          aria-label="Date chips input"
          :model-value="dateChips"
        />
      </InputField>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
