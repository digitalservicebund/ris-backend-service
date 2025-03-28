<script setup lang="ts">
import Button from "primevue/button"
import { onBeforeMount, ref } from "vue"
import InfoModal from "@/components/InfoModal.vue"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import { getCategoryLabels } from "@/components/text-check/categoryLabels"
import router from "@/router"
import { ResponseError } from "@/services/httpClient"
import languageToolService from "@/services/textCheckService"
import { useExtraContentSidePanelStore } from "@/stores/extraContentSidePanelStore"
import IconCheck from "~icons/ic/baseline-check"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

const props = defineProps<{
  documentNumber: string
  documentId: string
}>()

const responseError = ref<ResponseError | undefined>()

const loading = ref()
const totalTextCheckErrors = ref(0)
const sidePanelStore = useExtraContentSidePanelStore()
const textCategories = ref<string[] | undefined>()

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

function resetResults() {
  totalTextCheckErrors.value = 0
  textCategories.value = undefined
}

const checkAll = async (documentUnitId: string) => {
  resetResults()
  loading.value = true
  const response = await languageToolService.checkAll(documentUnitId)

  if (response.error) {
    responseError.value = response.error
  } else if (response.data && response.data.suggestions) {
    responseError.value = undefined
    totalTextCheckErrors.value = response.data.totalTextCheckErrors
    textCategories.value = getCategoryLabels(response.data.categoryTypes)
  }
  loading.value = false
}

onBeforeMount(async () => {
  await checkAll(props.documentId)
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

    <div v-else-if="loading" class="flex flex-col">
      <LoadingSpinner size="small" />
    </div>

    <div v-else>
      <div v-if="totalTextCheckErrors > 0">
        <div class="flex flex-col gap-16">
          <div class="flex flex-row gap-8">
            <IconErrorOutline class="text-red-800" />
            <div>
              Es wurden Rechtschreibfehler identifiziert:
              <div>
                <dl class="my-16">
                  <div
                    class="grid grid-cols-[auto_1fr] gap-x-16 px-0"
                    data-testid="total-text-check-errors"
                  >
                    <dt class="ds-label-02-bold self-center">Anzahl</dt>
                    <dd class="ds-body-02-reg">
                      {{ totalTextCheckErrors }}
                    </dd>
                    <dt class="ds-label-02-bold self-center">Rubrik</dt>
                    <dd class="ds-body-02-reg">
                      {{ textCategories?.join(", ") }}
                    </dd>
                  </div>
                </dl>
              </div>
            </div>
          </div>
          <Button
            aria-label="Rechtschreibfehler prüfen"
            class="w-fit"
            label="Rechtschreibfehler prüfen"
            severity="secondary"
            size="small"
            @click="navigateToTextCheckSummaryInCategories"
          ></Button>
        </div>
      </div>
      <div v-else class="flex flex-row gap-8">
        <IconCheck class="text-green-700" />
        <p>Es wurden keine Rechtschreibfehler identifiziert.</p>
      </div>
    </div>
  </div>
</template>
