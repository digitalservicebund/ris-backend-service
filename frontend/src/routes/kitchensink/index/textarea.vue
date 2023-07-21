<script setup lang="ts">
import { ref } from "vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextAreaInput from "@/shared/components/input/TextAreaInput.vue"

const fixed = ref("")
const autosize = ref("")
</script>

<template>
  <KitchensinkPage name="Textarea">
    <KitchensinkStory name="Fixed size">
      <InputField id="fixedTextarea" v-slot="{ id }" label="Textarea">
        <TextAreaInput
          :id="id"
          v-model="fixed"
          aria-label="Textarea"
          :rows="3"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Autosizing">
      <InputField id="autosizingTextarea" v-slot="{ id }" label="Textarea">
        <TextAreaInput
          :id="id"
          v-model="autosize"
          aria-label="Textarea"
          autosize
        />
      </InputField>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
