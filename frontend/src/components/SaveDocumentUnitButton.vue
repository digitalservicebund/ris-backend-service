<script lang="ts" setup>
import { onMounted, watch, ref } from "vue"
import TextButton from "./TextButton.vue"
import { UpdateStatus } from "@/enum/enumUpdateStatus"

const props = defineProps<{
  ariaLabel: string
  updateStatus: number
}>()
const emit = defineEmits<{
  (e: "updateDocumentUnit"): void
}>()
const isFristTimeLoad = ref(false)
const onUpload = ref(false)
const updateSucceed = ref(true)
const hasUpdateError = ref(false)
const lastUpdatedTime = ref("")
const getCurrentTime = () => {
  const uploadTime = new Date()
  const fullHour = ("0" + uploadTime.getHours()).slice(-2)
  const fullMinute = ("0" + uploadTime.getMinutes()).slice(-2)
  return `${fullHour}:${fullMinute}`
}

const setDefaultStatus = () => {
  isFristTimeLoad.value = false
  onUpload.value = false
  updateSucceed.value = false
  hasUpdateError.value = false
}
const setStatus = () => {
  setDefaultStatus()
  switch (props.updateStatus) {
    case UpdateStatus.BEFORE_UPDATE: {
      isFristTimeLoad.value = true
      return
    }
    case UpdateStatus.ON_UPDATE: {
      isFristTimeLoad.value = false
      onUpload.value = true
      return
    }
    case UpdateStatus.SUCCEED: {
      isFristTimeLoad.value = false
      updateSucceed.value = true
      lastUpdatedTime.value = getCurrentTime()
      return
    }
    default:
      isFristTimeLoad.value = false
      hasUpdateError.value = true
      return
  }
}

const handleUpdateDocumentUnit = () => {
  emit("updateDocumentUnit")
}
watch(
  () => props.updateStatus,
  () => {
    setStatus()
  }
)
onMounted(() => {
  setStatus()
})
</script>

<template>
  <div class="save-button-container">
    <TextButton :aria-label="ariaLabel" @click="handleUpdateDocumentUnit" />
    <div v-if="!isFristTimeLoad" class="save-status">
      <div v-if="onUpload">
        <div class="icon">
          <span class="material-icons"> cloud_upload </span>
        </div>
        <p class="status-text">Daten werden gespeichert</p>
      </div>
      <div v-if="hasUpdateError">
        <div class="icon icon--error">
          <span class="material-icons"> error_outline </span>
        </div>
        <p class="error-text">Fehler beim Speichern</p>
      </div>
      <div v-if="updateSucceed">
        <p class="status-text">
          Zuletzt gespeichert um
          <span class="on-succeed">{{ lastUpdatedTime }}</span> Uhr
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
