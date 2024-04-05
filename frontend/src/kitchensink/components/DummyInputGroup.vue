<script lang="ts" setup>
import { ref, watch, onBeforeUnmount } from "vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextInput from "@/components/input/TextInput.vue"
import DummyListItem from "@/kitchensink/domain/dummyListItem"

const props = defineProps<{
  modelValue?: DummyListItem
}>()

const emit = defineEmits<{
  "update:modelValue": [value: DummyListItem]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [void]
}>()

const lastSavedModelValue = ref(new DummyListItem({ ...props.modelValue }))
const listEntry = ref(new DummyListItem({ ...props.modelValue }))

async function addListEntry() {
  emit("update:modelValue", listEntry.value as DummyListItem)
  emit("addEntry")
}

watch(
  () => props.modelValue,
  () => {
    listEntry.value = new DummyListItem({ ...props.modelValue })
    lastSavedModelValue.value = new DummyListItem({ ...props.modelValue })
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  if (listEntry.value.isEmpty) emit("removeEntry")
})
</script>

<template>
  <div class="flex flex-col gap-24">
    <InputField id="listItemText" class="flex-col" label="Editier Input">
      <TextInput
        id="listItemText"
        v-model="listEntry.text"
        aria-label="Editier Input"
        class="ds-input-medium"
        size="medium"
      ></TextInput>
    </InputField>

    <div class="flex w-full flex-row justify-between">
      <div>
        <div>
          <TextButton
            aria-label="Listeneintrag speichern"
            button-type="tertiary"
            class="mr-24"
            :disabled="listEntry.isEmpty"
            label="Übernehmen"
            size="small"
            @click.stop="addListEntry"
          />
          <TextButton
            v-if="!lastSavedModelValue.isEmpty"
            aria-label="Abbrechen"
            button-type="ghost"
            label="Abbrechen"
            size="small"
            @click.stop="emit('cancelEdit')"
          />
        </div>
      </div>
      <TextButton
        v-if="!lastSavedModelValue.isEmpty"
        aria-label="Eintrag löschen"
        button-type="destructive"
        label="Eintrag löschen"
        size="small"
        @click.stop="emit('removeEntry')"
      />
    </div>
  </div>
</template>
