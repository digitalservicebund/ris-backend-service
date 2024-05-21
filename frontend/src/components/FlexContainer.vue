<script lang="ts" setup>
interface Props {
  flexDirection?:
    | "flex-row"
    | "flex-row-reverse"
    | "flex-col"
    | "flex-col-reverse"
}

const props = withDefaults(defineProps<Props>(), {
  flexDirection: "flex-row",
})
</script>

<template>
  <div class="flex" :class="props.flexDirection">
    <slot></slot>
  </div>
</template>
