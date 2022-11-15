<script lang="ts" setup>
import TextButton from "./TextButton.vue"
import { useSaveToRemote } from "@/composables/useSaveToRemote"
import { ServiceResponse } from "@/services/httpClient"

const props = defineProps<{
  ariaLabel: string
  serviceCallback: () => Promise<ServiceResponse<void>>
}>()

const getCurrentTime = (dateSaved: Date) => {
  const fullHour = ("0" + dateSaved.getHours()).slice(-2)
  const fullMinute = ("0" + dateSaved.getMinutes()).slice(-2)
  return `${fullHour}:${fullMinute}`
}

const { saveIsInProgress, triggerSave, lastSaveError, lastSavedOn } =
  useSaveToRemote(props.serviceCallback, 10000)
</script>

<template>
  <div class="save-button-container">
    <TextButton :aria-label="ariaLabel" @click="triggerSave" />
    <div class="save-status">
      <div v-if="saveIsInProgress">
        <div class="icon">
          <span class="material-icons"> cloud_upload </span>
        </div>
        <p class="status-text">Daten werden gespeichert</p>
      </div>
      <div v-if="lastSaveError !== undefined && !saveIsInProgress">
        <div class="icon icon--error">
          <span class="material-icons"> error_outline </span>
        </div>
        <p class="error-text">Fehler beim Speichern</p>
      </div>
      <div v-if="lastSavedOn !== undefined && !saveIsInProgress">
        <p class="status-text">
          Zuletzt gespeichert um
          <span class="on-succeed">{{ getCurrentTime(lastSavedOn) }}</span> Uhr
        </p>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.save-button-container {
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  justify-content: flex-start;
  column-gap: 10px;

  .save-status div {
    display: flex;
    flex-direction: row;
    align-items: flex-end;
    justify-items: flex-start;

    .icon {
      display: flex;
      width: 30px;
      height: 25px;
      flex-wrap: wrap;
      align-items: center;

      &--error {
        color: red;
      }
    }

    p {
      font-weight: 400;
      letter-spacing: 0.16px;
    }

    .status-text {
      font-size: 14px;
      line-height: 18px;
    }

    .error-text {
      font-size: 16px;
      line-height: 22px;
    }

    .on-succeed {
      animation: text-faded;
      animation-delay: 1s;
      animation-duration: 2s;
      animation-fill-mode: forwards;
      animation-timing-function: ease-in;
      font-size: 16px;
    }
  }

  @keyframes text-faded {
    from {
      font-size: 16px;
    }

    to {
      font-size: 14px;
    }
  }
}
</style>
