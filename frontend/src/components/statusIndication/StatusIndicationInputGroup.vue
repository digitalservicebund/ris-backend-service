<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import NoteStatusIndicationGroup from "@/components/statusIndication/NoteStatusIndicationGroup.vue"
import UpdateStatusIndicationGroup from "@/components/statusIndication/UpdateStatusIndicationGroup.vue"
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
  | MetadataSectionName.STATUS // Status + Reissue share the same component
  | MetadataSectionName.REISSUE
  | MetadataSectionName.REPEAL // Repeal + OtherStatus share the same component
  | MetadataSectionName.OTHER_STATUS

const childSection = ref<Metadata>({})
const selectedChildSectionName = ref<ChildSectionName>(
  MetadataSectionName.REPEAL,
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
  },
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
          id="statusSelection"
          v-slot="{ id }"
          label="Stand"
          :label-position="LabelPosition.RIGHT"
        >
          <RadioInput
            :id="id"
            v-model="selectedChildSectionName"
            name="statusIndication"
            size="medium"
            :value="MetadataSectionName.STATUS"
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
            v-model="selectedChildSectionName"
            name="statusIndication"
            size="medium"
            :value="MetadataSectionName.REISSUE"
          />
        </InputField>
      </div>

      <div class="mb-8 flex flex-col gap-8">
        <InputField
          id="repealSelection"
          v-slot="{ id }"
          label="Aufhebung"
          :label-position="LabelPosition.RIGHT"
        >
          <RadioInput
            :id="id"
            v-model="selectedChildSectionName"
            name="statusIndication"
            size="medium"
            :value="MetadataSectionName.REPEAL"
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
            v-model="selectedChildSectionName"
            name="statusIndication"
            size="medium"
            :value="MetadataSectionName.OTHER_STATUS"
          />
        </InputField>
      </div>
    </div>

    <component
      :is="component"
      v-model="childSection"
      :type="selectedChildSectionName"
    />
  </div>
</template>
