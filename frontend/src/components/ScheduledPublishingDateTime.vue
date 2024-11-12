<script setup lang="ts">
import dayjs from "dayjs"
import { computed, ref } from "vue"
import DateInput from "@/components/input/DateInput.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TimeInput from "@/components/input/TimeInput.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import IconErrorOutline from "~icons/ic/baseline-error-outline"
import IconWatch from "~icons/ic/outline-watch-later"

const { isPublishable } = defineProps<{ isPublishable: boolean }>()

const store = useDocumentUnitStore()

const scheduledPublishingDate = ref<string | undefined>(
  dayjs(store.documentUnit!.coreData.scheduledPublicationDate).format(
    "YYYY-MM-DD",
  ),
)
const scheduledPublishingTime = ref<string>(
  dayjs(store.documentUnit!.coreData.scheduledPublicationDate).format(
    "HH:mm",
  ) || "05:00",
)

const scheduledDateTime = computed(() =>
  dayjs(scheduledPublishingDate.value + " " + scheduledPublishingTime.value),
)

const isScheduled = computed<boolean>(
  () => !!store.documentUnit!.coreData.scheduledPublicationDate,
)

const isDateInFuture = computed<boolean>(
  () =>
    !scheduledPublishingDate.value ||
    !scheduledPublishingTime.value ||
    scheduledDateTime.value.isAfter(new Date()),
)

const saveScheduling = async () => {
  store.documentUnit!.coreData.scheduledPublicationDate =
    // TODO: time is missing
    scheduledDateTime.value.format("YYYY-MM-DD")
  await store.updateDocumentUnit()
}

const removeScheduling = async () => {
  store.documentUnit!.coreData.scheduledPublicationDate = undefined
  await store.updateDocumentUnit()
  scheduledPublishingDate.value = undefined
}
</script>

<template>
  <div class="flex flex-col gap-8">
    <p>Oder für später terminieren:</p>
    <div class="flex max-w-640 flex-row items-end gap-8">
      <InputField id="scheduledPublishingDate" label="Datum *">
        <DateInput
          id="publishingDate"
          v-model="scheduledPublishingDate"
          aria-label="Terminiertes Datum"
          class="ds-input-medium"
          is-future-date
          :read-only="!isPublishable || isScheduled"
        ></DateInput>
      </InputField>
      <InputField id="publishingTime" label="Uhrzeit *">
        <TimeInput
          id="publishingTime"
          v-model="scheduledPublishingTime"
          aria-label="Entscheidungsdatum"
          class="ds-input-medium"
          :read-only="!isPublishable || isScheduled"
        ></TimeInput>
      </InputField>
      <TextButton
        v-if="!isScheduled"
        aria-label="Terminieren"
        button-type="primary"
        class="w-fit"
        :disabled="
          !isPublishable ||
          !isDateInFuture ||
          !scheduledPublishingDate ||
          !scheduledPublishingTime
        "
        :icon="IconWatch"
        label="Terminieren"
        size="medium"
        @click="saveScheduling"
      />
      <TextButton
        v-if="isScheduled"
        aria-label="Terminierung löschen"
        button-type="primary"
        class="w-fit shrink-0"
        :icon="IconWatch"
        label="Terminierung löschen"
        size="medium"
        @click="removeScheduling"
      />
    </div>
    <div v-if="!isDateInFuture" class="flex flex-row items-center">
      <IconErrorOutline class="pr-4 text-14 text-red-800" />

      <div class="lex-row ds-label-03-reg mt-2 text-red-800">
        Der Terminierungszeitpunkt muss in der Zukunft liegen.
      </div>
    </div>
  </div>
</template>
