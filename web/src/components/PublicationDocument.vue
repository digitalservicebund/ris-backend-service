<script lang="ts" setup>
import { ref, computed } from "vue"
import InputField from "./InputField.vue"
import TextButton from "./TextButton.vue"
import TextInput from "./TextInput.vue"
import CodeSnippet from "@/components/CodeSnippet.vue"
import InfoModal from "@/components/InfoModal.vue"
import XmlMail from "@/domain/xmlMail"
import { InfoStatus } from "@/enum/enumInfoStatus"
import { ResponseError } from "@/services/httpClient"

const props = defineProps<{
  publishResult?: XmlMail
  lastPublishedXmlMail?: XmlMail
  errorMessage?: ResponseError
  succeedMessage?: { title: string; description: string }
}>()

const emits = defineEmits<{
  (e: "publishADocument", newValue: string): void
}>()

const showIssuesDetails = ref(false)
const toggleShowIssuesDetails = () => {
  showIssuesDetails.value = !showIssuesDetails.value
}
const receiverAddress = ref("")
const emailAddressInvalid = ref(false)
const isFirstTimePublication = computed(() => {
  return !props.lastPublishedXmlMail
})
const hasValidationError = computed(() => {
  return props.publishResult?.statusCode === "400"
})

function publishDocumentUnit() {
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
</script>

<template>
  <div class="flex-col-container publication-container">
    <h1 class="heading-02-regular">Veröffentlichen</h1>
    <div class="flex-col-container publication-check-container">
      <div
        class="bg-white publication-check-infos-container"
        :class="{
          'publication-check-infos-container__in-error': hasValidationError,
        }"
      >
        <p class="publication-text-header">Plausibilitätsprüfung</p>

        <div v-if="!publishResult" class="text-icon">
          <span
            class="material-icons material-symbols-outlined text-icon--outlined"
          >
            help
          </span>
          <p class="help-text">
            Durch Klick auf <em>Veröffentlichen</em> wird die
            Plausibilitätsprüfung ausgelöst.
          </p>
        </div>
        <div v-if="!!publishResult && !hasValidationError" class="text-icon">
          <div class="icon">
            <span class="material-icons"> done </span>
          </div>
          <p>0 Fehler</p>
        </div>
        <div v-if="hasValidationError" class="xml-validation-error-container">
          <div class="text-icon">
            <button class="icon" @click="toggleShowIssuesDetails">
              <span v-if="showIssuesDetails" class="material-icons">
                keyboard_arrow_up
              </span>
              <span v-else class="material-icons"> keyboard_arrow_down </span>
            </button>
            <p>
              {{ props.publishResult?.statusMessages?.length }} Pflichtfelder
              nicht befüllt
            </p>
          </div>
          <div
            v-show="showIssuesDetails"
            class="flex-col-container xml-validation-error-details"
          >
            <p
              v-for="issue in props.publishResult?.statusMessages"
              :key="issue"
            >
              {{ issue }}
            </p>
          </div>
        </div>
        <div>
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
              :has-error="emailAddressInvalid"
            />
          </InputField>
        </div>
        <div class="publication-button-container">
          <div class="publication-button">
            <TextButton
              aria-label="Dokumentationseinheit veröffentlichen"
              button-type="primary"
              icon="campaign"
              label="Dokumentationseinheit veröffentlichen"
              @click="publishDocumentUnit"
            />
          </div>
        </div>
      </div>

      <InfoModal v-if="errorMessage" class="mt-8" v-bind="errorMessage" />
      <InfoModal
        v-if="succeedMessage"
        class="mt-8"
        v-bind="succeedMessage"
        :status="InfoStatus.SUCCEED"
      />
    </div>
    <div class="flex-col-container publication-infos-container">
      <p class="publication-text-header">Letzte Veröffentlichungen</p>
      <p v-if="isFirstTimePublication">
        Diese Dokumentationseinheit wurde bisher nicht veröffentlicht
      </p>
      <div v-else class="email-infos-container flex-col-container">
        <p>
          Letzte Veröffentlichung am
          {{ props.lastPublishedXmlMail?.publishDate }}
        </p>
        <p class="publication-text-label">über</p>
        <div class="receiver-info">
          <p>
            E-Mail an:
            <span>{{ props.lastPublishedXmlMail?.receiverAddress }}</span>
          </p>
          <p>
            Betreff: <span>{{ props.lastPublishedXmlMail?.mailSubject }}</span>
          </p>
        </div>
        <p class="publication-text-label">als</p>
        <CodeSnippet
          v-if="!!props.lastPublishedXmlMail?.xml"
          title="xml"
          :xml="props.lastPublishedXmlMail.xml"
        />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.flex-col-container {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}

.publication-container {
  min-height: 100vh;
  background-color: #f6f7f8;
  row-gap: 32px;

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
        border: solid 1px #b0243f;
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

  .text-icon {
    display: flex;
    flex: row nowrap;
    align-items: center;
    justify-content: flex-start;
    column-gap: 12px;

    &--outlined {
      border-radius: 50%;
      background-color: black;
      color: white;
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
