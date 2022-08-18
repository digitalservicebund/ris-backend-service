<script lang="ts" setup>
import { watch, ref } from "vue"

const props = defineProps<{
  title: string
  xml: string
}>()

type CodeLine = {
  text: string
  marginLeft: number
}
const codeLineMarginLeftUnitInPx = 20
const marginLeft = ref(0)
const caculateLineMarginLeft = (line: string): number => {
  const isXMLTag = line.includes("<?xml")
  const isDocTypeTag = line.includes("<!DOCTYPE")
  const isCloseTag = line.startsWith("</")
  const isOpenTag = !isCloseTag && !line.includes("</")
  const hasBothTags =
    (!isCloseTag && line.includes("</")) ||
    (line.startsWith("<") && line.endsWith("/>"))
  let ml = 0
  if (isXMLTag || isDocTypeTag) {
    marginLeft.value = -1
    return 0
  }
  if (hasBothTags) {
    return (marginLeft.value + 1) * codeLineMarginLeftUnitInPx
  }
  if (isOpenTag) {
    marginLeft.value += 1
    ml = marginLeft.value * codeLineMarginLeftUnitInPx
  }
  if (isCloseTag) {
    ml = marginLeft.value * codeLineMarginLeftUnitInPx
    marginLeft.value -= 1
  }
  return ml
}

const getCodeLines = (): CodeLine[] => {
  if (props.xml.includes("<?xml")) {
    return props.xml
      .split("\n")
      .filter((line) => line.length > 0)
      .map((line) => {
        const ml = caculateLineMarginLeft(line)
        return { text: line, marginLeft: ml }
      })
  }
  return []
}
const codeLines = ref(getCodeLines())
watch(
  () => props.xml,
  () => {
    codeLines.value = getCodeLines()
  }
)
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
          ><span>{{ line.text }}</span></code
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
      /* Disable select line number*/
      -webkit-touch-callout: none; /* iOS Safari */
      -webkit-user-select: none; /* Safari */
      -khtml-user-select: none; /* Konqueror HTML */
      -moz-user-select: none; /* Old versions of Firefox */
      -ms-user-select: none; /* Internet Explorer/Edge */
      user-select: none; /* Non-prefixed version, currently supported by Chrome, Edge, Opera and Firefox */
    }
    .line {
      padding-left: 20px;
    }
  }
}
</style>
