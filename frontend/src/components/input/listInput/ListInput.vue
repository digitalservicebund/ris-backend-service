<script lang="ts" setup>
import { ref, computed, onMounted } from "vue"
import ListInputDisplay from "@/components/input/listInput/ListInputDisplay.vue"
import ListInputEdit from "@/components/input/listInput/ListInputEdit.vue"

const props = defineProps<{
  label: string
  modelValue: string[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: string[]]
  reset: []
}>()

const list = ref(props.modelValue)
const editMode = ref(true)
const sortAlphabetically = ref(false)

/**
 * Computed property to manage the list input value.
 * - Get: Joins the array into a newline-separated string for textarea display.
 * - Set: Updates the list based on the textarea input, trims, filters out empty values, removes duplicates,
 *        and sorts alphabetically if the option is enabled.
 * @returns {string} The joined string of list items separated by newlines.
 */
const listInputValue = computed({
  get: () => (props.modelValue ? props.modelValue.join("\n") : ""),
  set: (newValues: string) => {
    list.value = newValues
      .split("\n")
      .map((listitem) => listitem.trim())
      .filter((listitem) => listitem !== "")

    // Sort alphabetically if the option is set
    if (sortAlphabetically.value && list.value) {
      list.value = list.value.sort((a: string, b: string) => a.localeCompare(b))
    }

    // Remove duplicates
    list.value = [...new Set(list.value)] as string[]

    // Emit the updated value
    emit("update:modelValue", list.value)

    // Emit reset if the list is empty, to show the category wrapper again.
    if (!!list.value?.length) {
      editMode.value = false
    } else emit("reset")

    // Reset sorting option
    sortAlphabetically.value = false
  },
})

/**
 * Emit reset if the list is empty, to show the category wrapper again.
 * Otherwise toggles between edit mode and display mode.
 */
function toggleEditMode() {
  // Reset sorting option
  sortAlphabetically.value = false
  if (!!list.value?.length) {
    editMode.value = !editMode.value
  } else emit("reset")
}

/**
 * Initializes the edit mode based on whether the list is empty or not.
 */
onMounted(() => {
  editMode.value = !props.modelValue.length
})
</script>

<template>
  <div>
    <ListInputEdit
      v-if="editMode"
      v-model="listInputValue"
      :label="label"
      :list-item-count="modelValue.length"
      :sort-alphabetically="sortAlphabetically"
      @toggle="toggleEditMode"
      @toggle-sorting="sortAlphabetically = !sortAlphabetically"
    />
    <ListInputDisplay
      v-else
      v-model="list"
      :label="label"
      @toggle="toggleEditMode"
    />
  </div>
</template>
