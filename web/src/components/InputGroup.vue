<template>
  <div class="input-group" :style="gapStyle">
    <div
      v-for="(group, index) in fieldRows"
      :key="index"
      :style="gapStyle"
      class="input-group__row"
    >
      <InputField
        v-for="field in group"
        :id="field.id"
        :key="field.id"
        :style="fieldStyle"
        class="input-group__row__field"
        :label="field.label"
        :icon-name="field.iconName"
      >
        <TextInput
          :id="field.id"
          v-model="inputValues[field.id]"
          :aria-label="field.ariaLabel"
        />
      </InputField>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import InputField from "@/components/InputField.vue"
import TextInput from "@/components/TextInput.vue"

interface Field {
  id: string
  label: string
  ariaLabel: string
  iconName: string
}

type Values = { [fieldId: string]: string }

interface Props {
  fields: Field[]
  modelValue: Values
  columnCount?: number
}

interface Emits {
  (event: "update:modelValue", value: Values): void
}

const props = withDefaults(defineProps<Props>(), {
  columnCount: 1,
})
const emit = defineEmits<Emits>()

const inputValues = ref<Values>({})

/*
 * As the gap is more versatile than margins/paddings, but does not get
 * includes into the `box-sizing`, it is necessary to put into a custom
 * calculation. Therefore it is important to set the gap unified over all
 * elements and thereby keep it completely out of the style section.
 * After all, a grid layout might be the more powerful solution, but the flow
 * was not yet working and is more important.
 */
const gapSize = "3rem"
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

watch(
  () => props.modelValue,
  () => {
    inputValues.value = { ...inputValues.value, ...props.modelValue }
  },
  { immediate: true }
)

watch(
  inputValues,
  () => {
    emit("update:modelValue", inputValues.value)
  },
  { deep: true }
)
</script>

<style lang="scss" scoped>
.input-group {
  width: 100%;
  display: flex;
  flex-direction: column;

  &__row {
    width: 100%;
    display: flex;
    flex-wrap: wrap;

    &__field {
      min-width: 25rem;
    }
  }
}
</style>
