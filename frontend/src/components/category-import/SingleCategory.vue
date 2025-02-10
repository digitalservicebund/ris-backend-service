<script setup lang="ts">
import { computed, ref } from "vue"
import IconBadge from "@/components/IconBadge.vue"
import InfoModal from "@/components/InfoModal.vue"
import TextButton from "@/components/input/TextButton.vue"
import { ValidationError } from "@/components/input/types"
import IconAdd from "~icons/ic/baseline-add"
import IconCheck from "~icons/ic/baseline-check"
import IconInfo from "~icons/ic/outline-info"

const props = defineProps<{
  label: string
  errorMessage: ValidationError | undefined
  hasContent: boolean
  handleImport: () => void
  importable: boolean
}>()

const importSuccess = ref(false)
const errorMessage = computed(() =>
  props.errorMessage ? props.errorMessage.message : undefined,
)

function handleClick() {
  props.handleImport()
  importSuccess.value = true
  setTimeout(() => (importSuccess.value = false), 7000)
}
</script>

<template>
  <div class="flex flex-col gap-16">
    <div class="flex flex-row items-center gap-16">
      <TextButton
        :aria-label="label + ' übernehmen'"
        button-type="primary"
        :disabled="!hasContent || !importable"
        :icon="IconAdd"
        size="medium"
        @click="handleClick"
      />
      <span
        class="ds-label-01-reg"
        :class="hasContent && importable ? 'text-blue-800' : 'text-gray-900'"
        >{{ label }}</span
      >
      <IconBadge
        v-if="!hasContent"
        background-color="bg-blue-300"
        color="text-blue-900"
        :data-testid="label + '-empty'"
        :icon="IconInfo"
        label="Quellrubrik leer"
      />
      <IconBadge
        v-if="importSuccess"
        background-color="bg-green-300"
        color="text-green-800"
        :data-testid="label + '-success'"
        :icon="IconCheck"
        label="Übernommen"
      />
      <IconBadge
        v-else-if="!importable"
        background-color="bg-blue-300"
        color="text-blue-900"
        :data-testid="label + '-empty'"
        :icon="IconInfo"
        label="Zielrubrik ausgefüllt"
      />
    </div>
    <InfoModal
      v-if="errorMessage"
      :aria-label="errorMessage"
      :title="errorMessage"
    />
  </div>
</template>
