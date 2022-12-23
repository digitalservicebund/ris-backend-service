<script lang="ts" setup>
import { ref, computed, onMounted } from "vue"
import InputField from "./InputField.vue"
import TextButton from "./TextButton.vue"
import TextInput from "./TextInput.vue"
import CodeSnippet from "@/components/CodeSnippet.vue"
import InfoModal from "@/components/InfoModal.vue"
import DocumentUnit, { CoreData, Court } from "@/domain/documentUnit"
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
const receiverAddress = ref("")
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
    // console.log("address: " + receiverAddress.value)
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
//TODO: import coreDatefields, filter for required fields
const requiredFields = [
  { name: "fileNumbers", displayName: "Aktenzeichen" },
  { name: "court", displayName: "Gericht" },
  { name: "decisionDate", displayName: "Entscheidungsdatum" },
  { name: "legalEffect", displayName: "Rechtskraft" },
  { name: "category", displayName: "Dokumenttyp" },
]

const missingFields = ref<string[]>([])
const fieldsMissing = computed(() =>
  missingFields.value.length ? true : false
)

function checkMissingValues(value: string | unknown[] | Court | undefined) {
  //TODO: check for any invalid/ missing values
  if (value instanceof Array && value.length === 0) return true
  else return false
}

onMounted(() => {
  const coreData = { ...props.documentUnit.coreData }

  //TODO: do this check in documentUnit
  requiredFields.forEach((field) => {
    if (
      !Object.keys(coreData).includes(field.name) ||
      checkMissingValues(coreData[field.name as keyof CoreData])
    ) {
      missingFields.value.push(field.displayName)
    }
  })
})
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
          <router-link :to="categoriesRoute"
            ><TextButton
              aria-label="Rubriken bearbeiten"
              button-type="tertiary"
              class="w-fit"
              label="Rubriken bearbeiten"
          /></router-link>
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

<style lang="scss" scoped>
.publication-container {
  .icon {
    display: flex;
    width: 15px;
    height: 25px;
    flex-wrap: wrap;
    align-items: center;
  }

  p {
    font-style: normal;
    font-weight: 400;
    letter-spacing: 0.16px;
  }

  .publication-text-header {
    font-size: 24px;
    line-height: 30px;
  }

  .publication-text-label {
    color: #4e596a;
    font-size: 11px;
    font-style: normal;
    font-weight: 700;
    letter-spacing: 1px;
    line-height: 16px;
    text-transform: uppercase;
  }

  .publication-check-container {
    width: fit-content;

    .publication-check-infos-container {
      display: flex;
      width: 100%;
      flex-direction: column;
      justify-content: flex-start;
      padding: 16px 56px 16px 24px;
      row-gap: 16px;

      &__in-error {
        max-width: 50vw;
        border: solid 2px #b0243f;
      }

      .publication-button-container {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        column-gap: 32px;
      }
    }
  }

  .publication-infos-container {
    width: fit-content;
    row-gap: 16px;

    .email-infos-container {
      row-gap: 24px;

      .receiver-info {
        p {
          font-weight: 700;
        }

        span {
          margin-left: 5px;
          font-weight: 400;
        }
      }
    }
  }

  .help-text {
    margin-left: -8px;
  }

  .xml-validation-error-container {
    .xml-validation-error-details {
      padding-top: 16px;
      padding-bottom: 16px;
      padding-left: 27px;
      row-gap: 16px;
    }
  }
}
</style>
