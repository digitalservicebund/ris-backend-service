<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import InputElement from "@/components/InputElement.vue"
import InputFieldComponent from "@/components/InputField.vue"
import type { InputField, ModelType } from "@/domain"
import { ValidationError } from "@/domain"

type InputValues = { [fieldName: string]: ModelType }

interface Props {
  fields: InputField[]
  modelValue?: InputValues
  columnCount?: number
  validationErrors?: ValidationError[]
}

interface Emits {
  (event: "update:modelValue", value: InputValues): void
}

const props = withDefaults(defineProps<Props>(), {
  columnCount: 1,
  validationErrors: undefined,
})

const emit = defineEmits<Emits>()

/*
 * As the gap is more versatile than margins/paddings, but does not get
 * includes into the `box-sizing`, it is necessary to put into a custom
 * calculation. Therefore it is important to set the gap unified over all
 * elements and thereby keep it completely out of the style section.
 * After all, a grid layout might be the more powerful solution, but the flow
 * was not yet working and is more important.
 */
const gapSize = "2rem"
const gapStyle = { gap: gapSize }
const fieldStyle = computed(() => ({
  width: `calc((100% - ${gapSize} * ${props.columnCount - 1}) / ${
    props.columnCount
  })`,
}))

const fieldRows = computed(() => {
  const { fields, columnCount } = props
  const rows = []

  for (let index = 0; index < fields.length; index += columnCount) {
    const row = fields.slice(index, index + columnCount)
    rows.push(row)
  }

  return rows
})

// A writable computed value (with getter + setter) is not possible due to the depth
// of the model that gets split across the inputs.
const inputValues = ref<InputValues>({})

watch(
  () => props.modelValue,
  () => (inputValues.value = props.modelValue ? props.modelValue : {}),
  { immediate: true, deep: true }
)

watch(inputValues, () => emit("update:modelValue", inputValues.value), {
  deep: true,
})
</script>

<template>
  <div class="input-group pb-[2rem]" :style="gapStyle">
    <div
      v-for="(group, index) in fieldRows"
      :key="index"
      class="input-group__row"
      :style="gapStyle"
    >
      <InputFieldComponent
        v-for="field in group"
        :id="field.name"
        :key="field.name"
        class="input-group__row__field"
        :label="field.label"
        :label-position="field.inputAttributes.labelPosition"
        :required="field.required"
        :style="fieldStyle"
      >
        <InputElement
          :id="field.name"
          v-model="inputValues[field.name]"
          :attributes="field.inputAttributes"
          :type="field.type"
          :validation-error="
            props.validationErrors &&
            props.validationErrors.find(
              (err) => err.field.split('\.')[1] === field.name
            )
          "
        />
      </InputFieldComponent>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.input-group {
  display: flex;
  width: 100%;
  flex-direction: column;

  &__row {
    display: flex;
    width: 100%;
    flex-wrap: wrap;

    &__field {
      min-width: 15rem;
    }
  }
}
</style>
