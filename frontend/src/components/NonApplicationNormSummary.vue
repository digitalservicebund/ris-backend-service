<script lang="ts" setup>
import NonApplicationNorm from "@/domain/nonApplicationNorm"
import IconBook from "~icons/material-symbols/book-2"
import IconArrowRight from "~icons/material-symbols/subdirectory-arrow-right"

defineProps<{
  data: NonApplicationNorm
}>()
</script>

<template>
  <div class="flex w-full justify-between">
    <div class="flex flex-row items-center">
      <div
        v-if="data.singleNorms?.length === 1"
        class="flex flex-row items-center"
      >
        <component :is="IconBook" class="mr-8" />
        <div class="ris-label1-regular mr-8">
          {{ data.renderSummary + ", " + data.singleNorms[0].renderSummary }}
        </div>
      </div>
      <div v-else class="flex flex-col gap-24">
        <div class="flex flex-row items-center">
          <component :is="IconBook" class="mr-8" />
          <div class="ris-label1-regular mr-8">
            {{ data.renderSummary }}
          </div>
        </div>

        <div v-if="data.singleNorms?.length" class="flex flex-col gap-24">
          <div v-for="(singleNorm, index) in data.singleNorms" :key="index">
            <div v-if="!singleNorm.isEmpty" class="flex flex-row items-center">
              <component :is="IconArrowRight" class="mr-8" />
              {{
                data.renderSummary +
                ", " +
                data.singleNorms[index].renderSummary
              }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
