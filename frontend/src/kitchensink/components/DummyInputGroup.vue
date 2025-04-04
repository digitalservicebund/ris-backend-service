<script lang="ts" setup>
import Button from "primevue/button"
import InputText from "primevue/inputtext"
import { ref, watch, onBeforeUnmount } from "vue"
import InputField from "@/components/input/InputField.vue"
import DummyListItem from "@/kitchensink/domain/dummyListItem"

const props = defineProps<{
  modelValue?: DummyListItem
}>()

const emit = defineEmits<{
  "update:modelValue": [value: DummyListItem]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [value?: boolean]
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
      <InputText
        id="listItemText"
        v-model="listEntry.text"
        aria-label="Editier Input"
        fluid
        size="small"
      ></InputText>
    </InputField>

    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <Button
            aria-label="Listeneintrag speichern"
            :disabled="listEntry.isEmpty"
            label="Übernehmen"
            severity="secondary"
            size="small"
            @click.stop="addListEntry"
          ></Button>
          <Button
            v-if="!lastSavedModelValue.isEmpty"
            aria-label="Abbrechen"
            label="Abbrechen"
            size="small"
            text
            @click.stop="emit('cancelEdit')"
          ></Button>
        </div>
      </div>
      <Button
        v-if="!lastSavedModelValue.isEmpty"
        aria-label="Eintrag löschen"
        label="Eintrag löschen"
        severity="danger"
        size="small"
        @click.stop="emit('removeEntry', true)"
      ></Button>
    </div>
  </div>
</template>
