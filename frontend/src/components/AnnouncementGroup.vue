<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import DigitalAnnouncementInputGroup from "@/components/DigitalAnnouncementInputGroup.vue"
import EuAnnouncementInputGroup from "@/components/EuAnnouncementInputGroup.vue"
import OtherOfficialAnnouncementInputGroup from "@/components/OtherOfficialAnnouncementInputGroup.vue"
import PrintAnnouncementInputGroup from "@/components/PrintAnnouncementInputGroup.vue"
import { Metadata, MetadataSectionName, MetadataSections } from "@/domain/Norm"

interface Props {
  modelValue: MetadataSections
}

interface Emits {
  (event: "update:modelValue", value: MetadataSections): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

type ChildSectionName =
  | MetadataSectionName.PRINT_ANNOUNCEMENT
  | MetadataSectionName.DIGITAL_ANNOUNCEMENT
  | MetadataSectionName.EU_ANNOUNCEMENT
  | MetadataSectionName.OTHER_OFFICIAL_ANNOUNCEMENT

const childSection = ref<Metadata>({})
const selectedChildSectionName = ref<ChildSectionName>(
  MetadataSectionName.PRINT_ANNOUNCEMENT
)

watch(
  childSection,
  () =>
    emit("update:modelValue", {
      [selectedChildSectionName.value]: [childSection.value],
    }),
  {
    deep: true,
  }
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
  }
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
      return null
  }
})
</script>

<template>
  <div class="pb-32">
    <div class="flex flex-wrap gap-176">
      <div class="flex flex-col gap-24 radio-group">
        <label class="form-control">
          <input
            id="printAnnouncementSelection"
            v-model="selectedChildSectionName"
            aria-label="Papierverkündungsblatt"
            name="OfficialAnnouncement"
            type="radio"
            :value="MetadataSectionName.PRINT_ANNOUNCEMENT"
          />
          Papierverkündungsblatt
        </label>
        <label class="form-control">
          <input
            id="euAnnouncementSelection"
            v-model="selectedChildSectionName"
            aria-label="Amtsblatt der EU"
            name="OfficialAnnouncement"
            type="radio"
            :value="MetadataSectionName.EU_ANNOUNCEMENT"
          />
          Amtsblatt der EU
        </label>
      </div>
      <div class="flex flex-col gap-24 radio-group">
        <label class="flex form-control items-start">
          <input
            id="digitalAnnouncementSelection"
            v-model="selectedChildSectionName"
            aria-label="Elektronisches Verkündungsblatt"
            name="OfficialAnnouncement"
            type="radio"
            :value="MetadataSectionName.DIGITAL_ANNOUNCEMENT"
          />
          Elektronisches Verkündungsblatt
        </label>
        <label class="form-control">
          <input
            id="otherAnnouncementSelection"
            v-model="selectedChildSectionName"
            aria-label="Sonstige amtliche Fundstelle"
            name="OfficialAnnouncement"
            type="radio"
            :value="MetadataSectionName.OTHER_OFFICIAL_ANNOUNCEMENT"
          />
          Sonstige amtliche Fundstelle
        </label>
      </div>
    </div>
    <component :is="component" v-model="childSection" />
  </div>
</template>

<style lang="scss" scoped>
.radio-group {
  display: flex;
  justify-content: space-between;
  margin-bottom: 24px;
}

.form-control {
  display: flex;
  flex-direction: row;
  align-items: center;
}

input[type="radio"] {
  display: grid;
  width: 1.5em;
  height: 1.5em;
  border: 0.15em solid currentcolor;
  border-radius: 50%;
  margin-right: 10px;
  appearance: none;
  background-color: white;
  color: #004b76;
  place-content: center;
}

input[type="radio"]::before {
  width: 0.75em;
  height: 0.75em;
  border-radius: 50%;
  background-color: #004b76;
  content: "";
  transform: scale(0);
}

input[type="radio"]:checked::before {
  transform: scale(1);
}
</style>
