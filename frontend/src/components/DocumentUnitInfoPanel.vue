<script lang="ts" setup>
import dayjs from "dayjs"
import { computed, ref, toRaw, watchEffect } from "vue"
import IconBadge, { IconBadgeProps } from "@/components/IconBadge.vue"
import PropertyInfo from "@/components/PropertyInfo.vue"
import SaveButton from "@/components/SaveDocumentUnitButton.vue"
import { useStatusBadge } from "@/composables/useStatusBadge"
import DocumentUnit from "@/domain/documentUnit"
import { ServiceResponse } from "@/services/httpClient"

interface PropertyInfoType {
  label: string
  value?: string
}

interface Props {
  documentUnit: DocumentUnit
  heading?: string
  saveCallback?: () => Promise<ServiceResponse<void>>
}

const props = withDefaults(defineProps<Props>(), {
  heading: "",
  saveCallback: undefined,
})

const fileNumberInfo = computed(
  () => props.documentUnit?.coreData.fileNumbers?.[0],
)

const decisionDateInfo = computed(() =>
  props.documentUnit?.coreData.decisionDate
    ? dayjs(props.documentUnit?.coreData.decisionDate).format("DD.MM.YYYY")
    : undefined,
)

const documentationOffice = computed(
  () => props.documentUnit?.coreData.documentationOffice?.abbreviation,
)

const courtInfo = computed(() => props.documentUnit?.coreData.court?.label)

const statusBadge = ref(useStatusBadge(props.documentUnit?.status).value)

const firstRow = computed(() => [
  ...(statusBadge.value ? [statusBadge.value] : []),
  {
    label: "Dokumentationsstelle",
    value: documentationOffice.value,
  },
])

const secondRow = computed(() => [
  { label: "Aktenzeichen", value: fileNumberInfo.value },
  { label: "Entscheidungsdatum", value: decisionDateInfo.value },
  { label: "Gericht", value: courtInfo.value },
])

watchEffect(() => {
  statusBadge.value = useStatusBadge(props.documentUnit?.status).value
})

function isBadge(
  entry: PropertyInfoType | IconBadgeProps,
): entry is IconBadgeProps {
  return "icon" in entry
}
</script>

<template>
  <div
    class="sticky top-0 z-30 flex flex-row items-center justify-between border-b border-solid border-gray-400 bg-blue-200 px-[2rem]"
    :class="{ 'h-[8rem]': secondRow.length }"
  >
    <div class="-mt-1 flex h-80 flex-col justify-center gap-24">
      <div
        class="flex items-center space-x-[2rem]"
        data-testid="document-unit-info-panel-items"
      >
        <h1 class="text-30">{{ heading }}</h1>
        <div v-for="entry in firstRow" :key="entry.label">
          <IconBadge
            v-if="isBadge(entry)"
            :background-color="entry.backgroundColor"
            :color="entry.color"
            :icon="toRaw(entry.icon)"
            :label="entry.label"
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
            :background-color="entry.backgroundColor"
            :color="entry.color"
            :icon="entry.icon"
            :label="entry.label"
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
    <SaveButton
      v-if="saveCallback"
      aria-label="Speichern Button"
      :service-callback="saveCallback"
    />
  </div>
</template>
