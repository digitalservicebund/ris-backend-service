<script lang="ts" setup>
import PropertyInfo from "@/shared/components/PropertyInfo.vue"

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
    class="flex h-80 items-center gap-20 border-b border-solid border-gray-400 bg-blue-200 px-[2rem]"
  >
    <div
      class="flex items-baseline space-x-[2rem]"
      data-testid="norm-unit-info-panel-items"
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
  </div>
</template>
