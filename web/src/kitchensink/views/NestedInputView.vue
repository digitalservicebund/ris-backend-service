<script lang="ts" setup>
import NestedInput from "@/components/NestedInput.vue"
import { defineDateField, NestedInputAttributes } from "@/domain"

const nestedInputFields: NestedInputAttributes["fields"] = {
  parent: defineDateField(
    "decisionDate",
    "Entscheidungsdatum",
    "Entscheidungsdatum",
    true,
    undefined
  ),
  child: defineDateField(
    "decisionDate",
    "Entscheidungsdatum",
    "Entscheidungsdatum",
    true,
    undefined
  ),
}
</script>

<template>
  <div class="flex flex-col gap-y-20 h-auto w-1/2">
    <h1 class="font-bold text-24">Text Input</h1>

    <div>
      <NestedInput
        aria-label="Nested Input"
        :fields="nestedInputFields"
      ></NestedInput>
    </div>
  </div>
</template>
