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
    class="bg-blue-200 border-b border-gray-400 border-solid flex flex-row items-center justify-between px-[2rem] sticky top-0 z-10"
    :class="{ 'h-[8rem]': secondRow.length }"
  >
    <div class="-mt-1 flex flex-col gap-24 h-80 justify-center">
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
  </div>
</template>
