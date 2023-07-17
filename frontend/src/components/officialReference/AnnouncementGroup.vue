<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import DigitalAnnouncementInputGroup from "@/components/officialReference/DigitalAnnouncementInputGroup.vue"
import EuAnnouncementInputGroup from "@/components/officialReference/EuAnnouncementInputGroup.vue"
import OtherOfficialAnnouncementInputGroup from "@/components/officialReference/OtherOfficialAnnouncementInputGroup.vue"
import PrintAnnouncementInputGroup from "@/components/officialReference/PrintAnnouncementInputGroup.vue"
import { Metadata, MetadataSectionName, MetadataSections } from "@/domain/Norm"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import RadioInput from "@/shared/components/input/RadioInput.vue"

interface Props {
  modelValue: MetadataSections
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value: MetadataSections]
}>()

type ChildSectionName =
  | MetadataSectionName.PRINT_ANNOUNCEMENT
  | MetadataSectionName.DIGITAL_ANNOUNCEMENT
  | MetadataSectionName.EU_ANNOUNCEMENT
  | MetadataSectionName.OTHER_OFFICIAL_ANNOUNCEMENT

const childSection = ref<Metadata>({})
const selectedChildSectionName = ref<ChildSectionName>(
  MetadataSectionName.PRINT_ANNOUNCEMENT,
)

watch(
  childSection,
  () =>
    emit("update:modelValue", {
      [selectedChildSectionName.value]: [childSection.value],
    }),
  {
    deep: true,
  },
)

watch(
  () => props.modelValue,
  (modelValue) => {
    if (modelValue.PRINT_ANNOUNCEMENT) {
      selectedChildSectionName.value = MetadataSectionName.PRINT_ANNOUNCEMENT
      childSection.value = modelValue.PRINT_ANNOUNCEMENT[0]
    } else if (modelValue.DIGITAL_ANNOUNCEMENT) {
      selectedChildSectionName.value = MetadataSectionName.DIGITAL_ANNOUNCEMENT
      childSection.value = modelValue.DIGITAL_ANNOUNCEMENT[0]
    } else if (modelValue.EU_ANNOUNCEMENT) {
      selectedChildSectionName.value = MetadataSectionName.EU_ANNOUNCEMENT
      childSection.value = modelValue.EU_ANNOUNCEMENT[0]
    } else if (modelValue.OTHER_OFFICIAL_ANNOUNCEMENT) {
      selectedChildSectionName.value =
        MetadataSectionName.OTHER_OFFICIAL_ANNOUNCEMENT
      childSection.value = modelValue.OTHER_OFFICIAL_ANNOUNCEMENT[0]
    }
  },
  {
    immediate: true,
    deep: true,
  },
)

watch(selectedChildSectionName, () => (childSection.value = {}))

const component = computed(() => {
  switch (selectedChildSectionName.value) {
    case MetadataSectionName.PRINT_ANNOUNCEMENT:
      return PrintAnnouncementInputGroup
    case MetadataSectionName.DIGITAL_ANNOUNCEMENT:
      return DigitalAnnouncementInputGroup
    case MetadataSectionName.EU_ANNOUNCEMENT:
      return EuAnnouncementInputGroup
    case MetadataSectionName.OTHER_OFFICIAL_ANNOUNCEMENT:
      return OtherOfficialAnnouncementInputGroup
    default:
      throw new Error(
        `Unknown announcement child section: "${selectedChildSectionName.value}"`,
      )
  }
})
</script>

<template>
  <div class="flex flex-col gap-8">
    <div class="flex flex-wrap gap-176">
      <div class="mb-8 flex flex-col gap-8">
        <InputField
          id="printAnnouncementSelection"
          v-slot="{ id }"
          label="Papierverkündungsblatt"
          :label-position="LabelPosition.RIGHT"
        >
          <RadioInput
            :id="id"
            v-model="selectedChildSectionName"
            name="officialAnnouncement"
            size="medium"
            :value="MetadataSectionName.PRINT_ANNOUNCEMENT"
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
            v-model="selectedChildSectionName"
            name="officialAnnouncement"
            size="medium"
            :value="MetadataSectionName.EU_ANNOUNCEMENT"
          />
        </InputField>
      </div>

      <div class="mb-8 flex flex-col gap-8">
        <InputField
          id="digitalAnnouncementSelection"
          v-slot="{ id }"
          label="Elektronisches Verkündungsblatt"
          :label-position="LabelPosition.RIGHT"
        >
          <RadioInput
            :id="id"
            v-model="selectedChildSectionName"
            name="officialAnnouncement"
            size="medium"
            :value="MetadataSectionName.DIGITAL_ANNOUNCEMENT"
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
            v-model="selectedChildSectionName"
            name="officialAnnouncement"
            size="medium"
            :value="MetadataSectionName.OTHER_OFFICIAL_ANNOUNCEMENT"
          />
        </InputField>
      </div>
    </div>

    <component :is="component" v-model="childSection" />
  </div>
</template>
