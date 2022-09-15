<script lang="ts" setup>
import { onMounted, watch, ref } from "vue"
import TextButton from "./TextButton.vue"
import { UpdateStatus } from "@/enum/enumUpdateStatus"

const props = defineProps<{
  ariaLabel: string
  updateStatus: number
}>()
const emit = defineEmits<{
  (e: "updateDocUnit"): void
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

const handleUpdateDocUnit = () => {
  emit("updateDocUnit")
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
    <TextButton :aria-label="ariaLabel" @click="handleUpdateDocUnit" />
    <div v-if="!isFristTimeLoad" class="save-status">
      <div v-if="onUpload">
        <div class="icon">
          <span class="material-icons"> cloud_upload </span>
        </div>
        <p class="status-text">Daten werden gespeichert</p>
      </div>
      <div v-if="hasUpdateError">
        <div class="icon">
          <span class="material-icons" style="color: red"> error_outline </span>
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
  justify-content: flex-start;
  align-items: flex-end;
  column-gap: 10px;

  .save-status div {
    display: flex;
    flex-direction: row;
    justify-items: flex-start;
    align-items: flex-end;

    .icon {
      width: 30px;
      height: 25px;
      display: flex;
      align-items: center;
      flex-wrap: wrap;
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
      font-size: 16px;
      animation: text-faded;
      animation-delay: 1s;
      animation-duration: 2s;
      animation-timing-function: ease-in;
      -webkit-animation-fill-mode: forwards;
      animation-fill-mode: forwards;
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
