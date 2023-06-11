<script lang="ts" setup>
import { sanitizeUrl } from "@braintree/sanitize-url"
import { computed, h } from "vue"

interface Props {
  label?: string
  icon?: string
  ariaLabel?: string
  buttonType?: string
  disabled?: boolean
  href?: string
  download?: boolean | string
  size?: "large" | "medium" | "small"
  target?: "_self" | "_blank" | "_parent" | "_top"
}

const props = withDefaults(defineProps<Props>(), {
  label: undefined,
  icon: undefined,
  ariaLabel: undefined,
  buttonType: "primary",
  disabled: false,
  href: undefined,
  download: undefined,
  size: "large",
  target: undefined,
})

const buttonClasses = computed(() => ({
  "ds-button-large": !props.size || props.size === "large",
  "ds-button-medium": props.size === "medium",
  "ds-button-small": props.size === "small",
  "ds-button-primary": props.buttonType == "primary",
  "ds-button-secondary": props.buttonType == "secondary",
  "ds-button-ghost": props.buttonType == "ghost",
  "ds-button-tertiary": props.buttonType == "tertiary",
  "ds-button-with-icon": props.icon,
  "ds-button-with-icon-only": props.icon && !props.label,
  "is-disabled": props.href && props.disabled,
}))

const isLink = computed(() => !!props.href)
const sanitizedUrl = computed(() => sanitizeUrl(props.href))

const renderIcon = () =>
  props.icon
    ? h("span", { class: "material-icons ds-button-icon" }, props.icon)
    : undefined

const renderLabel = () =>
  props.label ? h("span", { class: "ds-button-label" }, props.label) : undefined

const render = () => {
  const { disabled, href, download, target } = props
  let tag = "button"
  if (isLink.value) {
    tag = disabled ? "div" : "a"
  }

  return h(
    tag,
    {
      class: ["ds-button", buttonClasses.value],
      "aria-label": props.ariaLabel,
      disabled: !href && disabled,
      href: sanitizedUrl.value,
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
// eslint-disable-next-line vue-scoped-css/no-unused-selector
.ds-button-large > .ds-button-icon {
  font-size: 2rem;
}
</style>
