<script setup lang="ts">
import { ref } from "vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextAreaInput from "@/shared/components/input/TextAreaInput.vue"
import { ValidationError } from "@/shared/components/input/types"

const validationError = ref<ValidationError>({
  message: "Invalid value",
  instance: "errorMessageTextInput",
})

const fixed = ref("")
const autosize = ref("")
const medium = ref("")
const small = ref("")
const error = ref("")
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

    <KitchensinkStory name="Medium">
      <InputField id="mediumTextarea" v-slot="{ id }" label="Textarea">
        <TextAreaInput
          :id="id"
          v-model="medium"
          aria-label="Textarea"
          size="medium"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Small">
      <InputField id="smallTextarea" v-slot="{ id }" label="Textarea">
        <TextAreaInput
          :id="id"
          v-model="small"
          aria-label="Textarea"
          size="small"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="With error">
      <InputField
        id="errorTextarea"
        v-slot="{ id, hasError, updateValidationError }"
        label="Textarea"
        :validation-error="validationError"
      >
        <TextAreaInput
          :id="id"
          v-model="error"
          aria-label="Textarea"
          :has-error="hasError"
          @update:validation-error="updateValidationError"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Readonly">
      <InputField id="readonlyTextarea" v-slot="{ id }" label="Textarea">
        <TextAreaInput
          :id="id"
          aria-label="Textarea"
          model-value="This is a readonly textarea"
          read-only
        />
      </InputField>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
