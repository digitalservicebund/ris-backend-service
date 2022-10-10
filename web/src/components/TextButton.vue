<script lang="ts" setup>
import { computed } from "vue"

interface Props {
  label?: string
  icon?: string
  ariaLabel?: string
  buttonType?: string
}

const props = withDefaults(defineProps<Props>(), {
  label: "Speichern",
  icon: undefined,
  ariaLabel: undefined,
  buttonType: "primary",
})

const buttonClasses = computed(() => ({
  "ris-btn": true,
  "btn-primary": props.buttonType == "primary",
  "btn-secondary": props.buttonType == "secondary",
  "btn-ghost": props.buttonType == "ghost",
  "btn-tertiary": props.buttonType == "tertiary",
}))

const labels = computed(() => ({
  "label-ghost": props.buttonType == "ghost",
}))
</script>

<template>
  <button
    :aria-label="ariaLabel"
    :class="buttonClasses"
    flat
    :ripple="false"
    :rounded="0"
  >
    <slot>
      <span v-if="icon" class="material-icons pr-2"> {{ icon }} </span>
      <span class="label-01-bold" :class="labels">{{ label }} </span>
    </slot>
  </button>
</template>

<style lang="scss" scoped>
.ris-btn {
  @apply align-middle bg-blue-800 inline-flex items-center 
  justify-center max-w-full
  no-underline overflow-hidden outline-none shrink-0 select-none
  relative tracking-widest no-underline normal-case 
  indent-[0.1em] px-[1.5rem] py-[1.188rem] whitespace-nowrap;

  line-height: normal;

  &:focus-visible {
    outline-offset: 2px;
  }
}

.btn-primary {
  @apply bg-blue-800 text-white;

  &:hover {
    @apply bg-blue-800;
  }

  &:active {
    @apply bg-blue-500;
  }

  &:focus-visible {
    @apply border-2 border-solid border-blue-800;
  }

  &:disabled {
    @apply bg-gray-400 text-gray-900;
  }
}

.btn-secondary {
  @apply border-solid border-2 border-blue-800 text-blue-800 bg-yellow-500;

  &:hover {
    @apply bg-yellow-700;
  }

  &:active {
    @apply bg-yellow-400;
  }

  &:focus-visible {
    @apply border-solid border-2 border-blue-800;
  }
}

.btn-ghost {
  @apply border-2 border-solid border-transparent bg-transparent text-blue-800;

  &:hover {
    @apply border-2 border-solid border-blue-800 bg-white;
  }

  &:active {
    @apply border-white bg-white;
  }

  &:focus-visible {
    @apply border-2 border-solid border-blue-800 bg-white;
  }
}

.label-ghost {
  @apply underline;
}

.btn-tertiary {
  @apply border-solid border-2 border-blue-800 text-blue-800 bg-white;

  &:hover {
    @apply bg-blue-200;
  }

  &:active {
    @apply border-blue-200 bg-blue-200;
  }

  &:focus-visible {
    @apply border-solid border-2 border-blue-800 bg-blue-200;
  }
}
</style>
