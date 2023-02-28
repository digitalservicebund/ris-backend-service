<script lang="ts" setup>
import ChipsInput from "@/components/ChipsInput.vue"
</script>

<template>
  <div class="bg-gray-100 flex flex-col p-20">
    <h1 class="heading-03-regular mb-[1rem]">Schlagwörter</h1>
    <div class="flex flex-row">
      <div class="flex-1">
        <ChipsInput
          id="keywords"
          aria-label="Schlagwörter"
          as-bottom-list
        ></ChipsInput>
      </div>
    </div>
  </div>
</template>
