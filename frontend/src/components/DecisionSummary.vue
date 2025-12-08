<script lang="ts" setup>
import { computed, toRaw } from "vue"
import type { Component } from "vue"
import { DisplayMode } from "@/components/enumDisplayMode"
import IconBadge from "@/components/IconBadge.vue"
import { useScroll } from "@/composables/useScroll"
import { useStatusBadge } from "@/composables/useStatusBadge"
import { PublicationStatus } from "@/domain/publicationStatus"
import BaselineArrowOutward from "~icons/ic/baseline-arrow-outward"

interface Props {
  summary: string
  status?: PublicationStatus
  documentNumber?: string
  displayMode?: DisplayMode
  icon?: Component
  linkClickable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  displayMode: DisplayMode.TAB,
  status: undefined,
  documentNumber: undefined,
  icon: undefined,
  linkClickable: true, // eslint-disable-line vue/no-boolean-default
})
const { openSidePanelAndScrollToSection } = useScroll()
const statusBadge = computed(() => useStatusBadge(props.status).value)

const summary = computed(() =>
  props.status ? `${props.summary},  ` : props.summary,
)

const divider = computed(() => (props.documentNumber ? ` | ` : undefined))
</script>

<template>
  <div class="flex justify-between">
    <div class="flex flex-row items-center">
      <component :is="icon" v-if="icon" class="mr-8 min-w-24" />
      <span class="mr-8" :data-testid="'decision-summary-' + documentNumber">
        <span>
          {{ summary }}
          <IconBadge
            v-if="status"
            :background-color="statusBadge.backgroundColor"
            class="ml-4 inline-block"
            :icon="toRaw(statusBadge.icon)"
            :label="statusBadge.label"
          />
          {{ divider }}

          <span v-if="!linkClickable"> {{ documentNumber }}</span>
          <!-- open preview in new tab -->
          <RouterLink
            v-else-if="documentNumber && props.displayMode === DisplayMode.TAB"
            class="ris-link1-bold whitespace-nowrap no-underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
            :data-testid="'document-number-link-' + documentNumber"
            tabindex="-1"
            target="_blank"
            :to="{
              name: 'caselaw-documentUnit-documentNumber-preview',
              params: { documentNumber: documentNumber },
            }"
          >
            {{ documentNumber }}
            <BaselineArrowOutward class="mb-4 inline w-24" />
          </RouterLink>
          <!-- or open preview in sidepanel -->
          <span
            v-else-if="
              documentNumber && props.displayMode === DisplayMode.SIDEPANEL
            "
          >
            <button
              class="ris-link1-bold whitespace-nowrap no-underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
              :data-testid="'document-number-link-' + documentNumber"
              @click="openSidePanelAndScrollToSection(documentNumber)"
            >
              {{ documentNumber }}
              <BaselineArrowOutward class="mb-4 inline w-24" />
            </button>
          </span>
        </span>
      </span>
    </div>
  </div>
</template>
