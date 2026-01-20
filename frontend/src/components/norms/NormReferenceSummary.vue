<script lang="ts" setup>
import Button from "primevue/button"
import IconBadge from "@/components/IconBadge.vue"
import NormReference from "@/domain/normReference"
import IconError from "~icons/ic/baseline-error"
import IconBook from "~icons/material-symbols/book-2"
import IconBreakingNews from "~icons/material-symbols/breaking-news"
import IconClear from "~icons/material-symbols/close-small"
import IconArrowRight from "~icons/material-symbols/subdirectory-arrow-right"

const props = defineProps<{
  data: NormReference
}>()

const emit = defineEmits<{
  "remove-single-norm": [payload: { data: NormReference; index: number }]
}>()
</script>

<template>
  <div class="flex w-full justify-between">
    <div class="flex w-full flex-row items-center">
      <div
        v-if="data.singleNorms?.length === 1"
        class="flex flex-row items-center"
      >
        <component :is="IconBook" class="mr-8 ml-2" />
        <div class="ris-label1-regular mr-8">
          {{ data.renderSummary + ", " + data.singleNorms[0].renderSummary }}
        </div>
        <div
          v-if="data.singleNorms[0].legalForce"
          class="flex flex-row items-center"
        >
          {{ " | " }}
          <component :is="IconBreakingNews" class="mx-8" />
          {{ data.singleNorms[0].renderLegalForce }}
          <IconBadge
            v-if="data.singleNorms[0].legalForce?.hasMissingRequiredFields"
            background-color="bg-red-300"
            class="ml-8"
            :icon="IconError"
            icon-color="text-red-900"
            label="Fehlende Daten"
          />
        </div>
        <IconBadge
          v-if="props.data?.hasAmbiguousNormReference"
          background-color="bg-red-300"
          :icon="IconError"
          icon-color="text-red-900"
          label="Mehrdeutiger Verweis"
        />
      </div>
      <div v-else class="flex w-full flex-col gap-8">
        <div class="flex min-h-40 flex-row items-center">
          <component :is="IconBook" class="mr-8 ml-2" />
          <div class="ris-label1-regular mr-8">
            {{ data.renderSummary }}
          </div>
          <IconBadge
            v-if="data.hasAmbiguousNormReference"
            background-color="bg-red-300"
            :icon="IconError"
            icon-color="text-red-900"
            label="Mehrdeutiger Verweis"
          />
        </div>

        <div v-if="data.singleNorms?.length" class="flex flex-col gap-8">
          <div v-for="(singleNorm, index) in data.singleNorms" :key="index">
            <div
              v-if="!singleNorm.isEmpty"
              class="-mr-48 flex flex-row items-center justify-between pr-48 hover:bg-blue-200"
            >
              <div class="flex flex-row items-center">
                <component :is="IconArrowRight" class="mr-8 ml-4" />
                {{
                  data.renderSummary +
                  ", " +
                  data.singleNorms[index].renderSummary
                }}
                <div
                  v-if="data.singleNorms[index].legalForce"
                  class="flex flex-row items-center"
                >
                  {{ " | " }}
                  <component :is="IconBreakingNews" class="mx-8" />
                  {{ data.singleNorms[index].renderLegalForce }}
                  <IconBadge
                    v-if="
                      data.singleNorms[index].legalForce
                        ?.hasMissingRequiredFields
                    "
                    background-color="bg-red-300"
                    class="ml-8"
                    :icon="IconError"
                    icon-color="text-red-900"
                    label="Fehlende Daten"
                  />
                </div>
              </div>
              <Button
                v-tooltip.bottom="{
                  value: 'Einzelnorm entfernen',
                }"
                aria-label="Einzelnorm löschen"
                class="-mr-48 self-end"
                size="small"
                text
                @click.stop="
                  emit('remove-single-norm', { data: props.data, index: index })
                "
              >
                <template #icon>
                  <IconClear class="order-last" />
                </template>
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
