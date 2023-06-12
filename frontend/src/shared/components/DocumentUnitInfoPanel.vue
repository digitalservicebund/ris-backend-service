<script lang="ts" setup>
import SaveButton from "@/components/SaveDocumentUnitButton.vue"
import DocumentUnit from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"
import { ServiceResponse } from "@/services/httpClient"
import IconBadge, { IconBadgeProps } from "@/shared/components/IconBadge.vue"
import PropertyInfo from "@/shared/components/PropertyInfo.vue"

interface PropertyInfo {
  label: string
  value?: string
}

interface Props {
  documentUnit?: DocumentUnit
  heading?: string
  firstRow?: (PropertyInfo | IconBadgeProps)[]
  secondRow?: (PropertyInfo | IconBadgeProps)[]
}

const props = withDefaults(defineProps<Props>(), {
  documentUnit: undefined,
  heading: "",
  firstRow: () => [],
  secondRow: () => [],
})

function isBadge(
  entry: PropertyInfo | IconBadgeProps
): entry is IconBadgeProps {
  return "icon" in entry
}

async function handleUpdateDocumentUnit(): Promise<ServiceResponse<void>> {
  let response: ServiceResponse<void> = {
    status: 200,
    data: undefined,
    error: undefined,
  }
  if (props.documentUnit) {
    response = (await documentUnitService.update(
      props.documentUnit
    )) as ServiceResponse<void>
    return {
      status: response.status,
      data: response.data,
      error: response.error,
    } as ServiceResponse<void>
  }
  return response
}
</script>

<template>
  <div
    class="bg-blue-200 border-b border-gray-400 border-solid flex flex-row items-center justify-between px-[2rem] sticky top-0 z-10"
    :class="{ 'h-[8rem]': secondRow.length }"
  >
    <div class="flex flex-col h-80 justify-between">
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
    <div>
      <SaveButton
        aria-label="Speichern Button"
        :service-callback="handleUpdateDocumentUnit"
      />
    </div>
  </div>
</template>
