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
        <span v-if="props.data.country">
          {{ props.data.country.identifier }} {{ props.data.country.text }}
        </span>
        <span v-if="props.data.country && props.data.fieldOfLaw">, </span>
        <span v-if="props.data.fieldOfLaw">
          {{ props.data.fieldOfLaw.identifier }}
          {{ props.data.fieldOfLaw.text }}
        </span>
      </div>

      <div
        v-if="props.data.legacyValue"
        class="ris-label1-regular mr-8 max-w-[calc(30vw)] truncate"
      >
        <IconBadge
          background-color="bg-yellow-600"
          class="inline"
          label="Altwert"
        />
        {{ props.data.legacyValue }}
      </div>
    </div>
  </div>
</template>
