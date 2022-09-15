<script lang="ts" setup>
import { ref, computed } from "vue"
import InputField from "./InputField.vue"
import TextButton from "./TextButton.vue"
import TextInput from "./TextInput.vue"
import CodeSnippet from "@/components/CodeSnippet.vue"
import ErrorModal from "@/components/ErrorModal.vue"
import XmlMail from "@/domain/xmlMail"

const props = defineProps<{
  publishResult?: XmlMail
  lastPublishedXmlMail?: XmlMail
  errorMessage?: { title: string; description: string }
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
    <h2>Veröffentlichen</h2>
    <div class="flex-col-container publication-check-container">
      <div
        class="publication-check-infos-container"
        :class="{
          'publication-check-infos-container__in_error': hasValidationError,
        }"
      >
        <p class="publication-text-header">Plausibilitätsprüfung</p>

        <div v-if="!publishResult" class="text-icon">
          <span
            class="material-icons material-symbols-outlined"
            style="color: white; background-color: black; border-radius: 50%"
          >
            help
          </span>
          <p style="margin-left: -8px">
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
            class="xml-validation-error-details flex-col-container"
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
            label="Empfänger-E-Mail-Adresse:"
            :error-message="
              emailAddressInvalid ? 'E-Mail-Adresse ungültig' : undefined
            "
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
              label="Dokumentationseinheit veröffentlichen"
              aria-label="Dokumentationseinheit veröffentlichen"
              button-type="primary"
              icon="campaign"
              @click="publishDocumentUnit()"
            />
          </div>
        </div>
      </div>
      <ErrorModal
        v-if="!!props.errorMessage"
        :title="props.errorMessage?.title"
        :description="props.errorMessage?.description"
      >
      </ErrorModal>
    </div>
    <div class="flex-col-container publication-infos-container">
      <p class="publication-text-header">Letzte Veröffentlichungen</p>
      <p v-if="isFirstTimePublication">
        Diese Dokumentationseinheit wurde bisher nicht veröffentlicht
      </p>
      <div v-else class="flex-col-container email-infos-container">
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
          :xml="props.lastPublishedXmlMail.xml"
          title="xml"
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
  background-color: #f6f7f8;
  min-height: 100vh;
  row-gap: 32px;
  padding-top: 32px;
  padding-left: 32px;
  padding-bottom: 32px;

  .icon {
    width: 15px;
    height: 25px;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
  }

  p {
    font-style: normal;
    font-weight: 400;
    letter-spacing: 0.16px;
    color: $black;
  }

  .publication-text-header {
    font-size: 24px;
    line-height: 30px;
  }

  .publication-text-label {
    font-style: normal;
    font-weight: 700;
    font-size: 11px;
    line-height: 16px;
    text-transform: uppercase;
    letter-spacing: 1px;
    color: #4e596a;
  }

  .publication-check-container {
    width: fit-content;

    .publication-check-infos-container {
      display: flex;
      flex-direction: column;
      justify-content: flex-start;
      row-gap: 16px;
      padding: 16px 56px 16px 24px;
      background-color: $white;
      width: 100%;

      &__in_error {
        border: solid 1px #b0243f;
        max-width: 50vw;
      }

      .publication-button-container {
        display: flex;
        flex-direction: row;
        column-gap: 32px;
        justify-content: space-between;
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
          font-weight: 400;
          margin-left: 5px;
        }
      }
    }
  }

  .text-icon {
    display: flex;
    flex: row nowrap;
    justify-content: flex-start;
    align-items: center;
    column-gap: 12px;
  }

  .xml-validation-error-container {
    .xml-validation-error-details {
      padding-top: 16px;
      padding-left: 27px;
      padding-bottom: 16px;
      row-gap: 16px;
    }
  }
}
</style>
