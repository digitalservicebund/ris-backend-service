<script lang="ts" setup>
import { computed, ref, watch, onMounted } from "vue"
import { useRouter } from "vue-router"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import useQuery, { Query } from "@/composables/useQueryFromRoute"
import LegalPeriodical from "@/domain/legalPeriodical"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import ComboboxItemService from "@/services/comboboxItemService"
import LegalPeriodicalEditionService from "@/services/legalPeriodicalEditionService"

const router = useRouter()
const filter = ref<LegalPeriodical>()
const currentEditions = ref<LegalPeriodicalEdition[]>()
const { getQueryFromRoute, pushQueryToRoute, route } = useQuery<"q">()
const query = ref(getQueryFromRoute())

watch(
  filter,
  async (newFilter) => {
    if (newFilter && newFilter.uuid) {
      await updateEditions(newFilter.uuid)
      debouncedPushQueryToRoute({ q: newFilter.uuid })
    } else {
      currentEditions.value = []
    }
  },
  { deep: true },
)

/**
 * Sets a timeout before pushing the search query to the route,
 * in order to only change the url params when the user input pauses.
 */
const debouncedPushQueryToRoute = (() => {
  let timeoutId: number | null = null

  return (currentQuery: Query<string>) => {
    if (timeoutId != null) window.clearTimeout(timeoutId)

    timeoutId = window.setTimeout(() => pushQueryToRoute(currentQuery), 500)
  }
})()

/**
 * Get query from url and set local query value
 */
watch(route, () => {
  const currentQuery = getQueryFromRoute()
  if (JSON.stringify(query.value) != JSON.stringify(currentQuery))
    query.value = currentQuery
})

onMounted(() => {
  if (query.value.q) updateEditions(query.value.q)
})

/**
 * Loads all editions of a legal periodical
 */
async function updateEditions(legalPeriodicalId: string) {
  const response =
    await LegalPeriodicalEditionService.getAllByLegalPeriodicalId(
      legalPeriodicalId,
    )
  if (response.data) {
    currentEditions.value = response.data
  }
}

const legalPeriodical = computed({
  get: () =>
    filter.value
      ? {
          label: filter.value.abbreviation,
          value: filter.value,
        }
      : undefined,
  set: (newValue) => {
    const legalPeriodical = { ...newValue } as LegalPeriodical
    if (newValue) {
      filter.value = legalPeriodical
    } else {
      filter.value = undefined
    }
  },
})
</script>

<template>
  <div class="flex flex-col gap-16 p-16">
    <div class="flex justify-between">
      <h1 class="ds-heading-02-reg">Periodika</h1>
      <TextButton
        aria-label="Neue Periodikaauswertung"
        class="ds-button-02-reg"
        label="Neue Periodikaauswertung"
        @click="router.push({ name: 'caselaw-periodical-evaluation-new' })"
      ></TextButton>
    </div>
    <div class="flex h-full flex-col space-y-24 bg-gray-100 px-16 py-16">
      <div class="flex flex-row items-end gap-24">
        <div class="flex-grow" role="search">
          <InputField id="legalPeriodical" label="Periodikum suchen">
            <ComboboxInput
              id="legalPeriodical"
              v-model="legalPeriodical"
              aria-label="Periodikum"
              clear-on-choosing-item
              :has-error="false"
              :item-service="ComboboxItemService.getLegalPeriodicals"
            ></ComboboxInput>
          </InputField>
        </div>
      </div>
      <ul>
        <li
          v-for="edition in currentEditions"
          :key="edition.id"
          class="flex gap-24"
        >
          Ausgabe {{ edition.name }} ({{ edition.references?.length }}
          Fundstellen)
          <router-link
            target="_blank"
            :to="{
              name: 'caselaw-periodical-evaluation-uuid',
              params: { uuid: edition.id },
            }"
          >
            <button class="ds-link-03 border-b-1 border-blue-800 leading-24">
              bearbeiten
            </button>
          </router-link>
        </li>
      </ul>
    </div>
  </div>
</template>
