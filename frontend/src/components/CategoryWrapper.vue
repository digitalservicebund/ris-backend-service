<script lang="ts" setup>
import { ref } from "vue"
import TextButton from "@/components/input/TextButton.vue"
import IconAdd from "~icons/material-symbols/add"

const props = defineProps<{
  classes?: string
  label: string
  shouldShowButton: boolean
}>()

const shouldShowButton = ref<boolean>(props.shouldShowButton)

function toggle() {
  shouldShowButton.value = !shouldShowButton.value
}
</script>

<template>
  <TextButton
    v-if="shouldShowButton"
    button-type="tertiary"
    :class="classes"
    :icon="IconAdd"
    :label="label"
    size="small"
    @click="toggle"
  />
  <slot v-else />
</template>
