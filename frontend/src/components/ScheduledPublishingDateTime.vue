<script setup lang="ts">
import dayjs from "dayjs"
import customParseFormat from "dayjs/plugin/customParseFormat"
import dayjsTimezone from "dayjs/plugin/timezone"
import dayjsUtc from "dayjs/plugin/utc"
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import InputMask from "primevue/inputmask"
import Message from "primevue/message"
import { computed, Ref, ref } from "vue"
import { ValidationError } from "./input/types"
import DateInput from "@/components/input/DateInput.vue"
import InputField from "@/components/input/InputField.vue"
import { DocumentationUnit } from "@/domain/documentationUnit"
import { ResponseError } from "@/services/httpClient"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import useSessionStore from "@/stores/sessionStore"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

const { isPublishable } = defineProps<{ isPublishable: boolean }>()

dayjs.extend(dayjsUtc)
dayjs.extend(dayjsTimezone)
dayjs.extend(customParseFormat)

const store = useDocumentUnitStore()
const { documentUnit } = storeToRefs(store) as {
  documentUnit: Ref<DocumentationUnit | undefined>
}
const sessionStore = useSessionStore()

const storedScheduledPublicationDateTime = computed({
  get: () => documentUnit.value!.managementData.scheduledPublicationDateTime,
  set: (newDate?: string) =>
    (documentUnit.value!.managementData.scheduledPublicationDateTime = newDate),
})

// initialize local values with stored date-time
/* eslint-disable vue/no-ref-object-reactivity-loss */
const scheduledPublishingDate = ref<string | undefined>(
  storedScheduledPublicationDateTime.value &&
    dayjs
      .utc(storedScheduledPublicationDateTime.value)
      .tz("Europe/Berlin")
      .format("YYYY-MM-DD"),
)
const scheduledPublishingTime = ref<string | undefined>(
  (storedScheduledPublicationDateTime.value &&
    dayjs
      .utc(storedScheduledPublicationDateTime.value)
      .tz("Europe/Berlin")
      .format("HH:mm")) ||
    "05:00",
)
/* eslint-enable vue/no-ref-object-reactivity-loss */

const scheduledDateTimeInput = computed(() =>
  isDateValid.value && isTimeValid.value
    ? dayjs.tz(
        scheduledPublishingDate.value + "T" + scheduledPublishingTime.value,
        "Europe/Berlin",
      )
    : undefined,
)

const isScheduled = computed<boolean>(
  () => !!storedScheduledPublicationDateTime.value,
)

const isDateInFuture = computed<boolean>(
  () =>
    !scheduledPublishingDate.value ||
    !scheduledPublishingTime.value ||
    !scheduledDateTimeInput.value ||
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

const docUnitSaveError = ref<ResponseError | null>(null)

const isSaving = ref(false)
const saveScheduling = async () => {
  docUnitSaveError.value = null
  isSaving.value = true
  if (dateValidationError.value || !isDateValid.value || !isTimeValid.value) {
    // This is needed as the DateInput does not update its modelValue when the date is incomplete.
    // First input correct date "01.01.2080", then delete last char "01.01.208". Without blurring input, click on button.
    return
  }

  storedScheduledPublicationDateTime.value =
    scheduledDateTimeInput.value?.toISOString()

  documentUnit.value!.managementData.scheduledByEmail = sessionStore.user?.email

  const { error } = await store.updateDocumentUnit()
  isSaving.value = false

  if (error) {
    storedScheduledPublicationDateTime.value = undefined
    docUnitSaveError.value = error
  } else {
    scheduledPublishingDate.value = dayjs(
      storedScheduledPublicationDateTime.value,
    )
      .tz("Europe/Berlin")
      .format("YYYY-MM-DD")
  }
}

const removeScheduling = async () => {
  isSaving.value = true
  docUnitSaveError.value = null
  const previousDate = storedScheduledPublicationDateTime.value
  storedScheduledPublicationDateTime.value = undefined
  documentUnit.value!.managementData.scheduledByEmail = undefined

  const { error } = await store.updateDocumentUnit()
  isSaving.value = false

  if (error) {
    storedScheduledPublicationDateTime.value = previousDate
    docUnitSaveError.value = error
  } else {
    scheduledPublishingDate.value = undefined
    scheduledPublishingTime.value = "05:00"
  }
}

const dateValidationError = ref<ValidationError | undefined>()
</script>

<template>
  <div class="flex flex-col gap-16">
    <p>Oder für später terminieren:</p>
    <div class="flex max-w-[640px] flex-row items-end gap-8">
      <InputField id="scheduledPublishingDate" label="Datum *">
        <DateInput
          id="publishingDate"
          v-model="scheduledPublishingDate"
          aria-label="Terminiertes Datum"
          :disabled="!isPublishable"
          :has-error="!!dateValidationError"
          is-future-date
          :read-only="isScheduled"
          @update:validation-error="(error) => (dateValidationError = error)"
        ></DateInput>
      </InputField>
      <InputField id="publishingTime" label="Uhrzeit *">
        <InputMask
          id="publishingTime"
          v-model="scheduledPublishingTime"
          aria-label="Terminierte Uhrzeit"
          class="w-full"
          :disabled="!isPublishable"
          :invalid="!isTimeValid"
          mask="99:99"
          placeholder="HH:MM"
          :readonly="isScheduled"
        ></InputMask>
      </InputField>
      <Button
        v-if="(!isScheduled && !isSaving) || (isScheduled && isSaving)"
        aria-label="Termin setzen"
        class="mb-4 w-fit"
        :disabled="!isPublishable || !isValidDateTime || isSaving"
        label="Termin&nbsp;setzen"
        :loading="isSaving"
        size="small"
        @click="saveScheduling"
      ></Button>
      <Button
        v-if="(isScheduled && !isSaving) || (!isScheduled && isSaving)"
        aria-label="Termin löschen"
        class="mb-4 w-fit shrink-0"
        :disabled="isSaving"
        label="Termin&nbsp;löschen"
        :loading="isSaving"
        severity="danger"
        size="small"
        @click="removeScheduling"
      ></Button>
    </div>
    <div
      v-if="
        (!isScheduled && !isDateInFuture) ||
        dateValidationError ||
        !isTimeValid ||
        (!isPublishable && scheduledPublishingDate)
      "
      class="flex flex-row items-center"
      data-testid="scheduledPublishingDate_errors"
    >
      <IconErrorOutline class="pr-4 text-red-800" />

      <div class="flex-col">
        <div
          v-if="!isDateInFuture"
          class="lex-row ris-label3-regular mt-2 text-red-800"
        >
          Der Terminierungszeitpunkt muss in der Zukunft liegen.
        </div>
        <div
          v-if="dateValidationError"
          class="lex-row ris-label3-regular mt-2 text-red-800"
        >
          {{ dateValidationError.message }}.
        </div>
        <div
          v-if="!isTimeValid"
          class="lex-row ris-label3-regular mt-2 text-red-800"
        >
          Unvollständige Uhrzeit.
        </div>
        <div
          v-if="!isPublishable && scheduledPublishingDate"
          class="lex-row ris-label3-regular mt-2 text-red-800"
        >
          Die terminierte Abgabe wird aufgrund von Fehlern in der
          Plausibilitätsprüfung fehlschlagen.
        </div>
      </div>
    </div>
    <Message
      v-if="docUnitSaveError"
      aria-label="Fehler bei der Terminierung"
      class="mt-8"
      severity="error"
    >
      <p class="ris-body1-bold">{{ docUnitSaveError.title }}</p>
      <p>{{ docUnitSaveError.description }}</p>
    </Message>
  </div>
</template>
