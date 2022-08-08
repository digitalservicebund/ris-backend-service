<script lang="ts" setup>
import { ref, onUpdated, onMounted } from "vue"

const props = defineProps<{
  title: string
  xml: string
}>()

type CodeLine = {
  codeLine: string
  marginLeft: number
}
const codeLineMarginLeftUnit = 20
const shouldUpdateMarginLeft = ref<boolean>(false)
const caculateLineMarginLeft = (line: string): number => {
  const isXMLTag = line.includes("<?xml")
  const isDocTypeTag = line.includes("<!DOCTYPE")
  const hasCloseTag = line.includes("</") || line.includes("/>")
  const isOpenTag = line.includes("<") && line.includes(">")
  if (isXMLTag || isDocTypeTag) return 0
  if (hasCloseTag) {
    marginLeft.value -= 1
    shouldUpdateMarginLeft.value = false
    return marginLeft.value * codeLineMarginLeftUnit
  }
  if (isOpenTag) {
    if (shouldUpdateMarginLeft.value) {
      marginLeft.value += 1
    }
    shouldUpdateMarginLeft.value = true
    return marginLeft.value * codeLineMarginLeftUnit
  }
  marginLeft.value += 1
  return marginLeft.value * codeLineMarginLeftUnit
}

const codeLines = ref<Array<CodeLine>>([])
const marginLeft = ref<number>(0)
const getCodeLines = (): Array<CodeLine> => {
  let codeLinesObjArr: Array<CodeLine> = []
  if (props.xml.includes("<?xml")) {
    const codeLines = props.xml
      .split("<")
      .map((s) => {
        return (s.includes(">") ? "<" + s : s)
          .split(">")
          .map((s) => (s.includes("<") ? s + ">" : s))
          .filter((s) => s.length > 0)
      })
      .filter((arr) => arr.length > 0)
      .reduce((arr, curVal) => {
        return arr.concat(curVal)
      }, [])
    codeLinesObjArr = codeLines.map((codeLine) => {
      const ml = caculateLineMarginLeft(codeLine)
      const codeLineObj = { codeLine: codeLine, marginLeft: ml }
      return codeLineObj
    })
  }
  return codeLinesObjArr
}
onUpdated(() => {
  marginLeft.value = 0
  codeLines.value = getCodeLines()
})
onMounted(() => {
  marginLeft.value = 0
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
          ><span>{{ line.codeLine }}</span></code
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
    height: 300px;
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
