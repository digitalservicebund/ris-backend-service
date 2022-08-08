<script lang="ts" setup>
import { ref, onMounted } from "vue"
import TextButton from "./TextButton.vue"
import ErrorModal from "@/components/ErrorModal.vue"

const isFristTimePublication = ref<boolean>(false)
const hasValidationError = ref<boolean>(true)
const lastPublicationDate = ref<string>("24.07.2022 16:53 Uhr")
const receiverEmail = ref<string>("dokmbx@juris.de")
const emailSubject = ref<string>('id=OVGNW name="knorr" da=r dt=b df=r')
const xml = ref<string>("xml wird geladet ....")
const errorMessageTitle = "Leider ist ein Fehler aufgetreten."
const errorMessage =
  "Die Dokumentationseinheit kann nicht veröffentlich werden."

onMounted(() => {
  xml.value =
    '<?xml version="1.0"?><!DOCTYPE juris-r SYSTEM "juris-r.dtd"><juris-r></juris-r>'
})
</script>

<template>
  <div class="flex-col-container publication-container">
    <h2>Veröffenlichen</h2>
    <div class="flex-col-container publication-check-container">
      <div
        class="publication-check-infos-container"
        :class="{
          'publication-check-infos-container__in_error': hasValidationError,
        }"
      >
        <p class="publication-text-header">Plausibilitätsprüfung</p>
        <p class="publication-text-body">0 Fehler</p>
        <div class="publication-button-container">
          <div v-if="!isFristTimePublication" class="text-container">
            <p class="publication-text-body">Zuletzt veröffentlicht</p>
            <p class="publication-text-subline">24.07.2022</p>
          </div>
          <div class="publication-button">
            <TextButton
              label="Dokumentationseinheit veröffenlichen"
              button-type="primary"
              :disabled="hasValidationError"
            />
          </div>
        </div>
      </div>
      <ErrorModal
        v-if="hasValidationError"
        :title="errorMessageTitle"
        :description="errorMessage"
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
          Letzte Veröffenlichung am {{ lastPublicationDate }}
        </p>
        <p class="publication-text-label">über</p>
        <div class="receiver-info">
          <p class="publication-text-body">
            E-Mail an: <span>{{ receiverEmail }}</span>
          </p>
          <p class="publication-text-body">
            Betreff: <span>{{ emailSubject }}</span>
          </p>
        </div>
        <p class="publication-text-label">als</p>
        <div class="xml-container">
          <p class="publication-text-body" style="font-weight: 700">XML</p>
          <code class="xml-viewer">{{ xml }}</code>
        </div>
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
  height: 100vh;
  row-gap: 32px;
  padding-top: 32px;
  padding-left: 32px;

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

  .xml-container {
    code {
      background-color: $white;
    }
  }
}
</style>
