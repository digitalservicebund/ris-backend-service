<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata, NormCategory } from "@/domain/Norm"
import CheckboxInput from "@/shared/components/input/CheckboxInput.vue"
import ChipsInput from "@/shared/components/input/ChipsInput.vue"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

interface Props {
  modelValue: Metadata
}

interface Emits {
  (event: "update:modelValue", value: Metadata): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const inputValue = ref(props.modelValue)

const typeName = computed({
  get: () => inputValue.value.TYPE_NAME?.[0],
  set: (data?: string) => data && (inputValue.value.TYPE_NAME = [data]),
})

const selectedNormCategories = ref<Record<NormCategory, boolean>>(
  {} as Record<NormCategory, boolean>
)

watch(
  selectedNormCategories,
  () => {
    inputValue.value.NORM_CATEGORY = Object.entries(
      selectedNormCategories.value
    )
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      .filter(([_category, isSelected]) => isSelected)
      .map(([category]) => category as NormCategory)
  },
  { deep: true }
)

watch(
  () => inputValue.value.NORM_CATEGORY,
  (categories) => {
    for (const category of Object.values(NormCategory)) {
      selectedNormCategories.value[category] =
        categories?.includes(category) ?? false
    }
  },
  { immediate: true, deep: true }
)

const templateName = computed({
  get: () => inputValue.value.TEMPLATE_NAME,
  set: (data?: string[]) => data && (inputValue.value.TEMPLATE_NAME = data),
})

const NORM_CATEGORY_NAMES = {
  [NormCategory.AMENDMENT_NORM]: "Änderungsnorm",
  [NormCategory.BASE_NORM]: "Stammnorm",
  [NormCategory.TRANSITIONAL_NORM]: "Übergangsnorm",
}

watch(props, () => (inputValue.value = props.modelValue), {
  immediate: true,
  deep: true,
})

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})
</script>

<template>
  <div class="flex flex-col">
    <div>
      <InputField
        id="documentTypeName"
        aria-label="Typbezeichnung"
        class="w-1/2"
        label="Typbezeichnung"
      >
        <TextInput
          id="documentTypeName"
          v-model="typeName"
          aria-label="Typbezeichnung"
        />
      </InputField>
    </div>
    <div class="mb-24 mt-24">
      <InputField
        id="documentNormCategory"
        aria-label="Art der Norm"
        label="Art der Norm"
      >
        <div class="flex gap-24">
          <InputField
            v-for="category in NormCategory"
            :id="category"
            :key="category"
            :aria-label="NORM_CATEGORY_NAMES[category]"
            :label="NORM_CATEGORY_NAMES[category]"
            :label-position="LabelPosition.RIGHT"
          >
            <CheckboxInput
              :id="category"
              v-model="selectedNormCategories[category]"
              :aria-label="NORM_CATEGORY_NAMES[category]"
            />
          </InputField>
        </div>
      </InputField>
    </div>
    <div>
      <InputField
        id="documentTemplateName"
        aria-label="Bezeichnung gemäß Vorlage"
        class="w-11/12"
        label="Bezeichnung gemäß Vorlage"
      >
        <ChipsInput
          id="documentTemplateName"
          v-model="templateName"
          aria-label="Bezeichnung gemäß Vorlage"
        />
      </InputField>
    </div>
  </div>
</template>
