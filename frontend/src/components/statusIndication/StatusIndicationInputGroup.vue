<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import NoteStatusIndicationGroup from "@/components/statusIndication/NoteStatusIndicationGroup.vue"
import UpdateStatusIndicationGroup from "@/components/statusIndication/UpdateStatusIndicationGroup.vue"
import { Metadata, MetadataSectionName, MetadataSections } from "@/domain/Norm"

interface Props {
  modelValue: MetadataSections
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value: MetadataSections]
}>()

type ChildSectionName =
  | MetadataSectionName.STATUS // Status + Reissue share the same component
  | MetadataSectionName.REISSUE
  | MetadataSectionName.REPEAL // Repeal + OtherStatus share the same component
  | MetadataSectionName.OTHER_STATUS

const childSection = ref<Metadata>({})
const selectedChildSectionName = ref<ChildSectionName>(
  MetadataSectionName.REPEAL
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
    if (modelValue.STATUS) {
      selectedChildSectionName.value = MetadataSectionName.STATUS
      childSection.value = modelValue.STATUS[0]
    } else if (modelValue.REISSUE) {
      selectedChildSectionName.value = MetadataSectionName.REISSUE
      childSection.value = modelValue.REISSUE[0]
    } else if (modelValue.REPEAL) {
      selectedChildSectionName.value = MetadataSectionName.REPEAL
      childSection.value = modelValue.REPEAL[0]
    } else if (modelValue.OTHER_STATUS) {
      selectedChildSectionName.value = MetadataSectionName.OTHER_STATUS
      childSection.value = modelValue.OTHER_STATUS[0]
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
    case MetadataSectionName.STATUS:
    case MetadataSectionName.REISSUE:
      return UpdateStatusIndicationGroup
    case MetadataSectionName.REPEAL:
    case MetadataSectionName.OTHER_STATUS:
      return NoteStatusIndicationGroup
    default:
      throw new Error(
        `Unknown announcement child section: "${selectedChildSectionName.value}"`
      )
  }
})
</script>

<template>
  <div class="flex flex-col gap-8">
    <div class="flex flex-wrap gap-176">
      <div class="flex flex-col gap-24 mb-24">
        <label class="form-control">
          <input
            id="statusSelection"
            v-model="selectedChildSectionName"
            aria-label="Stand"
            name="StatusIndication"
            type="radio"
            :value="MetadataSectionName.STATUS"
          />
          Stand
        </label>
        <label class="form-control">
          <input
            id="reissueSelection"
            v-model="selectedChildSectionName"
            aria-label="Neufassung"
            name="StatusIndication"
            type="radio"
            :value="MetadataSectionName.REISSUE"
          />
          Neufassung
        </label>
      </div>
      <div class="flex flex-col gap-24 mb-24">
        <label class="flex form-control items-start">
          <input
            id="repealSelection"
            v-model="selectedChildSectionName"
            aria-label="Aufhebung"
            name="StatusIndication"
            type="radio"
            :value="MetadataSectionName.REPEAL"
          />
          Aufhebung
        </label>
        <label class="form-control">
          <input
            id="otherStatusSelection"
            v-model="selectedChildSectionName"
            aria-label="Sonstiger Hinweis"
            name="StatusIndication"
            type="radio"
            :value="MetadataSectionName.OTHER_STATUS"
          />
          Sonstiger Hinweis
        </label>
      </div>
    </div>
    <component
      :is="component"
      v-model="childSection"
      :type="selectedChildSectionName"
    />
  </div>
</template>

<style lang="scss" scoped>
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
  width: 0.9em;
  height: 0.9em;
  border-radius: 50%;
  background-color: #004b76;
  content: "";
  transform: scale(0);
}

input[type="radio"]:hover,
input[type="radio"]:focus {
  border: 4px solid #004b76;
  outline: none;
}

input[type="radio"]:checked::before {
  transform: scale(1);
}
</style>
