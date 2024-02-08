<script lang="ts" setup>
import { computed, ref } from "vue"
import CheckboxInput from "@/components/CheckboxInput.vue"
import InputField from "@/components/InputField.vue"
import { LabelPosition } from "@/components/utils/enumLabelPosition"
import { ValidationError } from "@/components/utils/types"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"

const showValidationErrors = ref(false)

const validationError = computed<ValidationError | undefined>(() =>
  showValidationErrors.value
    ? { message: "Something went wrong", instance: "test" }
    : undefined,
)
</script>

<template>
  <KitchensinkPage name="Input field">
    <KitchensinkStory name="Properties">
      <InputField
        id="defaultInputField"
        label="Show validation errors"
        :label-position="LabelPosition.RIGHT"
      >
        <CheckboxInput v-model="showValidationErrors" />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Label at the top">
      <InputField
        id="topLabelInputField"
        label="Label at the top"
        :label-position="LabelPosition.TOP"
        :validation-error="validationError"
      >
        <span class="h-48 w-48 rounded bg-blue-300"></span>
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Label on the right">
      <InputField
        id="rightLabelInputField"
        label="Label on the right"
        :label-position="LabelPosition.RIGHT"
        :validation-error="validationError"
      >
        <span class="h-48 w-48 rounded bg-blue-300"></span>
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="No label">
      <InputField
        id="noLabelInputField"
        label=""
        :validation-error="validationError"
      >
        <span class="h-48 w-48 rounded bg-blue-300"></span>
      </InputField>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
