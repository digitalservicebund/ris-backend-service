<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { ref } from "vue"
import CheckboxInput from "@/components/input/CheckboxInput.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import TitleElement from "@/components/TitleElement.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import BaselineArrowOutward from "~icons/ic/baseline-arrow-outward"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

const { documentUnit } = storeToRefs(useDocumentUnitStore())

const isIgnored = ref(false)
</script>

<template>
  <div class="w-full grow p-24">
    <div class="flex flex-col gap-24 bg-white p-24">
      <TitleElement>Verwaltungsdaten</TitleElement>
      <dl class="my-16">
        <div class="grid grid-cols-3 gap-24 px-0">
          <dt class="ds-label-02-bold">Zuständige Dokstelle:</dt>
          <dd class="ds-body-02-reg">
            {{
              documentUnit?.coreData.court?.responsibleDocOffice?.abbreviation
            }}
          </dd>
        </div>
        <div class="grid grid-cols-3 gap-24 px-0">
          <dt class="ds-label-02-bold">Dublettenverdacht:</dt>
          <dd class="ds-body-02-reg">
            <div
              v-for="duplicate in documentUnit?.managementData
                .duplicateRelations"
              :key="duplicate.documentNumber"
              class="flex flex-row gap-4"
            >
              <span>
                <IconErrorOutline class="text-red-800" />
              </span>

              <RouterLink
                v-if="duplicate.documentNumber"
                class="ds-link-01-bold whitespace-nowrap no-underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
                :data-testid="
                  'document-number-link-' + duplicate.documentNumber
                "
                tabindex="-1"
                target="_blank"
                :to="{
                  name: 'caselaw-documentUnit-documentNumber-preview',
                  params: { documentNumber: duplicate.documentNumber },
                }"
              >
                {{ duplicate.documentNumber }}
                <BaselineArrowOutward class="mb-4 inline w-24" />
              </RouterLink>
              <InputField
                id="isIgnored"
                v-slot="{ id }"
                class="whitespace-nowrap"
                label="Warnung ignorieren"
                label-class="ds-label-01-reg"
                :label-position="LabelPosition.RIGHT"
              >
                <CheckboxInput
                  :id="id"
                  v-model="isIgnored"
                  aria-label="Warnung ignorieren"
                  size="small"
                />
              </InputField>
            </div>
          </dd>
        </div>
      </dl>
    </div>
  </div>
</template>
