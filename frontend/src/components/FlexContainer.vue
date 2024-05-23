<script lang="ts" setup>
interface Props {
  flexDirection?:
    | "flex-row"
    | "flex-row-reverse"
    | "flex-col"
    | "flex-col-reverse"
  alignItems?:
    | "items-start"
    | "items-end"
    | "items-center"
    | "items-baseline"
    | "items-stretch"
  justifyContent?:
    | "justify-start"
    | "justify-end"
    | "justify-center"
    | "justify-between"
    | "justify-around"
    | "justify-evenly"
}

const props = withDefaults(defineProps<Props>(), {
  flexDirection: "flex-row",
  alignItems: "items-stretch",
  justifyContent: "justify-start",
})
</script>

<template>
  <div
    class="flex"
    :class="[props.flexDirection, props.alignItems, props.justifyContent]"
  >
    <slot></slot>
  </div>
</template>
