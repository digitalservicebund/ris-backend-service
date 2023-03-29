<script lang="ts" setup>
import { computed, h } from "vue"

interface Props {
  label?: string
  icon?: string
  ariaLabel?: string
  disabled?: boolean
  href?: string
  download?: boolean | string
  target?: "_self" | "_blank" | "_parent" | "_top"
}

const props = withDefaults(defineProps<Props>(), {
  label: undefined,
  icon: undefined,
  ariaLabel: undefined,
  disabled: false,
  href: undefined,
  download: undefined,
  target: undefined,
})

const isLink = computed(() => !!props.href)

const renderIcon = () =>
  props.icon
    ? h("span", { class: "material-icons text-14" }, props.icon)
    : undefined

const renderLabel = () =>
  props.label
    ? h(
        "span",
        {
          class: "font-bold text-14 leading-18",
        },
        props.label
      )
    : undefined

const render = () => {
  const tag = isLink.value ? "a" : "button"
  const { disabled, href, download, target } = props
  const classes = ["ris-btn-nested", "flex", "gap-0.5"]
  if (!props.icon) {
    classes.push("pl-[0.25rem]")
  }

  return h(
    tag,
    {
      class: classes,
      "aria-label": props.ariaLabel,
      disabled,
      href,
      download,
      target,
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
.ris-btn-nested {
  @apply align-middle bg-blue-300 text-blue-800 inline-flex items-center
  justify-center max-w-full
  overflow-hidden shrink-0 select-none
  relative no-underline normal-case
  indent-[0.1em] pr-[0.25rem] py-[0.125rem] whitespace-nowrap
  border-blue-300 border-2
  outline-blue-800 outline-0 outline-offset-4 outline-none;

  letter-spacing: 0.16px;

  &:hover {
    @apply bg-blue-800 border-blue-800 text-white;
  }

  &:focus {
    @apply outline-4;
  }

  &:active {
    @apply bg-blue-500 text-white border-blue-500;
  }

  &:disabled {
    @apply bg-gray-400 text-gray-600 border-gray-400;
  }

  /* focus-visible with support for older browsers */
  &:focus:not(:focus-visible) {
    @apply outline-transparent;
  }
}
</style>
