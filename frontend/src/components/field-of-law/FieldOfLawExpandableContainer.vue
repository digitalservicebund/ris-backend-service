<script lang="ts" setup>
import { computed, nextTick, ref, watch } from "vue"
import FieldOfLawSummary from "@/components/field-of-law/FieldOfLawSummary.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import RadioInput from "@/components/input/RadioInput.vue"
import TextButton from "@/components/input/TextButton.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import IconAdd from "~icons/ic/baseline-add"

const props = defineProps<{
  fieldsOfLaw: FieldOfLaw[]
}>()

const emit = defineEmits<{
  "node:remove": [node: FieldOfLaw]
  "node:clicked": [node: FieldOfLaw]
  editingDone: [void]
  inputMethodSelected: [method: InputMethod]
}>()

const titleRef = ref<HTMLElement | null>(null)

const isExpanded = ref(false)

const inputMethod = ref(InputMethod.DIRECT)

const expandButtonLabel = computed(() => {
  return props.fieldsOfLaw.length > 0 ? "Weitere Angabe" : "Sachgebiete"
})

function removeNode(node: FieldOfLaw) {
  emit("node:remove", node)
}

function nodeClicked(node: FieldOfLaw) {
  emit("node:clicked", node)
  enterEditMode()
}

function enterEditMode() {
  isExpanded.value = true
}

async function exitEditMode() {
  isExpanded.value = false
  await nextTick()
  titleRef.value?.scrollIntoView({ block: "nearest" })
  emit("editingDone")
}

watch(
  inputMethod,
  () => {
    emit("inputMethodSelected", inputMethod.value)
  },
  { deep: true },
)
</script>
<script lang="ts">
export enum InputMethod {
  DIRECT = "direct",
  SEARCH = "search",
}
</script>
<template>
  <div class="flex w-full items-start justify-between bg-white">
    <div class="flex w-full flex-col">
      <div class="flex w-full flex-row items-center justify-between">
        <h2 ref="titleRef" class="ds-label-01-bold">Sachgebiete</h2>
      </div>
      <FieldOfLawSummary
        :fields-of-law="fieldsOfLaw"
        @node:clicked="nodeClicked"
        @node:remove="removeNode"
      />
    </div>
  </div>

  <div v-if="isExpanded" class="flex flex-col items-start gap-24">
    <div class="flex w-full flex-row justify-between">
      <div class="flex flex-row gap-8">
        <InputField
          id="direct"
          label="Direkteingabe"
          :label-position="LabelPosition.RIGHT"
          @click="() => (inputMethod = InputMethod.DIRECT)"
        >
          <RadioInput v-model="inputMethod" size="small" value="direct" />
        </InputField>

        <InputField
          id="search"
          label="Sachgebietsuche"
          :label-position="LabelPosition.RIGHT"
          @click="() => (inputMethod = InputMethod.SEARCH)"
        >
          <RadioInput v-model="inputMethod" size="small" value="search" />
        </InputField>
      </div>
      <TextButton
        button-type="primary"
        label="Fertig"
        size="small"
        @click="exitEditMode"
      />
    </div>
    <slot />
  </div>

  <TextButton
    v-else
    button-type="tertiary"
    :icon="IconAdd"
    :label="expandButtonLabel"
    size="small"
    @click="enterEditMode"
  />
</template>
