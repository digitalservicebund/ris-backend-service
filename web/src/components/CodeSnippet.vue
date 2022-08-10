<script lang="ts" setup>
import { ref, onMounted } from "vue"

const props = defineProps<{
  title: string
  xml: string
}>()

type CodeLine = {
  codeLineText: string
  marginLeft: number
}
const codeLineMarginLeftUnitInPx = 20
const shouldUpdateMarginLeft = ref<boolean>(false)
const marginLeft = ref<number>(0)
const caculateLineMarginLeft = (line: string): number => {
  const isXMLTag = line.includes("<?xml")
  const isDocTypeTag = line.includes("<!DOCTYPE")
  const isCloseTag = line.startsWith("</")
  const isOpenTag = !isCloseTag && !line.includes("</")
  const hasBothTag = !isCloseTag && line.includes("</")
  const isBreakLine = line === "<br/>"
  let ml = 0
  if (isXMLTag || isDocTypeTag) return 0

  if (hasBothTag || isBreakLine) {
    return (marginLeft.value + 1) * codeLineMarginLeftUnitInPx
  }
  if (isOpenTag) {
    if (shouldUpdateMarginLeft.value) marginLeft.value += 1
    shouldUpdateMarginLeft.value = true
    ml = marginLeft.value * codeLineMarginLeftUnitInPx
  }
  if (isCloseTag) {
    ml = marginLeft.value * codeLineMarginLeftUnitInPx
    marginLeft.value -= 1
    shouldUpdateMarginLeft.value = true
  }
  return ml
}
const codeLines = ref<Array<CodeLine>>([])
const getCodeLines = (): Array<CodeLine> => {
  if (props.xml.includes("<?xml")) {
    return props.xml.split("\n").map((line) => {
      const ml = caculateLineMarginLeft(line)
      return { codeLineText: line, marginLeft: ml }
    })
  }
  return []
}
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
        <code class="line" :style="{ 'margin-left': `${line.marginLeft}px` }"
          ><span>{{ line.codeLineText }}</span></code
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
