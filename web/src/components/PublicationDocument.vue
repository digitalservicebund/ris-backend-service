<script lang="ts" setup>
import { ref, onMounted } from "vue"
import TextButton from "./TextButton.vue"
import CodeSnippet from "@/components/CodeSnippet.vue"
import ErrorModal from "@/components/ErrorModal.vue"

const isFristTimePublication = ref<boolean>(false)
const hasValidationError = ref<boolean>(false)
const lastPublicationDate = ref<string>("24.07.2022 16:53 Uhr")
const receiverEmail = ref<string>("dokmbx@juris.de")
const emailSubject = ref<string>('id=OVGNW name="knorr" da=r dt=b df=r')
const xml = ref<string>("xml wird geladet ....")
const errorMessageTitle = "Leider ist ein Fehler aufgetreten."
const errorMessage =
  "Die Dokumentationseinheit kann nicht veröffentlich werden."
const showIssuesDetails = ref<boolean>(false)
const toggleShowIssuesDetails = () => {
  showIssuesDetails.value = !showIssuesDetails.value
}
onMounted(() => {
  xml.value =
    '<?xml version="1.0"?>\n<!DOCTYPE juris-r SYSTEM "juris-r.dtd">\n<juris-r>\n<metadaten>\n<gericht>\n<gertyp>Gerichtstyp</gertyp\n><gerort>Gerichtssitz</gerort>\n</gericht>\n</metadaten>\n<textdaten>\n<titelzeile>\n<body>\n<div>\n<p>Titelzeile</p>\n</div>\n</body>\n</titelzeile>\n<leitsatz>\n<body>\n<div>\n<p>Leitsatz</p>\n</div>\n</body>\n</leitsatz>\n<osatz>\n<body>\n<div>\n<p>Orientierungssatz</p>\n</div>\n</body>\n</osatz>\n<tenor>\n<body>\n<div>\n<p>Tenor</p>\n</div>\n</body>\n</tenor>\n<tatbestand>\n<body>\n<div>\n<p>Tatbestand</p>\n</div>\n</body>\n</tatbestand>\n<entscheidungsgruende>\n<body>\n<div>\n<p>Entscheidungsgründe</p>\n</div>\n</body>\n</entscheidungsgruende>\n<gruende>\n<body>\n<div>\n<p>Gründe</p>\n</div>\n</body>\n</gruende>\n</textdaten>\n</juris-r>'
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
            <p class="publication-text-body">3 Pflichtfelder nicht befüllt</p>
          </div>
          <div
            v-show="showIssuesDetails"
            class="xml-validation-error-details flex-col-container"
          >
            <p class="publication-text-body">Aktenzeichen</p>
            <p class="publication-text-body">Entscheidungsname</p>
            <p class="publication-text-body">Gericht</p>
          </div>
        </div>
        <div class="publication-button-container">
          <div v-if="!isFristTimePublication" class="text-container">
            <p class="publication-text-body">Zuletzt veröffentlicht</p>
            <p class="publication-text-subline">24.07.2022</p>
          </div>
          <div class="publication-button">
            <TextButton
              label="Dokumentationseinheit veröffenlichen"
              button-type="primary"
              icon="campaign"
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
        <CodeSnippet :xml="xml" title="xml" />
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
