<script lang="ts" setup>
import PropertyInfo from "@/shared/components/PropertyInfo.vue"

interface PropertyInfo {
  label: string
  value?: string
}

interface Props {
  heading?: string
  firstRow?: PropertyInfo[]
  secondRow?: PropertyInfo[]
}

withDefaults(defineProps<Props>(), {
  heading: "",
  firstRow: () => [],
  secondRow: () => [],
})
</script>

<template>
  <div
    class="bg-blue-200 border-b border-gray-400 border-solid grid h-96 items-center px-[2rem]"
  >
    <div class="flex items-center space-x-[2rem]">
      <div class="text-30">{{ heading }}</div>
      <div v-for="entry in firstRow" :key="entry.label" class="grow">
        <PropertyInfo
          direction="row"
          :label="entry.label"
          :value="entry.value || ' - '"
        ></PropertyInfo>
      </div>
    </div>

    <div class="flex space-x-[2rem]">
      <div v-for="entry in secondRow" :key="entry.label" class="-mt-20">
        <PropertyInfo
          direction="row"
          :label="entry.label"
          :value="entry.value || ' - '"
        ></PropertyInfo>
      </div>
    </div>
  </div>
</template>
