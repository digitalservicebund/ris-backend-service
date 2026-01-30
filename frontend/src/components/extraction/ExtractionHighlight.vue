<script setup lang="ts">
import { getHTMLFromFragment } from "@tiptap/core"
import { NodeViewProps, NodeViewWrapper, NodeViewContent } from "@tiptap/vue-3"
import { storeToRefs } from "pinia"
import { ref, nextTick } from "vue"
import { COMBOBOX_REMOTE_SEARCH_EVENT } from "@/components/ComboboxInput.vue"
import { EDITABLE_LIST_FOCUS_EVENT } from "@/components/EditableList.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

import AddIcon from "~icons/ic/baseline-add"
import UndoIcon from "~icons/ic/baseline-check"

// eslint-disable-next-line vue/prop-name-casing
const props = defineProps<NodeViewProps>()

const FIELDS_WITH_OBJECTS = ["court", "documentType"]

const EXTRACTION_CLASS_LABELS: Record<string, string> = {
  tenor: "Tenor",
  guiding_principle: "Leitsatz",
  reasons: "Gründe",
  case_facts: "Tatbestand",
  decision_reasons: "Entscheidungsgründe",
  court: "Gericht",
  date: "Entscheidungsdatum",
  file_number: "Aktenzeichen",
  headline: "Überschrift",
  document_type: "Dokumenttyp",
}

const getClassLabel = (extractionClass: string): string =>
  EXTRACTION_CLASS_LABELS[extractionClass] || extractionClass
const toKebabCase = (str: string) => str.replaceAll("_", "-").toLowerCase()

const store = useDocumentUnitStore()
const { documentUnit: decision } = storeToRefs(store)

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function setVal<T>(obj: any, path: string, value: T): void {
  // TODO: find a better solution to resolve a target path like "previousDecisions.0.decisionDate"
  const keys = path.split(".")
  const last = keys.pop()!

  const target = keys.reduce((acc, k) => {
    if (!acc[k] || typeof acc[k] !== "object") {
      acc[k] = {}
    }
    return acc[k]
  }, obj)

  if (target && typeof target === "object") {
    target[last] = value
  } else {
    console.warn(`Unable to set value at path ${path}`, obj, target)
  }
}

const isHtml = (p: string) =>
  ["longTexts", "shortTexts"].includes(p.split(".")[0])

const isActive = ref(false)
const isAccepted = ref(false)
const ctaBtnRef = ref<HTMLButtonElement | null>(null)

const saveValue = async () => {
  const targetPath = props.node.attrs.targetPath as string

  if (isAccepted.value) {
    setVal<string | undefined>(decision.value!, targetPath, undefined)
    isAccepted.value = false
  } else {
    const _html = getHTMLFromFragment(props.node.content, props.editor.schema)
    const html = _html.replace(/<\/?mark[^>]*>/g, "")
    const normalizedText = props.node.attrs.normalizedText
    const stringValue = props.node.textContent

    const value = isHtml(targetPath) ? html : normalizedText || stringValue

    const pathParts = targetPath.split(".")

    // expand previousDecision entry in expandable list if needed
    if (targetPath.startsWith("previousDecisions")) {
      const index = parseInt(pathParts[1])
      window.dispatchEvent(
        new CustomEvent(EDITABLE_LIST_FOCUS_EVENT, {
          detail: { listId: "previousDecisions", index },
        }),
      )
      await nextTick() // wait for DOM update
    }

    const field = targetPath.startsWith("previousDecisions")
      ? pathParts[2] // e.g., previousDecisions.{index}.{field}
      : pathParts[1] // e.g., coreData.{field}
    // a) Fields with objects (e.g., court, documentType) trigger a query in ComboboxInput.vue to set the value
    if (FIELDS_WITH_OBJECTS.includes(field)) {
      window.dispatchEvent(
        // TODO: define ComboboxRemoteSearchEvent
        new CustomEvent(COMBOBOX_REMOTE_SEARCH_EVENT, {
          detail: { id: targetPath, query: value },
        }),
      )
    }
    // b) Regular string fields are set directly on the decision object
    else {
      setVal<string | undefined>(decision.value!, targetPath, value)
    }

    isAccepted.value = true

    console.log("UPDATE", targetPath, value)

    await nextTick() // wait for DOM update
    scrollToAndHighlight(targetPath)
  }
}

function scrollToAndHighlight(targetPath: string) {
  // TODO: this needs a better solution
  const [group, field] = targetPath.split(".")
  const targetEl =
    group === "previousDecisions"
      ? document.getElementById("previousDecisions")
      : ["court", "documentType"].includes(field)
        ? document.getElementById(field)?.parentElement
        : field === "fileNumbers"
          ? document.querySelector(
              `#fileNumbers div[aria-label="Aktenzeichen"]`,
            )
          : document.getElementById(field)
  if (targetEl) {
    targetEl.scrollIntoView({ behavior: "smooth", block: "center" })
    targetEl.classList.add("highlight-pulse")
    setTimeout(() => {
      targetEl.classList.remove("highlight-pulse")
    }, 2000)
  }
}

function handleClick() {
  isActive.value = true
  ctaBtnRef.value?.focus({ preventScroll: true })
}

const labelPrefix = props.node.attrs.targetPath?.startsWith("previousDecisions")
  ? "Vorinstanz: "
  : ""
</script>

<template>
  <NodeViewWrapper
    as="button"
    class="extraction"
    :class="[
      toKebabCase($props.node.attrs.extractionClass),
      {
        'has-path': $props.node.attrs.targetPath,
        active: isActive,
        accepted: isAccepted,
      },
    ]"
    :data-extraction-class="$props.node.attrs.extractionClass"
    :data-extraction-id="$props.node.attrs.extractionId"
    tabindex="-1"
    @click.stop="handleClick"
  >
    <NodeViewContent />
    <button
      ref="ctaBtnRef"
      class="cta-button"
      tabindex="0"
      @blur="isActive = false"
      @click="saveValue"
      @focus="isActive = true"
    >
      <component :is="isAccepted ? UndoIcon : AddIcon" />
      {{ labelPrefix + getClassLabel($props.node.attrs.extractionClass) }}
    </button>
  </NodeViewWrapper>
</template>

<style lang="css" scoped>
@keyframes highlight-pulse {
  0% {
    background-color: transparent;
  }

  10% {
    background-color: var(--color-yellow-200);
    outline: 4px solid var(--color-blue-800);
  }

  30% {
    background-color: transparent;
    outline: 4px solid transparent;
  }

  50% {
    background-color: var(--color-yellow-100);
    outline: 4px solid var(--color-blue-100);
  }

  100% {
    background-color: transparent;
    outline: 4px solid transparent;
  }
}

:global(.highlight-pulse) {
  animation: highlight-pulse 1.8s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  will-change: background-color, outline;
}

.extraction {
  position: relative;
  cursor: pointer;
  text-align: inherit;

  --outline-offset: 2px;
}

mark.extraction {
  display: inline-block;
}

.extraction:not(.has-path) {
  background-color: rgb(200 200 200 / 20%);
}

.extraction.accepted {
  background-color: var(--color-green-200) !important;
}

.extraction.has-path {
  background-color: rgb(var(--rgb) / 30%);
}

.extraction.tenor.has-path {
  --rgb: 59 130 246;
}

.extraction.reasons.has-path {
  --rgb: 168 85 247;
}

.extraction.case-facts.has-path {
  --rgb: 245 158 11;
}

.extraction.decision-reasons.has-path {
  --rgb: 168 85 247;
}

.extraction.headline.has-path {
  --rgb: 132 204 22;
}

.extraction.guiding-principle.has-path {
  --rgb: 249 115 22;
}

.extraction.date.has-path {
  --rgb: 6 182 212;
}

.extraction.file-number.has-path {
  --rgb: 239 68 68;
}

.extraction.court.has-path {
  --rgb: 234 179 8;
}

.extraction.document-type.has-path {
  --rgb: 236 72 153;
}

.extraction.has-path:hover {
  background-color: rgb(var(--rgb) / 50%);
}

.cta-button {
  position: absolute;
  z-index: 200;
  bottom: calc(-1 * var(--outline-offset));
  left: calc(-1 * var(--outline-offset) - 2px);
  display: none;
  display: flex;

  /* added this to prevent overflow-scroll in TextEditor */
  overflow: hidden;
  width: 0;
  height: 0;
  align-items: center;
  justify-content: flex-start;
  padding: 6px 14px 6px 6px;
  border: 2px solid transparent;
  background-color: var(--color-blue-800);
  color: #fff;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 700;
  gap: 4px;
  opacity: 0;
  outline: none;
  pointer-events: none;
  transform: translateY(100%);
  transition: background-color 0.2s;
  white-space: nowrap;
}

.cta-button:hover {
  background-color: var(--color-blue-700);
}

.cta-button:active {
  background-color: var(--color-blue-500);
}

.extraction.has-path.active > .cta-button {
  width: auto;
  height: auto;
  opacity: 1;
  pointer-events: auto;
}

.extraction.active {
  outline: 2px solid var(--color-blue-800);
  outline-offset: var(--outline-offset);
}
</style>
