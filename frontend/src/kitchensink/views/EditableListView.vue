<script lang="ts" setup>
import { ref } from "vue"
import EditableList from "@/shared/components/EditableList.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

const listWithEntries = ref(["list entry 1", "list entry 2"])
const emptyList = ref([])
const defaultValue = ""
</script>

<template>
  <div class="mb-56">
    <h2 class="heading-02-regular mb-[1rem]">Editable List with entries</h2>
    <EditableList
      v-model="listWithEntries"
      :default-value="defaultValue"
      :edit-component="TextInput"
    />
  </div>

  <h2 class="heading-02-regular mb-[1rem]">Editable List without entries</h2>
  <EditableList
    v-model="emptyList"
    :default-value="defaultValue"
    :edit-component="TextInput"
    transformer=""
  />
</template>
