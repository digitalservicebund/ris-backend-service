<script lang="ts" setup>
import TextButton from "@/components/input/TextButton.vue"

defineProps<{
  label: string
  modelValue: string[]
}>()

const emit = defineEmits<{
  toggle: []
}>()
</script>

<template>
  <div>
    <div class="flex flex-col gap-16">
      <div class="flex flex-col gap-4">
        <span class="ds-label-02-reg">{{ label }}</span>
        <ul class="m-0 flex flex-row flex-wrap gap-8 p-0">
          <li
            v-for="(chip, i) in modelValue"
            :key="modelValue[i]"
            class="rounded-full bg-blue-300"
            data-testid="chip"
          >
            <span
              class="overflow-hidden text-ellipsis whitespace-nowrap px-8 py-6 text-18"
              data-testid="chip-value"
              >{{ chip }}
            </span>
          </li>
        </ul>
      </div>
      <TextButton
        :aria-label="`${label} bearbeiten`"
        button-type="tertiary"
        :label="`${label} bearbeiten`"
        size="small"
        @click.stop="emit('toggle')"
      />
    </div>
  </div>
</template>
