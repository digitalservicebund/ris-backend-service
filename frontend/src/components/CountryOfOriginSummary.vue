<script lang="ts" setup>
import IconBadge from "@/components/IconBadge.vue"
import CountryOfOrigin from "@/domain/countryOfOrigin"
import IcOutlineLocationOn from "~icons/ic/outline-location-on"

const props = defineProps<{
  data: CountryOfOrigin
}>()
</script>

<template>
  <div class="flex w-full justify-between">
    <div class="flex flex-row items-center">
      <IcOutlineLocationOn class="mr-8" />

      <div class="ris-label1-regular mr-8 max-w-[calc(30vw)] truncate">
        <IconBadge
          v-if="props.data.legacyValue"
          background-color="bg-yellow-600"
          class="inline"
          label="Altwert"
        />
        {{ props.data.renderSummary }}
      </div>
    </div>
  </div>
</template>
