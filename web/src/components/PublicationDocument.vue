<script lang="ts" setup>
import { ref } from "vue"
import TextButton from "./TextButton.vue"
import CodeSnippet from "@/components/CodeSnippet.vue"
import ErrorModal from "@/components/ErrorModal.vue"

const props = defineProps<{
  xml: string
  issues: Array<string>
  receiverEmail: string
  emailSubject: string
  lastPublicationDate: string
  isFristTimePublication: boolean
  hasValidationError: boolean
}>()

defineEmits<{
  (e: "publishADocument"): void
}>()

const showIssuesDetails = ref<boolean>(false)
const toggleShowIssuesDetails = () => {
  showIssuesDetails.value = !showIssuesDetails.value
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
        <div class="text-icon">
          <div class="icon">
            <span
              v-if="hasValidationError && !showIssuesDetails"
              class="material-icons"
            >
              error
            </span>
            <span
              v-if="hasValidationError && showIssuesDetails"
              class="material-icons"
            >
              priority_high
            </span>
            <span v-if="!hasValidationError" class="material-icons">
              done_all
            </span>
          </div>
          <p class="publication-text-header">Plausibilitätsprüfung</p>
        </div>

        <div v-if="!hasValidationError" class="text-icon">
          <div class="icon">
            <span class="material-icons"> done </span>
          </div>
          <p class="publication-text-body">0 Fehler</p>
        </div>
        <div v-else class="xml-validation-error-container">
          <div class="text-icon">
            <button class="icon" @click="toggleShowIssuesDetails">
              <span v-if="showIssuesDetails" class="material-icons">
                keyboard_arrow_up
              </span>
              <span v-else class="material-icons"> keyboard_arrow_down </span>
            </button>
            <p class="publication-text-body">
              {{ props.issues.length }} Pflichtfelder nicht befüllt
            </p>
          </div>
          <div
            v-show="showIssuesDetails"
            class="xml-validation-error-details flex-col-container"
          >
            <p
              v-for="issue in issues"
              :key="issue"
              class="publication-text-body"
            >
              {{ issue }}
            </p>
          </div>
        </div>
        <div class="publication-button-container">
          <div v-if="!isFristTimePublication" class="text-container">
            <p class="publication-text-body">Zuletzt veröffentlicht</p>
            <p class="publication-text-subline">
              {{ props.lastPublicationDate }}
            </p>
          </div>
          <div class="publication-button">
            <TextButton
              label="Dokumentationseinheit veröffenlichen"
              button-type="primary"
              icon="campaign"
              @click="$emit('publishADocument')"
            />
          </div>
        </div>
      </div>
      <ErrorModal
        v-if="hasValidationError"
        title="Leider ist ein Fehler aufgetreten."
        description="Die Dokumentationseinheit kann nicht veröffentlich werden."
      >
      </ErrorModal>
    </div>
    <div class="flex-col-container publication-infos-container">
      <p class="publication-text-header">Letzte Veröffentlichungen</p>
      <p v-if="isFristTimePublication" class="publication-text-body">
        Diese Dokumentationseinheit wurde bisher nicht veröffentlicht
      </p>
      <div v-else class="flex-col-container email-infos-container">
        <p class="publication-text-body">
          Letzte Veröffenlichung am {{ props.lastPublicationDate }}
        </p>
        <p class="publication-text-label">über</p>
        <div class="receiver-info">
          <p class="publication-text-body">
            E-Mail an: <span>{{ props.receiverEmail }}</span>
          </p>
          <p class="publication-text-body">
            Betreff: <span>{{ props.emailSubject }}</span>
          </p>
        </div>
        <p class="publication-text-label">als</p>
        <CodeSnippet :xml="props.xml" title="xml" />
      </div>
    </div>
  </div>
</template>

<style lang="scss">
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
  .publication-text-body  {
    font-size: 16px;
    line-height: 26px;
  }
  .publication-text-subline {
    font-size: 11px;
    line-height: 16px;
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
      }
      .text-container  {
        display: flex;
        flex-direction: column;
        justify-content: space-between;
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
    flex-direction: row;
    flex-wrap: nowrap;
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
