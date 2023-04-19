<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import DigitalAnnouncementInputGroup from "@/components/DigitalAnnouncementInputGroup.vue"
import EuGovernmentGazetteInputGroup from "@/components/EuGovernmentGazetteInputGroup.vue"
import OtherOfficialReferenceInputGroup from "@/components/OtherOfficialReferenceInputGroup.vue"
import PrintAnnouncementInputGroup from "@/components/PrintAnnouncementInputGroup.vue"
import { Metadata } from "@/domain/Norm"

interface Props {
  modelValue: Metadata
}

interface Emits {
  (event: "update:modelValue", value: Metadata): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

enum InputType {
  PRINT = "printAnnouncement",
  DIGITAL = "digitalAnnouncement",
  EU = "euAnnouncement",
  OTHER = "otherAnnouncement",
}

const inputValue = ref<Metadata>(props.modelValue)
const selectedInputType = ref<InputType | undefined>(undefined)

watch(props, () => (inputValue.value = props.modelValue), {
  immediate: true,
  deep: true,
})

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})

watch(
  inputValue,
  () => {
    if (!selectedInputType.value) {
      selectedInputType.value = InputType.PRINT
    }
  },
  {
    deep: true,
    immediate: true,
  }
)

watch(selectedInputType, () => {
  inputValue.value = {}
})

const component = computed(() => {
  switch (selectedInputType.value) {
    case InputType.PRINT:
      return PrintAnnouncementInputGroup
    case InputType.DIGITAL:
      return DigitalAnnouncementInputGroup
    case InputType.EU:
      return EuGovernmentGazetteInputGroup
    case InputType.OTHER:
      return OtherOfficialReferenceInputGroup
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
            v-model="selectedInputType"
            aria-label="Papierverkündungsblatt"
            name="OfficialAnnouncement"
            type="radio"
            :value="InputType.PRINT"
          />
          Papierverkündungsblatt
        </label>
        <label class="form-control">
          <input
            v-model="selectedInputType"
            aria-label="Amtsblatt der EU"
            name="OfficialAnnouncement"
            type="radio"
            :value="InputType.EU"
          />
          Amtsblatt der EU
        </label>
      </div>
      <div class="flex flex-col gap-24 radio-group">
        <label class="flex form-control items-start">
          <input
            v-model="selectedInputType"
            aria-label="Elektronisches Verkündungsblatt"
            name="OfficialAnnouncement"
            type="radio"
            :value="InputType.DIGITAL"
          />
          Elektronisches Verkündungsblatt
        </label>
        <label class="form-control">
          <input
            v-model="selectedInputType"
            aria-label="Sonstige amtliche Fundstelle"
            name="OfficialAnnouncement"
            type="radio"
            :value="InputType.OTHER"
          />
          Sonstige amtliche Fundstelle
        </label>
      </div>
    </div>
    <component :is="component" v-model="inputValue" />
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
