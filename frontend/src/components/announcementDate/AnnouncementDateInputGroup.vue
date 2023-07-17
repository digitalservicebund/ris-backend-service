<script lang="ts" setup>
import { computed } from "vue"
import { Metadata } from "@/domain/Norm"
import InputElement from "@/shared/components/input/InputElement.vue"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import RadioInput from "@/shared/components/input/RadioInput.vue"
import { InputType } from "@/shared/components/input/types"

interface Props {
  modelValue: Metadata
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value: Metadata]
}>()

const selectedInputType = computed({
  get: () => (props.modelValue.YEAR ? InputType.YEAR : InputType.DATE_TIME),
  set: (value) => {
    emit(
      "update:modelValue",
      value === InputType.DATE_TIME ? { DATE: [], TIME: [] } : { YEAR: [] },
    )
  },
})

const dateValue = computed({
  get: () => props.modelValue.DATE?.[0] ?? "",
  set: (value) => {
    const next: Metadata = {
      DATE: value ? [value] : undefined,
      TIME: props.modelValue.TIME,
    }
    emit("update:modelValue", next)
  },
})

const timeValue = computed({
  get: () => props.modelValue.TIME?.[0] ?? "",
  set: (value) => {
    const next: Metadata = {
      TIME: value ? [value] : undefined,
      DATE: props.modelValue.DATE,
    }
    emit("update:modelValue", next)
  },
})

const yearValue = computed({
  get: () => props.modelValue.YEAR?.[0] ?? "",
  set: (value) => {
    const next: Metadata = { YEAR: value ? [value] : [] }
    emit("update:modelValue", next)
  },
})
</script>

<template>
  <div>
    <div class="mb-8 flex gap-96">
      <div>
        <InputField
          id="announcementDateSelection"
          v-slot="{ id }"
          label="Datum"
          :label-position="LabelPosition.RIGHT"
        >
          <RadioInput
            :id="id"
            v-model="selectedInputType"
            name="announcementDateSelection"
            size="medium"
            :value="InputType.DATE_TIME"
          />
        </InputField>
      </div>

      <div>
        <InputField
          id="announcementYearSelection"
          v-slot="{ id }"
          label="Jahresangabe"
          :label-position="LabelPosition.RIGHT"
        >
          <RadioInput
            :id="id"
            v-model="selectedInputType"
            name="announcementDateSelection"
            size="medium"
            :value="InputType.YEAR"
          />
        </InputField>
      </div>
    </div>

    <div v-if="selectedInputType === InputType.DATE_TIME" class="flex gap-24">
      <div class="w-288">
        <InputField id="announcementDateInput" v-slot="{ id }" label="Datum">
          <InputElement
            :id="id"
            v-model="dateValue"
            :attributes="{ ariaLabel: 'Datum' }"
            is-future-date
            :type="InputType.DATE"
          />
        </InputField>
      </div>

      <div class="w-288">
        <InputField id="announcementDateTime" v-slot="{ id }" label="Uhrzeit">
          <InputElement
            :id="id"
            v-model="timeValue"
            :attributes="{ ariaLabel: 'Uhrzeit' }"
            :type="InputType.TIME"
          />
        </InputField>
      </div>
    </div>

    <div v-if="selectedInputType === InputType.YEAR" class="w-112">
      <InputField
        id="announcementDateYearInput"
        v-slot="{ id }"
        label="Jahresangabe"
      >
        <InputElement
          :id="id"
          v-model="yearValue"
          :attributes="{ ariaLabel: 'Jahresangabe' }"
          :type="InputType.YEAR"
        />
      </InputField>
    </div>
  </div>
</template>
