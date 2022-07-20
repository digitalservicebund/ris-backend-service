<script lang="ts" setup>
import TextButton from "./TextButton.vue"

const props = defineProps<{
  contentText: string
  confirmText: string
}>()
defineEmits<{
  (e: "closeModal"): void
  (e: "confirmAction"): void
}>()
</script>

<template>
  <div
    class="popup-modal-wrapper"
    @click.self="$emit('closeModal')"
    @keydown.esc="$emit('closeModal')"
  >
    <div class="modal-container">
      <div class="modal-text-content">
        {{ props.contentText }}
      </div>
      <div class="modal-buttons-container">
        <TextButton label="Abbrechen" @click="$emit('closeModal')" />
        <TextButton
          :label="props.confirmText"
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
  background-color: rgba(175, 175, 175, 0.8);
  z-index: 999;
  display: flex;
  justify-content: center;
  align-items: center;

  .modal-container {
    background: #fff;
    min-height: 40vh;
    max-width: 40vw;
    border-radius: 10px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    align-items: center;
    font-size: 1.25em;
    padding: 20px;

    .modal-text-content {
      text-align: left;
      display: inline-block;
      padding-top: 30px;
      -webkit-touch-callout: none; /* iOS Safari */
      -webkit-user-select: none; /* Safari */
      -khtml-user-select: none; /* Konqueror HTML */
      -moz-user-select: none; /* Old versions of Firefox */
      -ms-user-select: none; /* Internet Explorer/Edge */
      user-select: none; /* Non-prefixed version, currently
                                  supported by Chrome, Edge, Opera and Firefox */
    }

    .modal-buttons-container {
      display: flex;
      width: 100%;
      flex-direction: row;
      justify-content: end;
      column-gap: 20px;

      button {
        min-width: 150px;
      }
    }
  }
}
</style>
