<script setup lang="ts">
import { onMounted, ref } from "vue"
import InfoModal from "@/components/InfoModal.vue"
import TextButton from "@/components/input/TextButton.vue"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import router from "@/router"
import { ResponseError } from "@/services/httpClient"
import languageToolService from "@/services/languageToolService"
import { useExtraContentSidePanelStore } from "@/stores/extraContentSidePanelStore"
import IconCheck from "~icons/ic/baseline-check"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

const props = defineProps<{
  documentNumber: string
  documentId: string
}>()

const responseError = ref<ResponseError | undefined>()

const loading = ref(true)
const errorCount = ref(0)
const sidePanelStore = useExtraContentSidePanelStore()

useExtraContentSidePanelStore().setSidePanelMode("text-check")

async function navigateToTextCheckSummaryInCategories() {
  await router.push({
    name: "caselaw-documentUnit-documentNumber-categories",
    params: {
      documentNumber: props.documentNumber,
    },
  })
  sidePanelStore.setSidePanelMode("text-check")
  sidePanelStore.togglePanel(true)
}

const checkAll = async (documentUnitId: string) => {
  const response = await languageToolService.checkAll(documentUnitId)

  if (response.error) {
    responseError.value = response.error
  }
  if (response.data && response.data.suggestions) {
    let counter = 0
    response.data.suggestions.forEach(
      (suggestion) => (counter += suggestion.matches.length),
    )
    errorCount.value = counter

    responseError.value = undefined
  }
}

onMounted(async () => {
  await checkAll(props.documentId)
  loading.value = false
})
</script>

<template>
  <div aria-label="Rechtschreibprüfung" class="flex flex-col">
    <h2 class="ds-label-01-bold mb-16">Rechtschreibprüfung</h2>

    <div v-if="responseError">
      <InfoModal
        :description="responseError.description"
        :title="responseError.title"
      />
    </div>

    <div v-if="loading" class="flex flex-col">
      <LoadingSpinner size="small" />
    </div>

    <div v-else>
      <div v-if="errorCount > 0">
        <div class="flex flex-col gap-16">
          <div class="flex flex-row gap-8">
            <IconErrorOutline class="text-red-800" />
            <div>
              Es wurden Rechtschreibfehler identifiziert:
              <div>
                <dl class="my-16">
                  <div class="grid grid-cols-[auto_1fr] gap-x-16 px-0">
                    <dt class="ds-label-02-bold self-center">Anzahl</dt>
                    <dd class="ds-body-02-reg">{{ errorCount }}</dd>
                    <dt class="ds-label-02-bold self-center">Rubrik</dt>
                    <dd class="ds-body-02-reg">
                      Schlagwörter, Leitsatz, Gründe
                    </dd>
                  </div>
                </dl>
              </div>
            </div>
          </div>
          <TextButton
            aria-label="Rechtschreibfehler prüfen"
            button-type="tertiary"
            class="w-fit"
            label="Rechtschreibfehler prüfen"
            size="small"
            @click="navigateToTextCheckSummaryInCategories"
          />
        </div>
      </div>
      <div v-else class="flex flex-row gap-8">
        <IconCheck class="text-green-700" />
        <p>Es wurden keine Rechtschreibfehler identifiziert.</p>
      </div>
    </div>
  </div>
</template>
