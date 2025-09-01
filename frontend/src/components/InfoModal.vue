<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { InfoStatus } from "@/components/enumInfoStatus"
import InfoModalIcon from "@/components/InfoModalIcon.vue"

const props = withDefaults(
  defineProps<{
    ariaLabel?: string
    title: string
    description?: string | string[]
    status?: InfoStatus
    link?: {
      displayText: string
      url?: string
    }
  }>(),
  {
    ariaLabel: "Infomodal",
    description: "",
    status: InfoStatus.ERROR,
    link: undefined,
  },
)

const titleRef = ref(props.title)
const descriptionRef = ref<string | string[]>(props.description ?? "")
const statusRef = ref(props.status ?? InfoStatus.ERROR)

type ModalAttribute = {
  borderClass: string
  backgroundColorClass: string
}
const staticContainerClass =
  "border-l-[0.125rem] flex gap-[0.625rem] px-[1.25rem] py-[1.125rem] w-full"
const modalAttribute = computed((): ModalAttribute => {
  if (statusRef.value === InfoStatus.SUCCEED) {
    return {
      borderClass: "border-l-green-700",
      backgroundColorClass: "bg-white",
    }
  } else if (statusRef.value === InfoStatus.INFO) {
    return {
      borderClass: "border-l-blue-800",
      backgroundColorClass: "bg-white",
    }
  }
  return {
    borderClass: "border-l-red-800",
    backgroundColorClass: "bg-red-200",
  }
})

const isArray = computed(() => {
  return Array.isArray(props.description)
})

const ariaLabelIcon = props.title + " icon"

watch(
  props,
  () => {
    titleRef.value = props.title
    descriptionRef.value = props.description ?? ""
  },
  { immediate: true, deep: true },
)
</script>

<template>
  <div
    :aria-label="ariaLabel"
    class="flex items-center"
    :class="[
      staticContainerClass,
      modalAttribute.borderClass,
      modalAttribute.backgroundColorClass,
    ]"
  >
    <InfoModalIcon :aria-label="ariaLabelIcon" :status="statusRef" />

    <div class="flex flex-col">
      <span class="ris-label2-bold">{{ titleRef }}</span>
      <div
        v-if="isArray && descriptionRef.length > 1"
        class="ris-label2-regular"
      >
        <ul class="m-0 list-disc ps-20">
          <li v-for="(desc, index) in descriptionRef" :key="index">
            {{ desc }}
          </li>
        </ul>
      </div>
      <div v-else class="flex flex-row items-center">
        <span class="ris-label2-regular"
          >{{ isArray ? descriptionRef[0] : descriptionRef }}
        </span>
        <a
          v-if="link !== undefined"
          class="ris-link2-regular whitespace-nowrap no-underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
          :href="link.url"
          rel="noopener noreferrer"
          target="_blank"
          >{{ link.displayText }}</a
        >
        <div v-if="link !== undefined">.</div>
      </div>
    </div>
  </div>
</template>
