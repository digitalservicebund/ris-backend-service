<script lang="ts" setup>
import Button from "primevue/button"

defineProps<{
  label: string
  modelValue: string[]
}>()

const emit = defineEmits<{
  toggle: []
}>()
</script>

<template>
  <div class="flex scroll-m-64 flex-col gap-16">
    <div class="flex flex-col gap-4">
      <span class="ris-label2-regular">{{ label }}</span>
      <ul class="m-0 flex flex-row flex-wrap gap-8 p-0">
        <li
          v-for="chip in modelValue"
          :key="chip"
          class="rounded-full bg-blue-300"
          data-testid="chip"
        >
          <span
            class="overflow-hidden px-8 py-6 text-ellipsis whitespace-nowrap"
            :data-testid="`ListInputDisplay_${label}_${chip}`"
            >{{ chip }}
          </span>
        </li>
      </ul>
    </div>
    <Button
      :aria-label="`${label} bearbeiten`"
      class="self-start"
      :label="`${label} bearbeiten`"
      severity="secondary"
      size="small"
      @click.stop="emit('toggle')"
    ></Button>
  </div>
</template>
