<script lang="ts" setup>
import { ref } from "vue"
import TextEditor from "../components/input/TextEditor.vue"
import TextButton from "@/components/input/TextButton.vue"
import { TextAreaInputAttributes } from "@/components/input/types"
import IconAdd from "~icons/material-symbols/add"

interface Props {
  name?: string
  label?: string
  value?: string
  collapsedByDefault?: boolean
  fieldSize: TextAreaInputAttributes["fieldSize"]
}

const props = withDefaults(defineProps<Props>(), {
  name: undefined,
  label: undefined,
  value: undefined,
  collapsedByDefault: false,
  fieldSize: "medium",
})

const emit = defineEmits<{
  "update-value": [value: string]
}>()

const collapsed = ref<boolean>(props.value ? false : props.collapsedByDefault)

function expand() {
  collapsed.value = true
}
</script>

<template>
  <div class="flex flex-col gap-24">
    <div v-if="!collapsed">
      <label class="ds-label-02-reg mb-4" :for="props.name">{{
        props.label
      }}</label>

      <TextEditor
        :id="props.name"
        :aria-label="props.label"
        class="shadow-blue focus-within:shadow-focus hover:shadow-hover"
        editable
        :field-size="props.fieldSize"
        :value="props.value"
        @update-value="emit('update-value', $event)"
      />
    </div>
    <TextButton
      v-else
      button-type="tertiary"
      class="m-32"
      :icon="IconAdd"
      :label="props.label"
      size="small"
      @click="expand"
    />
  </div>
</template>
