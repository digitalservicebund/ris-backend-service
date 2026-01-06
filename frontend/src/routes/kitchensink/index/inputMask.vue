<script lang="ts" setup>
import InputMask from "primevue/inputmask"
import Message from "primevue/message"
import { reactive } from "vue"
import DateInput from "@/components/input/DateInput.vue"
import InputField from "@/components/input/InputField.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"

const values = reactive({
  regularTextInput: "",
  textInputWithPlaceholder: "",
  textInputWithValue: "Hello world",
  invalidTextInput: "Invalid value",
  readonlyTextInput: "Read-only",
  mediumTextInput: "Large text input",
  date: "",
  dateInFuture: "02.01.2080",
  yearInput: "",
  timeInput: "",
})

const validationStore = useValidationStore<["date", "dateInFuture"][number]>()
</script>

<template>
  <KitchensinkPage name="Input Mask">
    <Message severity="info">
      <p class="ris-body1-bold">Code convention for usage of inputs</p>
      <p>
        In order to ensure a consistent layout for inputs, the actual input
        field must always be wrapped with an InputField containing the label and
        (if necessary) the error message.
      </p>
    </Message>

    <KitchensinkStory name="Date, year, time">
      <InputField
        id="date"
        v-slot="slotProps"
        label="Datum"
        :validation-error="validationStore.getByField('date')"
      >
        <DateInput
          :id="slotProps.id"
          v-model="values.date"
          aria-label="Datum"
          :has-error="slotProps.hasError"
          @focus="validationStore.remove('date')"
          @update:validation-error="slotProps.updateValidationError"
        ></DateInput>
      </InputField>

      <InputField
        id="dateInFuture"
        v-slot="slotProps"
        label="Fehlerhaftes Datum"
        :validation-error="validationStore.getByField('dateInFuture')"
      >
        <DateInput
          :id="slotProps.id"
          v-model="values.dateInFuture"
          aria-label="Datum"
          :has-error="slotProps.hasError"
          @focus="validationStore.remove('dateInFuture')"
          @update:validation-error="slotProps.updateValidationError"
        ></DateInput>
      </InputField>

      <InputField id="yearInput" v-slot="{ id, hasError }" label="Year input">
        <InputMask
          :id="id"
          v-model="values.yearInput"
          fluid
          :invalid="hasError"
          mask="9999"
          placeholder="JJJJ"
        />
      </InputField>

      <InputField id="timeInput" v-slot="{ id }" label="Time input">
        <InputMask
          :id="id"
          v-model="values.timeInput"
          aria-label="Time input"
          fluid
          mask="99:99"
          placeholder="HH:MM"
        ></InputMask>
      </InputField>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
