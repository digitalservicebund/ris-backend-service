<script lang="ts" setup>
import { UseFetchReturn } from "@vueuse/core"
import Button from "primevue/button"
import { computed, onMounted, Ref, ref, shallowRef, watch } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputField from "@/components/input/InputField.vue"
import { ComboboxItem } from "@/components/input/types"
import CountryOfOrigin from "@/domain/countryOfOrigin"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import ComboboxItemService from "@/services/comboboxItemService"

const props = defineProps<{
  modelValue?: CountryOfOrigin
  modelValueList?: CountryOfOrigin[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: CountryOfOrigin]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [value?: boolean]
}>()

const lastSavedModelValue = ref(new CountryOfOrigin({ ...props.modelValue }))
const currentValue: Ref<CountryOfOrigin> = ref(
  new CountryOfOrigin({ ...props.modelValue }),
)

async function addNewEntry() {
  emit("update:modelValue", currentValue.value)
  emit("addEntry")
}

watch(
  () => props.modelValue,
  () => {
    currentValue.value = new CountryOfOrigin({
      ...props.modelValue,
    })
    lastSavedModelValue.value = new CountryOfOrigin({
      ...props.modelValue,
    })
  },
)

onMounted(() => {
  currentValue.value = new CountryOfOrigin({
    ...props.modelValue,
  })
})

const countryWithLabel = computed({
  get: () =>
    currentValue.value?.country
      ? {
          label: currentValue.value.country.identifier,
          value: currentValue.value.country,
        }
      : undefined,
  set: (newValue: FieldOfLaw) => {
    currentValue.value.country = newValue
  },
})

const fieldOfLawWithLabel = computed({
  get: () =>
    currentValue.value?.fieldOfLaw
      ? {
          label: currentValue.value.fieldOfLaw.identifier,
          value: currentValue.value.fieldOfLaw,
        }
      : undefined,
  set: (newValue: FieldOfLaw) => {
    currentValue.value.fieldOfLaw = newValue
  },
})

const fieldOfLawWithoutCountriesService = (
  filter: Ref<string | undefined>,
): UseFetchReturn<ComboboxItem[]> => {
  const useFetchReturn =
    ComboboxItemService.getFieldOfLawSearchByIdentifier(filter)

  const data = shallowRef<ComboboxItem[] | null>(null)

  watch(
    useFetchReturn.data,
    () => {
      data.value =
        useFetchReturn.data.value?.filter(
          (item) => !item.label.startsWith("RE-07-"),
        ) ?? null
    },
    { immediate: true },
  )

  return {
    ...useFetchReturn,
    data,
  }
}
</script>

<template>
  <div class="flex flex-col gap-24">
    <div class="flex flex-row gap-24">
      <div class="basis-1/2">
        <InputField
          id="countryOfOriginCountryInput"
          v-slot="slotProps"
          label="Landbezeichnung"
        >
          <ComboboxInput
            id="countryOfOriginCountryInputText"
            v-model="countryWithLabel"
            aria-label="Landbezeichnung"
            class="w-full"
            :invalid="slotProps.hasError"
            :item-service="
              ComboboxItemService.getCountryFieldOfLawSearchByIdentifier
            "
          ></ComboboxInput>
        </InputField>
      </div>
      <div class="basis-1/2">
        <InputField
          id="countryOfOriginFieldOfLawInput"
          v-slot="slotProps"
          label="Rechtlicher Rahmen"
        >
          <ComboboxInput
            id="countryOfOriginFieldOfLawInputText"
            v-model="fieldOfLawWithLabel"
            aria-label="Rechtlicher Rahmen"
            class="w-full"
            :invalid="slotProps.hasError"
            :item-service="fieldOfLawWithoutCountriesService"
          ></ComboboxInput>
        </InputField>
      </div>
    </div>

    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <Button
            aria-label="Herkunftsland speichern"
            :disabled="currentValue.isEmpty"
            label="Übernehmen"
            severity="secondary"
            size="small"
            @click.stop="addNewEntry"
          ></Button>
          <Button
            v-if="!lastSavedModelValue.isEmpty"
            aria-label="Abbrechen"
            label="Abbrechen"
            size="small"
            text
            @click.stop="emit('cancelEdit')"
          ></Button>
        </div>
      </div>
      <Button
        v-if="!lastSavedModelValue.isEmpty"
        aria-label="Eintrag löschen"
        label="Eintrag löschen"
        severity="danger"
        size="small"
        @click.stop="emit('removeEntry', true)"
      ></Button>
    </div>
  </div>
</template>
