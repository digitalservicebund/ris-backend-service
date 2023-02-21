<script lang="ts" setup>
import { computed, h } from "vue"

interface Props {
  label?: string
  icon?: string
  ariaLabel?: string
  buttonType?: string
  href?: string
}

const props = withDefaults(defineProps<Props>(), {
  label: "Speichern", // TODO: Why?! Just why?
  icon: undefined,
  ariaLabel: undefined,
  buttonType: "primary",
  href: undefined,
})

const buttonClasses = computed(() => ({
  "btn-primary": props.buttonType == "primary",
  "btn-secondary": props.buttonType == "secondary",
  "btn-ghost": props.buttonType == "ghost",
  "btn-tertiary": props.buttonType == "tertiary",
}))

const isLink = computed(() => !!props.href)

const renderIcon = () =>
  props.icon ? h("span", { class: "material-icons" }, props.icon) : undefined

const renderLabel = () =>
  props.label ? h("span", { class: "label-02-bold" }, props.label) : undefined

const render = () => {
  const tag = isLink.value ? "a" : "button"

  return h(
    tag,
    {
      class: ["ris-btn", "flex", "gap-12", buttonClasses.value],
      href: props.href,
      "aria-label": props.ariaLabel,
    },
    [renderIcon(), renderLabel()]
  )
}
</script>

<template>
  <render />
</template>

<style lang="scss" scoped>
// The ignoring of the rule is necessary because we use manual render functions
// instead of a rich template.
// eslint-disable vue-scoped-css/no-unused-selector
.ris-btn {
  @apply align-middle bg-blue-800 text-white inline-flex items-center
  justify-center max-w-full
  overflow-hidden shrink-0 select-none
  relative no-underline normal-case
  indent-[0.1em] px-[1.5rem] py-[1.188rem] whitespace-nowrap
  border-blue-800 border-2
  outline-blue-800 outline-0 outline-offset-4 outline-none;

  letter-spacing: 0.16px;

  &:hover {
    @apply bg-blue-700 border-blue-700;
  }

  &:focus {
    @apply outline-4;
  }

  &:active {
    @apply bg-blue-500 text-blue-800 border-blue-500;
  }

  &:disabled {
    @apply bg-gray-400 text-gray-600 border-gray-400;
  }
}

.btn-secondary {
  @apply text-blue-800 bg-yellow-500;

  &:hover {
    @apply bg-yellow-700;
  }

  &:focus {
    @apply border-yellow-500;
  }

  &:active {
    @apply bg-yellow-400 border-blue-800 border-2;
  }

  &:disabled {
    @apply bg-gray-400 text-gray-600 border-gray-400;
  }
}

.btn-tertiary {
  @apply text-blue-800 bg-transparent;

  &:hover {
    @apply bg-blue-200;
  }

  &:active {
    @apply border-blue-200 bg-blue-200;
  }

  &:disabled {
    @apply bg-white border-2 border-gray-600;
  }
}

.btn-ghost {
  @apply border-transparent bg-transparent text-blue-800;

  &:hover,
  &:focus {
    @apply border-gray-600 bg-white;
  }

  &:active {
    @apply border-white bg-white;
  }

  &:disabled {
    @apply bg-transparent;
  }

  .label-02-bold {
    @apply underline;
  }
}
</style>
