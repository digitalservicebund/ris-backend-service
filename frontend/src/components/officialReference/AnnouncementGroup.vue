<script lang="ts" setup>
import { computed } from "vue"
import DigitalAnnouncementInputGroup from "@/components/officialReference/DigitalAnnouncementInputGroup.vue"
import EuAnnouncementInputGroup from "@/components/officialReference/EuAnnouncementInputGroup.vue"
import OtherOfficialAnnouncementInputGroup from "@/components/officialReference/OtherOfficialAnnouncementInputGroup.vue"
import PrintAnnouncementInputGroup from "@/components/officialReference/PrintAnnouncementInputGroup.vue"
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
  PRINT_ANNOUNCEMENT: props.modelValue.PRINT_ANNOUNCEMENT,
  DIGITAL_ANNOUNCEMENT: props.modelValue.DIGITAL_ANNOUNCEMENT,
  EU_ANNOUNCEMENT: props.modelValue.EU_ANNOUNCEMENT,
  OTHER_OFFICIAL_ANNOUNCEMENT: props.modelValue.OTHER_OFFICIAL_ANNOUNCEMENT,
}

const selectedChildSection = computed<
  | MetadataSectionName.PRINT_ANNOUNCEMENT
  | MetadataSectionName.DIGITAL_ANNOUNCEMENT
  | MetadataSectionName.EU_ANNOUNCEMENT
  | MetadataSectionName.OTHER_OFFICIAL_ANNOUNCEMENT
>({
  get: () => {
    if (props.modelValue.PRINT_ANNOUNCEMENT?.[0]) {
      return MetadataSectionName.PRINT_ANNOUNCEMENT
    } else if (props.modelValue.DIGITAL_ANNOUNCEMENT?.[0]) {
      return MetadataSectionName.DIGITAL_ANNOUNCEMENT
    } else if (props.modelValue.EU_ANNOUNCEMENT?.[0]) {
      return MetadataSectionName.EU_ANNOUNCEMENT
    } else if (props.modelValue.OTHER_OFFICIAL_ANNOUNCEMENT?.[0]) {
      return MetadataSectionName.OTHER_OFFICIAL_ANNOUNCEMENT
    } else {
      return MetadataSectionName.PRINT_ANNOUNCEMENT
    }
  },
  set(value) {
    emit("update:modelValue", { [value]: initialValue[value] ?? [{}] })
  },
})

/* -------------------------------------------------- *
 * Section data                                       *
 * -------------------------------------------------- */

const printAnnouncementSection = computed({
  get: () => props.modelValue.PRINT_ANNOUNCEMENT?.[0] ?? {},
  set: (data?: Metadata) => {
    const effectiveData = data ? [data] : undefined
    initialValue.PRINT_ANNOUNCEMENT = effectiveData

    const next: MetadataSections = { PRINT_ANNOUNCEMENT: effectiveData }
    emit("update:modelValue", next)
  },
})

const digitalAnnouncementSection = computed({
  get: () => props.modelValue.DIGITAL_ANNOUNCEMENT?.[0] ?? {},
  set: (data?: Metadata) => {
    const effectiveData = data ? [data] : undefined
    initialValue.DIGITAL_ANNOUNCEMENT = effectiveData

    const next: MetadataSections = { DIGITAL_ANNOUNCEMENT: effectiveData }
    emit("update:modelValue", next)
  },
})

const euAnnouncementSection = computed({
  get: () => props.modelValue.EU_ANNOUNCEMENT?.[0] ?? {},
  set: (data?: Metadata) => {
    const effectiveData = data ? [data] : undefined
    initialValue.EU_ANNOUNCEMENT = effectiveData

    const next: MetadataSections = { EU_ANNOUNCEMENT: effectiveData }
    emit("update:modelValue", next)
  },
})

const otherOfficialAnnouncementSection = computed({
  get: () => props.modelValue.OTHER_OFFICIAL_ANNOUNCEMENT?.[0] ?? {},
  set: (data?: Metadata) => {
    const effectiveData = data ? [data] : undefined
    initialValue.OTHER_OFFICIAL_ANNOUNCEMENT = effectiveData

    const next: MetadataSections = {
      OTHER_OFFICIAL_ANNOUNCEMENT: effectiveData,
    }
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
        id="printAnnouncementSelection"
        v-slot="{ id }"
        label="Papierverkündungsblatt"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedChildSection"
          name="officialAnnouncement"
          size="medium"
          :value="MetadataSectionName.PRINT_ANNOUNCEMENT"
        />
      </InputField>

      <InputField
        id="digitalAnnouncementSelection"
        v-slot="{ id }"
        label="Elektronisches Verkündungsblatt"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedChildSection"
          name="officialAnnouncement"
          size="medium"
          :value="MetadataSectionName.DIGITAL_ANNOUNCEMENT"
        />
      </InputField>

      <InputField
        id="euAnnouncementSelection"
        v-slot="{ id }"
        label="Amtsblatt der EU"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedChildSection"
          name="officialAnnouncement"
          size="medium"
          :value="MetadataSectionName.EU_ANNOUNCEMENT"
        />
      </InputField>

      <InputField
        id="otherAnnouncementSelection"
        v-slot="{ id }"
        label="Sonstige amtliche Fundstelle"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedChildSection"
          name="officialAnnouncement"
          size="medium"
          :value="MetadataSectionName.OTHER_OFFICIAL_ANNOUNCEMENT"
        />
      </InputField>
    </div>

    <DigitalAnnouncementInputGroup
      v-if="selectedChildSection === MetadataSectionName.DIGITAL_ANNOUNCEMENT"
      v-model="digitalAnnouncementSection"
    />

    <EuAnnouncementInputGroup
      v-else-if="selectedChildSection === MetadataSectionName.EU_ANNOUNCEMENT"
      v-model="euAnnouncementSection"
    />

    <OtherOfficialAnnouncementInputGroup
      v-else-if="
        selectedChildSection === MetadataSectionName.OTHER_OFFICIAL_ANNOUNCEMENT
      "
      v-model="otherOfficialAnnouncementSection"
    />

    <PrintAnnouncementInputGroup
      v-else-if="
        selectedChildSection === MetadataSectionName.PRINT_ANNOUNCEMENT
      "
      v-model="printAnnouncementSection"
    />
  </div>
</template>
