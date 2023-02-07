<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { ValidationError } from "@/domain"

interface Props {
  id: string
  value?: string
  modelValue?: string
  ariaLabel: string
  isFutureDate?: boolean
  validationError?: ValidationError
}

const props = defineProps<Props>()

const dayValue = ref<string>()
const monthValue = ref<string>()
const yearValue = ref<string>()

watch(
  props,
  () => {
    if (props.modelValue) {
      const splitDate = props.modelValue.split("-")
      dayValue.value = splitDate[2]
      monthValue.value = splitDate[1]
      yearValue.value = splitDate[0]
    }
  },
  {
    immediate: true,
  }
)

const ariaLabelDay = computed(() => props.ariaLabel + " Tag")

const ariaLabelMonth = computed(() => props.ariaLabel + " Monat")

const ariaLabelYear = computed(() => props.ariaLabel + " Jahr")

function updateValue(event: Event) {
  //check for allowed length of value
  const target = event.target as HTMLInputElement
  if (target.value.length >= target.maxLength) {
    if (!target) return
    let next = target
    while ((next = next.nextElementSibling as HTMLInputElement)) {
      if (next == null) break
      if (next.tagName.toLowerCase() == "input") {
        next.focus()
        break
      }
    }
  }
}

function selectAll(event: Event) {
  ;(event.target as HTMLInputElement).select()
}
</script>

<template>
  <div
    :aria-label="ariaLabel"
    class="bg-white border-2 border-blue-800 flex flex-row focus:outline-2 h-[3.75rem] hover:outline-2 input items-center outline-0 outline-blue-800 outline-none outline-offset-[-4px] px-16 uppercase w-full"
    @input="updateValue"
  >
    <input
      :id="id"
      v-model="dayValue"
      :aria-label="ariaLabelDay"
      class="focus:outline-none w-20"
      maxLength="2"
      placeholder="TT"
      type="number"
      @focus="selectAll($event)"
    />
    <span class="mr-2">.</span>
    <input
      :id="id"
      v-model="monthValue"
      :aria-label="ariaLabelMonth"
      class="focus:outline-none w-20"
      maxLength="2"
      placeholder="MM"
      type="number"
      @focus="selectAll($event)"
    />
    <span class="mr-2">.</span>
    <input
      :id="id"
      v-model="yearValue"
      :aria-label="ariaLabelYear"
      class="focus:outline-none w-40"
      maxLength="4"
      placeholder="JJJJ"
      type="number"
      @focus="selectAll($event)"
    />
  </div>
</template>

<style lang="scss" scoped>
input[type="number"] {
  appearance: textfield;
}

input::-webkit-outer-spin-button,
input::-webkit-inner-spin-button {
  appearance: none;
}

.input {
  &:autofill {
    @apply shadow-white text-inherit;
  }

  &:autofill:focus {
    @apply shadow-white text-inherit;
  }
}
</style>
