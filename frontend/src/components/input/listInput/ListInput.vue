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
}>()

const list = ref(props.modelValue)
const editMode = ref(true)
const sortAlphabetically = ref(false)

const listInputValue = computed({
  get: () => (props.modelValue ? props.modelValue.join("\n") : ""), // Join array with newlines for textarea
  set: (newValues: string) => {
    // split the text by newline, trim each line, filter out empty lines, and set back into the array
    list.value = newValues
      .split("\n")
      .map((listitem) => listitem.trim())
      .filter((listitem) => listitem !== "")

    // sort alphabetically if option set
    if (sortAlphabetically.value && list.value) {
      list.value = list.value.sort((a: string, b: string) => a.localeCompare(b))
    }

    list.value = [...new Set(list.value)] as string[] //remove duplicates

    emit("update:modelValue", list.value)
    if (!!list.value?.length) editMode.value = false
    sortAlphabetically.value = false
  },
})

function toggleEditMode() {
  editMode.value = !editMode.value
}

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
