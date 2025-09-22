<script setup lang="ts">
import Button from "primevue/button"
import { onBeforeMount, ref } from "vue"
import { getCategoryLabel } from "./categoryLabels"
import InfoModal from "@/components/InfoModal.vue"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import router from "@/router"
import { ResponseError } from "@/services/httpClient"
import languageToolService from "@/services/textCheckService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import IconCheck from "~icons/ic/baseline-check"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

const props = defineProps<{
  documentNumber: string
  documentId: string
}>()

const responseError = ref<ResponseError | undefined>()

const loading = ref()
const totalTextCheckErrors = ref(0)
const store = useDocumentUnitStore()

const textCategories = ref<string[] | undefined>()

async function navigateToTextCheckSummaryInCategories() {
  await router.push({
    name: "caselaw-documentUnit-documentNumber-categories",
    params: {
      documentNumber: props.documentNumber,
    },
  })
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
    textCategories.value = response.data.categoryTypes
  }
  loading.value = false
}

const textCategoriesRouter = (category: string) => ({
  name: "caselaw-documentUnit-documentNumber-categories",
  hash: `#${category}`,
  params: {
    documentNumber: props.documentNumber,
  },
})

onBeforeMount(async () => {
  await store.updateDocumentUnit()
  await checkAll(props.documentId)
})
</script>

<template>
  <div aria-label="Rechtschreibprüfung" class="flex flex-col">
    <h2 class="ris-label1-bold mb-16">Rechtschreibprüfung</h2>

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
                    data-testid="total-text-check-errors-container"
                  >
                    <dt class="ris-label2-bold self-center">Anzahl</dt>
                    <dd
                      class="ris-body2-regular"
                      data-testid="total-text-check-errors"
                    >
                      {{ totalTextCheckErrors }}
                    </dd>
                    <dt class="ris-label2-bold self-center">Rubrik</dt>
                    <dd class="ris-body2-regular">
                      <div class="flex flex-row gap-8">
                        <RouterLink
                          v-for="category in textCategories"
                          :key="category"
                          class="ris-link2-regular"
                          :data-testid="`text-check-handover-link-${category}`"
                          :to="textCategoriesRouter(category)"
                        >
                          {{ getCategoryLabel(category) }}
                        </RouterLink>
                      </div>
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
