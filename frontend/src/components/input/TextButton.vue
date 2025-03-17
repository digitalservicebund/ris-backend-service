<script lang="ts" setup>
import Button from "primevue/button"
import type { Component } from "vue"

interface Props {
  label?: string
  icon?: Component
  ariaLabel?: string
  buttonType?: ButtonType
  disabled?: boolean
  target?: "_self" | "_blank" | "_parent" | "_top"
  iconPosition?: "left" | "right"
  loading?: boolean
}

defineProps<Props>()
</script>

<script lang="ts">
export type ButtonType =
  | "primary"
  | "secondary"
  | "tertiary"
  | "ghost"
  | "destructive"
</script>

<template>
  <Button
    :aria-label="ariaLabel"
    :disabled="disabled"
    :icon-pos="iconPosition"
    :label="label"
    :loading="false"
    :severity="buttonType"
    :target="target"
    :text="false"
  >
    <template #icon>
      <icon />
    </template>
  </Button>
</template>
