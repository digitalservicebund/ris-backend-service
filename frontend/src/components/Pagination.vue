<!-- eslint-disable vue/multi-word-component-names -->
<script setup lang="ts">
import Button from "primevue/button"
import FlexContainer from "@/components/FlexContainer.vue"
import IconArrowBack from "~icons/ic/baseline-arrow-back"
import IconArrowForward from "~icons/ic/baseline-arrow-forward"

const props = withDefaults(
  defineProps<{
    page?: Page<any> // eslint-disable-line @typescript-eslint/no-explicit-any
    navigationPosition?: "top" | "bottom"
    isLoading?: boolean
  }>(),
  { page: undefined, navigationPosition: "top", isLoading: false },
)

const emits = defineEmits<(e: "updatePage", page: number) => void>()

async function nextPage(): Promise<void> {
  if (props.page && !props.page.last) {
    emits("updatePage", props.page.number + 1)
  }
}

async function previousPage(): Promise<void> {
  if (props.page && !props.page?.first) {
    emits("updatePage", props.page.number - 1)
  }
}
</script>

<script lang="ts">
export type Page<T> = {
  content: T[]
  size: number
  number: number
  numberOfElements: number
  totalElements?: number
  totalPages?: number
  first: boolean
  last: boolean
  empty: boolean
}
</script>

<template>
  <slot v-if="props.navigationPosition == 'bottom'"></slot>
  <FlexContainer
    v-if="page?.content && !page?.empty && !isLoading"
    class="mt-20 mb-24 px-24"
    flex-direction="flex-row"
    justify-content="justify-between"
  >
    <Button
      v-if="!(page.first && page.last)"
      aria-label="vorherige Ergebnisse"
      :disabled="page?.first"
      label="Zurück"
      severity="secondary"
      size="small"
      @click="previousPage"
      @keydown.enter="previousPage"
      ><template #icon>
        <IconArrowBack />
      </template>
    </Button>
    <FlexContainer
      class="w-full"
      flex-direction="flex-row"
      flex-wrap="flex-wrap"
      justify-content="justify-center"
    >
      <b v-if="!(page.first && page.last)"
        >Seite {{ page.number + 1
        }}{{ page.totalPages ? " von " + page.totalPages : "" }}:
      </b>
      <span>&nbsp;</span>
      <FlexContainer flex-direction="flex-row">
        {{
          page.numberOfElements == 1
            ? page.numberOfElements + " Ergebnis"
            : page.numberOfElements + " Ergebnisse"
        }}
        {{ page.first && page.last ? "gefunden" : "angezeigt" }}
      </FlexContainer>
    </FlexContainer>
    <Button
      v-if="!(page.first && page.last)"
      aria-label="nächste Ergebnisse"
      :disabled="page?.last"
      icon-position="right"
      label="Weiter"
      severity="secondary"
      size="small"
      @click="nextPage"
      @keydown.enter="nextPage"
      ><template #icon>
        <IconArrowForward class="order-last" />
      </template>
    </Button>
  </FlexContainer>
  <slot v-if="props.navigationPosition == 'top'"></slot>
</template>
