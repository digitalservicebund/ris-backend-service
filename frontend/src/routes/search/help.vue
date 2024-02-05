<script lang="ts" setup>
import DropdownInput from "@/shared/components/input/DropdownInput.vue"
import type { DropdownItem } from "@/shared/components/input/types"
import { computed, ref } from "vue"
import { availableFields, availableFeatures, type FieldType } from "./data"

const filter = ref<FieldType>("all")
const filters: DropdownItem[] = [
  { label: "Alle", value: "all" },
  { label: "Rechtsprechung", value: "case_law" },
  { label: "Rechtssetzung", value: "norms" },
  { label: "Verwaltungsvorschriften", value: "administrative_regulations" },
  { label: "Literaturnachweise", value: "literature" },
]

const fields = computed(() => {
  return filter.value === "all"
    ? availableFields
    : availableFields.filter((f) => f.types.includes(filter.value))
})
</script>

<template>
  <div class="container mx-auto py-16">
    <div class="flex flex-col gap-48">
      <div>
        <h1 class="ds-heading-02-reg mb-8 mt-24">Hilfe zur Suche</h1>
        <p>
          Hier finden Sie eine Übersicht über die aktuell verfügbaren Funktionen
          und Rubriken der Suche. Wir arbeiten daran weitere Daten hinzuzufügen
          und die Funktionalität nutzerfreundlicher zu machen.
        </p>
      </div>

      <div>
        <h2 class="ds-heading-03-reg mb-20">Verfügbare Funktionen</h2>
        <dl class="flex flex-col gap-16 divide-y divide-gray-200">
          <div
            v-for="feature in availableFeatures"
            :key="feature.label"
            :id="feature.id"
            class="gap-8 md:flex md:flex-row"
          >
            <dt class="w-2/6">
              <h3 class="ds-subhead">
                {{ feature.label }}
              </h3>
            </dt>
            <dd class="w-4/6">
              <p class="" v-html="feature.description" />
              <div
                class="ds-body-02-reg mt-4"
                v-if="feature.examples.length > 0"
              >
                <template v-for="example in feature.examples">
                  <pre
                    class="inline-block w-auto border border-gray-200 px-4"
                    >{{ example }}</pre
                  >
                </template>
              </div>
            </dd>
          </div>
        </dl>
      </div>

      <div>
        <div class="flex flex-row justify-between">
          <h2 class="ds-heading-03-reg mb-20">Verfügbare Rubriken</h2>
          <DropdownInput
            :items="filters"
            aria-label="dropdown input"
            v-model="filter"
            placeholder="Bitte auswählen"
            class="ds-select-small w-auto"
          />
        </div>
        <table class="w-full">
          <thead class="sticky top-0">
            <tr>
              <th
                class="border-b-2 border-blue-300 bg-white px-16 py-12 text-left"
              >
                Rubrik
              </th>
              <th
                class="border-b-2 border-blue-300 bg-white px-16 py-12 text-left"
              >
                Kennung
              </th>
              <th
                class="border-b-2 border-blue-300 bg-white px-16 py-12 text-left"
              >
                Beispiele
              </th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="field in fields"
              :key="field.label"
              :id="field.label.toLocaleLowerCase()"
              class="hover:bg-gray-100"
            >
              <td class="border-b-1 border-blue-300 px-16 py-12 align-middle">
                {{ field.label }}
              </td>
              <td
                class="ds-body-02-reg border-b-1 border-blue-300 px-16 py-12 align-middle"
              >
                <template v-for="alias in field.aliases">
                  <code>{{ alias }}</code>
                </template>
              </td>
              <td
                class="ds-body-02-reg border-b-1 border-blue-300 px-16 py-12 align-middle"
              >
                <template v-for="example in field.examples">
                  <pre
                    class="inline-block w-auto border border-gray-200 px-4"
                    >{{ example }}</pre
                  >
                </template>
              </td>
            </tr>
            <tr v-if="fields.length === 0">
              <td colspan="3" class="p-12">
                Für diese Dokumentart sind aktuell keine Rubriken verfügbar.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
