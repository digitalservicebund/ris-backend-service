<script lang="ts" setup>
import { ref, onUpdated, onMounted } from "vue"

const props = defineProps<{
  title: string
  xml: string
}>()

const codeLines = ref<Array<string>>([])
const getCodeLines = (): Array<string> => {
  if (props.xml.includes("<?xml")) {
    return props.xml.split("\n")
  }
  return []
}
onUpdated(() => {
  codeLines.value = getCodeLines()
})
onMounted(() => {
  codeLines.value = getCodeLines()
})
</script>

<template>
  <div class="xml-container">
    <p class="xml-container-title">{{ props.title }}</p>
    <div class="code-lines">
      <div v-for="(line, index) in codeLines" :key="index" class="code-line">
        <code
          class="line-number"
          :style="{
            'min-width': `${codeLines.length.toString().length * 15}px`,
          }"
          ><span>{{ index + 1 }}</span></code
        >
        <code class="line"
          ><span>{{ line }}</span></code
        >
      </div>
    </div>
  </div>
</template>

<style lang="scss">
.xml-container {
  .xml-container-title {
    font-weight: 700;
    font-size: 16px;
    line-height: 26px;
    text-transform: uppercase;
    color: $black;
  }

  .code-lines {
    border: solid 1px $white;
    overflow: auto;
    white-space: nowrap;
    width: 60vw;
  }
  .code-line {
    display: flex;
    flex-direction: row;
    justify-content: flex-start;
    row-gap: 10px;
    box-sizing: border-box;
    .line-number {
      background-color: #ebecf0;
      display: flex;
      justify-content: flex-end;
      padding-right: 5px;
    }
    .line {
      padding-left: 20px;
    }
  }
}
</style>
