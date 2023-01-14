<script lang="ts" setup>
import { ref, computed } from "vue"
import { RouterLink } from "vue-router"
import InputField from "./InputField.vue"
import TextButton from "./TextButton.vue"
import TextInput from "./TextInput.vue"
import CodeSnippet from "@/components/CodeSnippet.vue"
import InfoModal from "@/components/InfoModal.vue"
import { fieldLabels } from "@/domain"
import DocumentUnit from "@/domain/documentUnit"
import XmlMail from "@/domain/xmlMail"
import { InfoStatus } from "@/enum/enumInfoStatus"
import { ResponseError } from "@/services/httpClient"

const props = defineProps<{
  documentUnit: DocumentUnit
  publishResult?: XmlMail
  lastPublishedXmlMail?: XmlMail
  errorMessage?: ResponseError
  succeedMessage?: { title: string; description: string }
}>()

const emits = defineEmits<{
  (e: "publishADocument", newValue: string): void
}>()

const categoriesRoute = computed(() => ({
  name: "caselaw-documentUnit-:documentNumber-categories",
  params: { documentNumber: props.documentUnit.documentNumber },
}))
const receiverAddress = ref("dokmbx@juris.de")
const emailAddressInvalid = ref(false)
const isFirstTimePublication = computed(() => {
  return !props.lastPublishedXmlMail
})

const errorMessage = ref(props.errorMessage)

function publishDocumentUnit() {
  if (fieldsMissing.value) {
    errorMessage.value = {
      title: "Es sind noch nicht alle Pflichtfelder befüllt.",
      description:
        "Die Dokumentationseinheit kann nicht veröffentlicht werden.",
    }
  }
  if (validateEmailAddress()) {
    emailAddressInvalid.value = false
    emits("publishADocument", receiverAddress.value)
  } else {
    emailAddressInvalid.value = true
  }
}

function validateEmailAddress(): boolean {
  // deactivate sonar for this line because it's the best regex for checking of email addresses
  const EMAIL_REGEX =
    /(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])/ // NOSONAR

  return EMAIL_REGEX.test(receiverAddress.value)
}

function selectAll(event: Event) {
  ;(event.target as HTMLInputElement).select()
}

const missingFields = ref(
  props.documentUnit.missingRequiredFields.map((field) => fieldLabels[field])
)
const fieldsMissing = computed(() =>
  missingFields.value.length ? true : false
)
</script>

<template>
  <div class="flex flex-col flex-start gap-40 justify-start max-w-[42rem]">
    <h1 class="heading-02-regular">Veröffentlichen</h1>
    <div aria-label="Plausibilitätsprüfung" class="flex flex-row gap-16">
      <div class="w-[15.625rem]">
        <p class="subheading">1. Plausibilitätsprüfung</p>
      </div>
      <div v-if="fieldsMissing" class="flex flex-row gap-8">
        <div>
          <span class="bg-red-800 material-icons rounded-full text-white">
            error
          </span>
        </div>
        <div class="flex flex-col gap-32">
          <div>
            <p class="body-01-reg">
              Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:
            </p>
            <ul class="list-disc">
              <li
                v-for="field in missingFields"
                :key="field"
                class="body-01-reg list-item ml-[1rem]"
              >
                {{ field }}
              </li>
            </ul>
          </div>
          <RouterLink :to="categoriesRoute"
            ><TextButton
              aria-label="Rubriken bearbeiten"
              button-type="tertiary"
              class="w-fit"
              label="Rubriken bearbeiten"
          /></RouterLink>
        </div>
      </div>
      <div v-else class="flex flex-row gap-8">
        <span class="material-icons text-green-700"> check </span>
        <p class="body-01-reg">Alle Pflichtfelder sind korrekt ausgefüllt</p>
      </div>
    </div>
    <div class="border-b-1 border-b-gray-400"></div>
    <div class="flex flex-row gap-16">
      <div class="w-[15.625rem]">
        <p class="subheading">2. Empfänger der Export-Email</p>
      </div>
      <div class="grow">
        <InputField
          id="receiverAddress"
          key="receiverAddress"
          :error-message="
            emailAddressInvalid ? 'E-Mail-Adresse ungültig' : undefined
          "
          label="Empfänger-E-Mail-Adresse:"
        >
          <TextInput
            id="receiverAddress"
            v-model="receiverAddress"
            aria-label="Empfängeradresse E-Mail"
            @focus="selectAll($event)"
          />
        </InputField>
      </div>
    </div>
    <div class="border-b-1 border-b-gray-400"></div>
    <InfoModal
      v-if="errorMessage"
      aria-label="Fehler bei Veröffentlichung"
      class="mt-8"
      v-bind="errorMessage"
    />
    <InfoModal
      v-if="succeedMessage"
      aria-label="Erfolg der Veröffentlichung"
      class="mt-8"
      v-bind="succeedMessage"
      :status="InfoStatus.SUCCEED"
    />
    <TextButton
      aria-label="Dokumentationseinheit veröffentlichen"
      button-type="secondary"
      class="w-fit"
      icon="campaign"
      label="Dokumentationseinheit veröffentlichen"
      @click="publishDocumentUnit"
    />
    <div aria-label="Letzte Veröffentlichungen" class="flex flex-col gap-24">
      <h3 class="heading-03-regular">Letzte Veröffentlichungen</h3>
      <p v-if="isFirstTimePublication">
        Diese Dokumentationseinheit wurde bisher nicht veröffentlicht
      </p>
      <div v-else class="flex flex-col gap-24">
        <div class="label-02-regular">
          Letzte Veröffentlichung am
          {{ props.lastPublishedXmlMail?.publishDate }}
          ({{ props.lastPublishedXmlMail?.publishState }})
        </div>
        <div class="label-section text-gray-900">ÜBER</div>
        <div class="label-02-regular">
          <div>
            <span class="label-02-bold">E-Mail an:</span>
            {{ props.lastPublishedXmlMail?.receiverAddress }}
          </div>
          <div>
            <span class="label-02-bold"> Betreff: </span>
            {{ props.lastPublishedXmlMail?.mailSubject }}
          </div>
        </div>
        <div class="label-section text-gray-900">ALS</div>
        <CodeSnippet
          v-if="!!props.lastPublishedXmlMail?.xml"
          title="XML"
          :xml="props.lastPublishedXmlMail.xml"
        />
      </div>
    </div>
  </div>
</template>
