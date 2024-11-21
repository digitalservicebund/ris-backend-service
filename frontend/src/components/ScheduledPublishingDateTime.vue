<script setup lang="ts">
import dayjs from "dayjs"
import customParseFormat from "dayjs/plugin/customParseFormat"
import dayjsTimezone from "dayjs/plugin/timezone"
import dayjsUtc from "dayjs/plugin/utc"
import { computed, ref } from "vue"
import { ValidationError } from "./input/types"
import DateInput from "@/components/input/DateInput.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TimeInput from "@/components/input/TimeInput.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

const { isPublishable } = defineProps<{ isPublishable: boolean }>()

dayjs.extend(dayjsUtc)
dayjs.extend(dayjsTimezone)
dayjs.extend(customParseFormat)

const store = useDocumentUnitStore()

const scheduledPublishingDate = ref<string | undefined>(
  store.documentUnit!.coreData.scheduledPublicationDateTime &&
    dayjs
      .utc(store.documentUnit!.coreData.scheduledPublicationDateTime)
      .tz("Europe/Berlin")
      .format("YYYY-MM-DD"),
)
const scheduledPublishingTime = ref<string | undefined>(
  (store.documentUnit!.coreData.scheduledPublicationDateTime &&
    dayjs
      .utc(store.documentUnit!.coreData.scheduledPublicationDateTime)
      .tz("Europe/Berlin")
      .format("HH:mm")) ||
    "05:00",
)

const scheduledDateTimeInput = computed(() =>
  dayjs.tz(
    scheduledPublishingDate.value + "T" + scheduledPublishingTime.value,
    "Europe/Berlin",
  ),
)

const isScheduled = computed<boolean>(
  () => !!store.documentUnit!.coreData.scheduledPublicationDateTime,
)

const isDateInFuture = computed<boolean>(
  () =>
    !scheduledPublishingDate.value ||
    !scheduledPublishingTime.value ||
    scheduledDateTimeInput.value.isAfter(new Date()),
)

const isDateValid = computed<boolean>(() =>
  dayjs(scheduledPublishingDate.value, "YYYY-MM-DD", true).isValid(),
)

const isTimeValid = computed<boolean>(() =>
  dayjs(scheduledPublishingTime.value, "HH:mm", true).isValid(),
)

const isValidDateTime = computed<boolean>(
  () =>
    isDateInFuture.value &&
    isDateValid.value &&
    isTimeValid.value &&
    !dateValidationError.value,
)

const saveScheduling = async () => {
  if (dateValidationError.value || !isDateValid.value || !isTimeValid.value) {
    // This is needed as the DateInput does not update its modelValue when the date is incomplete.
    // First input correct date "01.01.2080", then delete last char "01.01.208". Without blurring input, click on button.
    return
  }

  store.documentUnit!.coreData.scheduledPublicationDateTime =
    scheduledDateTimeInput.value.toISOString()
  await store.updateDocumentUnit()

  scheduledPublishingDate.value = dayjs(
    store.documentUnit!.coreData.scheduledPublicationDateTime,
  )
    .tz("Europe/Berlin")
    .format("YYYY-MM-DD")
}

const removeScheduling = async () => {
  store.documentUnit!.coreData.scheduledPublicationDateTime = undefined
  await store.updateDocumentUnit()
  scheduledPublishingDate.value = undefined
}

const dateValidationError = ref<ValidationError | undefined>()
</script>

<template>
  <div class="flex flex-col gap-16">
    <p>Oder für später terminieren:</p>
    <div class="flex max-w-640 flex-row items-end gap-8">
      <InputField id="scheduledPublishingDate" label="Datum *">
        <DateInput
          id="publishingDate"
          v-model="scheduledPublishingDate"
          aria-label="Terminiertes Datum"
          class="ds-input-medium"
          :has-error="!!dateValidationError"
          is-future-date
          :read-only="!isPublishable || isScheduled"
          @update:validation-error="(error) => (dateValidationError = error)"
        ></DateInput>
      </InputField>
      <InputField id="publishingTime" label="Uhrzeit *">
        <TimeInput
          id="publishingTime"
          v-model="scheduledPublishingTime"
          aria-label="Terminierte Uhrzeit"
          class="ds-input-medium"
          :read-only="!isPublishable || isScheduled"
        ></TimeInput>
      </InputField>
      <TextButton
        v-if="!isScheduled"
        aria-label="Termin setzen"
        button-type="primary"
        class="w-fit"
        :disabled="!isPublishable || !isValidDateTime"
        label="Termin&nbsp;setzen"
        size="medium"
        @click="saveScheduling"
      />
      <TextButton
        v-if="isScheduled"
        aria-label="Termin löschen"
        button-type="destructive"
        class="w-fit shrink-0"
        label="Termin&nbsp;löschen"
        size="medium"
        @click="removeScheduling"
      />
    </div>
    <div
      v-if="
        !isDateInFuture ||
        dateValidationError ||
        !isTimeValid ||
        (!isPublishable && scheduledPublishingDate)
      "
      class="flex flex-row items-center"
      data-testid="scheduledPublishingDate_errors"
    >
      <IconErrorOutline class="pr-4 text-14 text-red-800" />

      <div class="flex-col">
        <div
          v-if="!isDateInFuture"
          class="lex-row ds-label-03-reg mt-2 text-red-800"
        >
          Der Terminierungszeitpunkt muss in der Zukunft liegen.
        </div>
        <div
          v-if="dateValidationError"
          class="lex-row ds-label-03-reg mt-2 text-red-800"
        >
          {{ dateValidationError.message }}.
        </div>
        <div
          v-if="!isTimeValid"
          class="lex-row ds-label-03-reg mt-2 text-red-800"
        >
          Unvollständige Uhrzeit.
        </div>
        <div
          v-if="!isPublishable && scheduledPublishingDate"
          class="lex-row ds-label-03-reg mt-2 text-red-800"
        >
          Die terminierte Abgabe kann aufgrund von Fehlern in der
          Plausibilitätsprüfung nicht durchgeführt werden.
        </div>
      </div>
    </div>
  </div>
</template>
