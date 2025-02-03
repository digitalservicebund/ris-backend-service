<script setup lang="ts">
import { computed } from "vue"
import TextButton from "@/components/input/TextButton.vue"
import { Match } from "@/types/languagetool"
import IconCheck from "~icons/ic/baseline-check"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

const props = defineProps<{
  documentNumber: string
  matches?: Match[]
}>()

const categoriesRoute = computed(() => ({
  name: "caselaw-documentUnit-documentNumber-categories",
  params: { documentNumber: props.documentNumber },
}))
</script>

<template>
  <div aria-label="Rechtschreibprüfung" class="flex flex-col">
    <h2 class="ds-label-01-bold mb-16">Rechtschreibprüfung</h2>
    <div v-if="matches && props.matches!.length > 0">
      <div class="flex flex-col gap-16">
        <div class="flex flex-row gap-8">
          <IconErrorOutline class="text-red-800" />
          <div>
            Es wurden Rechtschreibfehler identifiziert:
            <div>
              <dl class="my-16">
                <div class="grid grid-cols-[auto_1fr] gap-x-16 px-0">
                  <dt class="ds-label-02-bold self-center">Anzahl</dt>
                  <dd class="ds-body-02-reg">{{ matches.length }}</dd>
                  <dt class="ds-label-02-bold self-center">Rubrik</dt>
                  <dd class="ds-body-02-reg">Schlagwörter, Leitsatz, Gründe</dd>
                </div>
              </dl>
            </div>
          </div>
        </div>
        <RouterLink :to="categoriesRoute">
          <TextButton
            aria-label="Rechtschreibfehler prüfen"
            button-type="tertiary"
            class="w-fit"
            label="Rechtschreibfehler prüfen"
            size="small"
          />
        </RouterLink>
      </div>
    </div>
    <div v-else class="flex flex-row gap-8">
      <IconCheck class="text-green-700" />
      <p>Es wurden keine Rechtschreibfehler identifiziert.</p>
    </div>
  </div>
</template>
