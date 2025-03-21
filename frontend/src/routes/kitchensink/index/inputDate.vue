<script lang="ts" setup>
import { reactive } from "vue"
import { InfoStatus } from "@/components/enumInfoStatus"
import InfoModal from "@/components/InfoModal.vue"
import DateInput from "@/components/input/DateInput.vue"
import InputField from "@/components/input/InputField.vue"
import TimeInput from "@/components/input/TimeInput.vue"
import YearInput from "@/components/input/YearInput.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"

const values = reactive({
  regularTextInput: "",
  textInputWithPlaceholder: "",
  textInputWithValue: "Hello world",
  invalidTextInput: "Invalid value",
  readonlyTextInput: "Read-only",
  mediumTextInput: "Large text input",
  dateInput: "",
  yearInput: "",
  timeInput: "",
})
</script>

<template>
  <KitchensinkPage name="Input Date">
    <InfoModal
      description="In order to ensure a consistent layout for inputs, the actual input field must always be wrapped with an InputField containing the label and (if necessary) the error message."
      :status="InfoStatus.INFO"
      title="Code convention for usage of inputs"
    />

    <KitchensinkStory name="Date, year, time">
      <InputField
        id="dateInput"
        v-slot="{ id, hasError, updateValidationError }"
        label="Date input"
      >
        <DateInput
          :id="id"
          v-model="values.dateInput"
          aria-label="Date input"
          :has-error="hasError"
          @update:validation-error="updateValidationError"
        />
      </InputField>

      <InputField
        id="yearInput"
        v-slot="{ id, hasError, updateValidationError }"
        label="Year input"
      >
        <YearInput
          :id="id"
          v-model="values.yearInput"
          :has-error="hasError"
          @update:validation-error="updateValidationError"
        />
      </InputField>
      <div class="mb-12">
        <span class="ds-label-03-bold mr-8">Current year value:</span>
        <span class="ds-label-03-reg">{{ values.yearInput }}</span>
      </div>

      <InputField id="timeInput" v-slot="{ id }" label="Time input">
        <TimeInput :id="id" v-model="values.timeInput" />
      </InputField>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
