<script setup lang="ts">
import IconBadge from "@/components/IconBadge.vue"
import InfoModal from "@/components/InfoModal.vue"
import TextButton from "@/components/input/TextButton.vue"
import IconAdd from "~icons/ic/baseline-add"
import IconCheck from "~icons/ic/baseline-check"
import IconInfo from "~icons/ic/outline-info"

defineProps<{
  label: string
  importable: boolean
  importSuccess: boolean
  errorMessage?: string
}>()

const emits = defineEmits<{
  import: [void]
}>()
</script>

<template>
  <div class="flex flex-row items-center gap-16">
    <TextButton
      :aria-label="label + ' übernehmen'"
      button-type="primary"
      :disabled="!importable"
      :icon="IconAdd"
      size="medium"
      @click="emits('import')"
    />
    <span
      class="ds-label-01-reg"
      :class="importable ? 'text-blue-800' : 'text-gray-900'"
      >{{ label }}</span
    >
    <IconBadge
      v-if="!importable"
      background-color="bg-blue-300"
      color="text-blue-900"
      :icon="IconInfo"
      label="Quellrubrik leer"
    />
    <IconBadge
      v-if="importSuccess"
      background-color="bg-green-300"
      color="text-green-800"
      :icon="IconCheck"
      label="Übernommen"
    />
  </div>
  <InfoModal
    v-if="errorMessage"
    :aria-label="errorMessage"
    :title="errorMessage"
  />
</template>
