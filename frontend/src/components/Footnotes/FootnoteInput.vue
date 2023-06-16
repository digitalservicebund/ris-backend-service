<script lang="ts" setup>
import { computed } from "vue"
import SegmentEditor, {
  Segment,
  SuggestionGroupOptions,
} from "./SegmentEditor.vue"
import {Footnote, FOOTNOTE_TYPE_TO_LABEL_MAPPING, FootnoteSectionType} from "@/components/Footnotes/types";

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ({ parts: [] }),
})

const emit = defineEmits<Emits>()
interface Props {
  modelValue?: Footnote
}
interface Emits {
  (event: "update:modelValue", value?: Footnote): void
}
const inputValue = computed({
  get: () => parseFootnoteAsSegments(props.modelValue),
  set: (segments: Segment[]) =>
    emit("update:modelValue", parseSegmentsAsFootnote(segments)),
})

const FOOTNOTE_SEGMENT_TYPE = "footnote_type"

function parseFootnoteAsSegments(footnote: Footnote): Segment[] {
  const segments = footnote.parts
    .map((part): Segment[] => {
      const segments = []
      part.type &&
        segments.push({
          type: FOOTNOTE_SEGMENT_TYPE,
          content: FOOTNOTE_TYPE_TO_LABEL_MAPPING[part.type],
          id: part.type,
        })
      part.content && segments.push({ type: "text", content: part.content })
      return segments
    })
    .reduce(
      (allSegments, currentValue) => [...allSegments, ...currentValue],
      []
    )
  footnote.prefix &&
    segments.unshift({ type: "text", content: footnote.prefix })
  return segments
}
function parseSegmentsAsFootnote(segments: Segment[]): Footnote {
  const footnote: Footnote = { parts: [] }
  let partIndex = 0
  if (segments.length > 0 && segments[0].type == "text") {
    footnote.prefix = segments[0].content
    partIndex = 1
  }
  while (partIndex < segments.length) {
    const segment = segments[partIndex]
    const hasFootnoteType = segment.type == FOOTNOTE_SEGMENT_TYPE
    const footnoteType = hasFootnoteType
      ? (segment.id as FootnoteSectionType)
      : undefined
    const nextSegment = segments[partIndex + 1] ?? ({} as Segment)
    const hasFootnoteContent = nextSegment.type == "text"
    const footnoteContent = hasFootnoteContent ? nextSegment.content : undefined
    footnote.parts.push({ type: footnoteType, content: footnoteContent })
    partIndex += hasFootnoteContent ? 2 : 1
  }
  return footnote
}
const options: SuggestionGroupOptions = {
  segmentType: FOOTNOTE_SEGMENT_TYPE,
  trigger: "@",
  elementClasses: ["bg-yellow-300", "rounded", "px-10", "py-2"],
  callback: (input: string) =>
    Object.entries(FOOTNOTE_TYPE_TO_LABEL_MAPPING)
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      .filter(([_, label]) =>
        label.toLowerCase().startsWith(input.toLowerCase())
      )
      .map(([id, label]) => ({ label, id })),
}
</script>

<template>
  <div>
    <span class="text-gray-900">
      Sie können mit @ den Fußnoten-Typ wählen (z.B. Änderungsfußnote,
      Kommentierende Fußnote)
    </span>

    <SegmentEditor v-model="inputValue" :suggestions="[options]" />
  </div>
</template>
