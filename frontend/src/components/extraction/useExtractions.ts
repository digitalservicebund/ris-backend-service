import { storeToRefs } from "pinia"
import { Ref, ref } from "vue"
import type { Extraction } from "./types"
import TextEditor from "@/components/input/TextEditor.vue"
import extractionService, {
  ExtractionRequest,
  ExtractionResponse,
} from "@/services/extractionService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const BACKEND: "python" | "java" = "java"

type EditorRef = Ref<InstanceType<typeof TextEditor> | undefined>

async function getExtractionsFromPythonBackend(payload: ExtractionRequest) {
  const data = (await fetch("http://localhost:8000/api/extract-html", {
    method: "POST",
    body: JSON.stringify(payload),
    headers: { "Content-Type": "application/json; charset=utf-8" },
  }).then((r) => r.json())) as ExtractionResponse
  return { data }
}

function useExtractions(editorRef: EditorRef) {
  const prevHtml = ref<string | null>(null)
  const showExtractions = ref<boolean>(false)

  const store = useDocumentUnitStore()
  const { documentUnit: decision } = storeToRefs(store)

  async function toggleShowExtractions() {
    if (!showExtractions.value) {
      const editorHtml = editorRef.value?.editor.getHTML()
      if (!editorHtml) return
      const html = unescapeHtml(editorHtml)
      const court = decision.value?.coreData.court?.label
      const payload: ExtractionRequest = { html, court }
      const requestFunc =
        BACKEND === "java"
          ? extractionService.getExtractions
          : getExtractionsFromPythonBackend
      const { data } = await requestFunc(payload)
      const extractions = data?.extractions ?? []
      console.log(BACKEND, { extractions })
      const filteredExtractions = extractions.filter((e) => !!e.targetPath)
      const wrappedHtml = wrapHtmlString(html, filteredExtractions)

      prevHtml.value = editorHtml
      editorRef.value?.editor.commands.setContent(wrappedHtml)
      showExtractions.value = true
    } else {
      editorRef.value?.editor.commands.setContent(prevHtml.value)
      showExtractions.value = false
    }
  }

  return { showExtractions, toggleShowExtractions }
}

function unescapeHtml(html: string): string {
  const textArea = document.createElement("textarea")
  textArea.innerHTML = html
  return textArea.value
}

interface ExtractionEvent {
  pos: number
  type: "start" | "end"
  length: number
  index: number
  tag: string
}

function wrapHtmlString(htmlString: string, extractions: Extraction[]): string {
  const events: ExtractionEvent[] = []

  extractions.forEach((ext, index) => {
    if (!ext.charInterval) return
    const start = ext.charInterval.startPos
    const end = ext.charInterval.endPos
    const length = end - start
    const targetPath = ext.targetPath || ""

    const el = ext.isSection ? "div" : "mark"

    events.push({
      pos: start,
      type: "start",
      length,
      index,
      tag: `<${el} data-extraction-class="${ext.extractionClass}" data-extraction-id="${ext.id}" data-target-path="${targetPath}" data-normalized-text="${ext.normalizedText || ""}">`,
    })

    events.push({
      pos: end,
      type: "end",
      length,
      index,
      tag: `</${el}>`,
    })
  })

  events.sort((a, b) => {
    if (a.pos !== b.pos) return a.pos - b.pos
    const typeA = a.type === "end" ? 0 : 1
    const typeB = b.type === "end" ? 0 : 1
    if (typeA !== typeB) return typeA - typeB

    // Nesting logic: Longest start first, Shortest end first
    if (a.type === "start") {
      if (a.length !== b.length) return b.length - a.length
      return a.index - b.index
    } else {
      if (a.length !== b.length) return a.length - b.length
      return b.index - a.index
    }
  })

  let result = ""
  let currentIdx = 0

  for (const event of events) {
    if (event.pos > currentIdx) {
      result += htmlString.substring(currentIdx, event.pos)
      currentIdx = event.pos
    }
    result += event.tag
  }

  if (currentIdx < htmlString.length) result += htmlString.substring(currentIdx)
  console.log({ result })
  return result
}

export { useExtractions }
