<script lang="ts" setup>
import PropertyInfo from "@/components/PropertyInfo.vue"

interface PropertyInfo {
  label: string
  value?: string
}

interface Props {
  heading?: string
  propertyInfos?: PropertyInfo[]
}

withDefaults(defineProps<Props>(), {
  heading: "",
  propertyInfos: () => [],
})
</script>

<template>
  <div
    class="bg-blue-200 border-b border-gray-400 border-solid flex gap-20 h-80 items-center px-[2rem]"
  >
    <div class="text-30">{{ heading }}</div>

    <div v-for="entry in propertyInfos" :key="entry.label" class="grow">
      <PropertyInfo
        direction="row"
        :label="entry.label"
        :value="entry.value || ' - '"
      ></PropertyInfo>
    </div>
  </div>
</template>
