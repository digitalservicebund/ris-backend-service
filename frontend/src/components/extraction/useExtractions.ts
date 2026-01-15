import { storeToRefs } from "pinia"
import { Ref, ref } from "vue"
import type { Extraction } from "./types"
import TextEditor from "@/components/input/TextEditor.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

type EditorRef = Ref<InstanceType<typeof TextEditor> | undefined>

const SECTION_CLASSES = new Set([
  "tenor",
  "reasons",
  "case_facts",
  "decision_reasons",
  "guiding_principle",
  "headline",
])

function useExtractions(editorRef: EditorRef) {
  const prevHtml = ref<string | null>(null)
  const showExtractions = ref<boolean>(false)

  const store = useDocumentUnitStore()
  const { documentUnit: decision } = storeToRefs(store)

  async function toggleShowExtractions() {
    if (!showExtractions.value) {
      const editorHtml = editorRef.value?.editor.getHTML()
      if (!editorHtml) return
      const html_str = unescapeHtml(editorHtml)
      const court = decision.value?.coreData.court?.label
      const data = await fetch("http://localhost:8000/api/extract-html", {
        method: "POST",
        body: JSON.stringify({ html_str, court }),
        headers: { "Content-Type": "application/json; charset=utf-8" },
      }).then((r) => r.json())
      const targetPaths = getTargetPaths(data)
      const extractions = (data.extractions as Extraction[])
        .map((e) => ({
          ...e,
          targetPath: targetPaths.get(e.id.toString()),
          isSection: SECTION_CLASSES.has(e.extraction_class),
        }))
        .filter((e) => !!e.targetPath) as Extraction[]
      const wrappedHtml = wrapHtmlString(html_str, extractions)

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

function getTargetPaths(result: {
  extracted_json_prov?: unknown
}): Map<string, string> {
  const targetPaths = new Map<string, string>()
  const collectLeafStrings = (obj: unknown, path: string = "") => {
    if (typeof obj === "string") {
      targetPaths.set(obj, path)
    } else if (obj && typeof obj === "object") {
      Object.entries(obj).forEach(([key, value]) => {
        const newPath = path ? `${path}.${key}` : key
        collectLeafStrings(value, newPath)
      })
    }
  }
  if (result.extracted_json_prov) collectLeafStrings(result.extracted_json_prov)
  return targetPaths
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
    if (!ext.char_interval) return
    const start = ext.char_interval.start_pos
    const end = ext.char_interval.end_pos
    const length = end - start
    const targetPath = ext.targetPath || ""

    const el = ext.isSection ? "div" : "mark"

    events.push({
      pos: start,
      type: "start",
      length,
      index,
      tag: `<${el} data-extraction-class="${ext.extraction_class}" data-extraction-id="${ext.id}" data-target-path="${targetPath}" data-normalized-value="${ext.normalizedValue || ""}">`,
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

  return result
}

export { useExtractions }
