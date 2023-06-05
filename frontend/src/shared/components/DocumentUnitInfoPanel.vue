<script lang="ts" setup>
import IconBadge, { IconBadgeProps } from "@/shared/components/IconBadge.vue"
import PropertyInfo from "@/shared/components/PropertyInfo.vue"

interface PropertyInfo {
  label: string
  value?: string
}

interface Props {
  heading?: string
  firstRow?: (PropertyInfo | IconBadgeProps)[]
  secondRow?: (PropertyInfo | IconBadgeProps)[]
}

withDefaults(defineProps<Props>(), {
  heading: "",
  firstRow: () => [],
  secondRow: () => [],
})

function isBadge(
  entry: PropertyInfo | IconBadgeProps
): entry is IconBadgeProps {
  return "icon" in entry
}
</script>

<template>
  <div
    class="bg-blue-200 border-b border-gray-400 border-solid grid h-80 items-center px-[2rem]"
    :class="{ 'h-96': secondRow.length }"
  >
    <div class="flex items-center space-x-[2rem]">
      <div class="text-30">{{ heading }}</div>
      <div v-for="entry in firstRow" :key="entry.label">
        <IconBadge
          v-if="isBadge(entry)"
          :color="entry.color"
          :icon="entry.icon"
          :value="entry.value"
        />
        <PropertyInfo
          v-else
          direction="row"
          :label="entry.label"
          :value="entry.value || ' - '"
        ></PropertyInfo>
      </div>
    </div>

    <div v-if="secondRow.length" class="flex space-x-[2rem]">
      <div v-for="entry in secondRow" :key="entry.label" class="-mt-20">
        <IconBadge
          v-if="isBadge(entry)"
          :color="entry.color"
          :icon="entry.icon"
          :value="entry.value"
        />
        <PropertyInfo
          v-else
          direction="row"
          :label="entry.label"
          :value="entry.value || ' - '"
        ></PropertyInfo>
      </div>
    </div>
  </div>
</template>
