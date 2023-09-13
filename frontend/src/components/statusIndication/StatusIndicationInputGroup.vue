<script lang="ts" setup>
import { computed } from "vue"
import NoteStatusIndicationGroup from "@/components/statusIndication/NoteStatusIndicationGroup.vue"
import UpdateStatusIndicationGroup from "@/components/statusIndication/UpdateStatusIndicationGroup.vue"
import { Metadata, MetadataSectionName, MetadataSections } from "@/domain/norm"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import RadioInput from "@/shared/components/input/RadioInput.vue"

const props = defineProps<{
  modelValue: MetadataSections
}>()

const emit = defineEmits<{
  "update:modelValue": [value: MetadataSections]
}>()

/* -------------------------------------------------- *
 * Section type                                       *
 * -------------------------------------------------- */

const initialValue: MetadataSections = {
  STATUS: props.modelValue.STATUS,
  REISSUE: props.modelValue.REISSUE,
  REPEAL: props.modelValue.REPEAL,
  OTHER_STATUS: props.modelValue.OTHER_STATUS,
}

const selectedChildSection = computed<
  | MetadataSectionName.STATUS // Status + Reissue share the same component
  | MetadataSectionName.REISSUE
  | MetadataSectionName.REPEAL // Repeal + OtherStatus share the same component
  | MetadataSectionName.OTHER_STATUS
>({
  get: () => {
    if (props.modelValue.STATUS?.[0]) {
      return MetadataSectionName.STATUS
    } else if (props.modelValue.REISSUE?.[0]) {
      return MetadataSectionName.REISSUE
    } else if (props.modelValue.REPEAL?.[0]) {
      return MetadataSectionName.REPEAL
    } else if (props.modelValue.OTHER_STATUS?.[0]) {
      return MetadataSectionName.OTHER_STATUS
    } else {
      return MetadataSectionName.STATUS
    }
  },
  set(value) {
    emit("update:modelValue", { [value]: initialValue[value] ?? [{}] })
  },
})

/* -------------------------------------------------- *
 * Section data                                       *
 * -------------------------------------------------- */

const statusSection = computed({
  get: () => props.modelValue.STATUS?.[0] ?? {},
  set: (data?: Metadata) => {
    const effectiveData = data ? [data] : undefined
    initialValue.STATUS = effectiveData

    const next: MetadataSections = { STATUS: effectiveData }
    emit("update:modelValue", next)
  },
})

const reissueSection = computed({
  get: () => props.modelValue.REISSUE?.[0] ?? {},
  set: (data?: Metadata) => {
    const effectiveData = data ? [data] : undefined
    initialValue.REISSUE = effectiveData

    const next: MetadataSections = { REISSUE: effectiveData }
    emit("update:modelValue", next)
  },
})

const repealSection = computed({
  get: () => props.modelValue.REPEAL?.[0] ?? {},
  set: (data?: Metadata) => {
    const effectiveData = data ? [data] : undefined
    initialValue.REPEAL = effectiveData

    const next: MetadataSections = { REPEAL: effectiveData }
    emit("update:modelValue", next)
  },
})

const otherStatusSection = computed({
  get: () => props.modelValue.OTHER_STATUS?.[0] ?? {},
  set: (data?: Metadata) => {
    const effectiveData = data ? [data] : undefined
    initialValue.OTHER_STATUS = effectiveData

    const next: MetadataSections = { OTHER_STATUS: effectiveData }
    emit("update:modelValue", next)
  },
})
</script>

<template>
  <div class="flex flex-col gap-8">
    <div
      class="mb-8 grid grid-cols-[repeat(2,minmax(0,max-content))] gap-x-176 gap-y-8"
    >
      <InputField
        id="statusSelection"
        v-slot="{ id }"
        label="Stand"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedChildSection"
          name="statusIndication"
          size="medium"
          :value="MetadataSectionName.STATUS"
        />
      </InputField>

      <InputField
        id="repealSelection"
        v-slot="{ id }"
        label="Aufhebung"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedChildSection"
          name="statusIndication"
          size="medium"
          :value="MetadataSectionName.REPEAL"
        />
      </InputField>

      <InputField
        id="reissueSelection"
        v-slot="{ id }"
        label="Neufassung"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedChildSection"
          name="statusIndication"
          size="medium"
          :value="MetadataSectionName.REISSUE"
        />
      </InputField>

      <InputField
        id="otherStatusSelection"
        v-slot="{ id }"
        label="Sonstiger Hinweis"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedChildSection"
          name="statusIndication"
          size="medium"
          :value="MetadataSectionName.OTHER_STATUS"
        />
      </InputField>
    </div>

    <UpdateStatusIndicationGroup
      v-if="selectedChildSection === MetadataSectionName.STATUS"
      v-model="statusSection"
      :type="MetadataSectionName.STATUS"
    />

    <UpdateStatusIndicationGroup
      v-else-if="selectedChildSection === MetadataSectionName.REISSUE"
      v-model="reissueSection"
      :type="MetadataSectionName.REISSUE"
    />

    <NoteStatusIndicationGroup
      v-if="selectedChildSection === MetadataSectionName.REPEAL"
      v-model="repealSection"
      :type="MetadataSectionName.REPEAL"
    />

    <NoteStatusIndicationGroup
      v-else-if="selectedChildSection === MetadataSectionName.OTHER_STATUS"
      v-model="otherStatusSection"
      :type="MetadataSectionName.OTHER_STATUS"
    />
  </div>
</template>
