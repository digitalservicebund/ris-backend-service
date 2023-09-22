<script lang="ts" setup>
import { RouteLocationRaw } from "vue-router"

withDefaults(
  defineProps<{
    title: string
    to: RouteLocationRaw
    marginLevel?: number
  }>(),
  {
    marginLevel: 0,
  },
)
</script>

<template>
  <div
    class="bg-gray-100 hover:bg-blue-300 hover:underline active:bg-blue-500 active:underline"
  >
    <router-link
      class="ds-label-02-reg block py-4"
      :style="{ paddingLeft: `${marginLevel * 24 + 4}px` }"
      :to="to"
    >
      {{ title }}
    </router-link>
  </div>
</template>
