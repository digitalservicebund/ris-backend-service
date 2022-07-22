<script lang="ts" setup>
import { onMounted } from "vue"
import TextButton from "./TextButton.vue"

const props = defineProps<{
  headerText?: string
  contentText: string
  confirmText: string
  cancelButtonType?: string
  confirmButtonType?: string
}>()
defineEmits<{
  (e: "closeModal"): void
  (e: "confirmAction"): void
}>()
onMounted(() => {
  ;(
    document.getElementsByClassName("popup-modal-wrapper")[0] as HTMLElement
  ).focus()
})
</script>

<template>
  <div
    class="popup-modal-wrapper"
    tabindex="0"
    @click.self="$emit('closeModal')"
    @keydown.esc="$emit('closeModal')"
  >
    <div class="modal-container">
      <div class="modal-text-title">
        {{ props.headerText }}
      </div>
      <div class="modal-text-content">{{ props.contentText }}</div>
      <div class="modal-buttons-container">
        <TextButton
          label="Abbrechen"
          :button-type="props.cancelButtonType"
          @click="$emit('closeModal')"
        />
        <TextButton
          :label="props.confirmText"
          :button-type="props.confirmButtonType"
          @click="$emit('confirmAction')"
        />
      </div>
    </div>
  </div>
</template>

<style lang="scss">
.popup-modal-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100%;
  background-color: rgba(217, 217, 217, 0.5);
  z-index: 999;
  display: flex;
  justify-content: center;
  align-items: center;

  .modal-container {
    background: $white;
    min-height: 222px;
    max-width: 442px;
    box-sizing: border-box;
    border: 2px solid $blue800;
    padding: 40px 56px;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;

    .modal-text-title {
      font-weight: 700;
      font-size: 18px;
      line-height: 26px;
      letter-spacing: 0.16px;
      color: $black;
    }

    .modal-text-content {
      font-style: normal;
      font-weight: 400;
      font-size: 14px;
      line-height: 18px;
      letter-spacing: 0.16px;
      color: $black;
      text-align: left;
    }

    .modal-buttons-container {
      display: flex;
      min-width: 70%;
      flex-direction: row;
      justify-content: space-between;
      column-gap: 16px;
    }
  }
}
</style>
