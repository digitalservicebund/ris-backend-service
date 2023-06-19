<script lang="ts" setup>
import { computed } from "vue"
import SegmentEditor, {
  Segment,
  SuggestionGroupOptions,
} from "../SegmentEditor.vue"
import { Footnote, FOOTNOTE_LABELS } from "@/components/Footnotes/types"
import { MetadatumType } from "@/domain/Norm"

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ({} as Footnote),
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

function parseFootnoteAsSegments(section): Segment[] {
  const segments = []
  section?.FOOTNOTE?.forEach((footnote) => {
    const metadatumType = Object.keys(footnote)[0]
    if (metadatumType != MetadatumType.FOOTNOTE_REFERENCE) {
      segments.push({
        type: FOOTNOTE_SEGMENT_TYPE,
        content: FOOTNOTE_LABELS[metadatumType],
        id: MetadatumType[metadatumType],
      })
    }
    segments.push({ type: "text", content: Object.values(footnote)[0][0] })
  })
  return segments
}
function parseSegmentsAsFootnote(segments: Segment[]): Footnote {
  const footnote: Footnote = { FOOTNOTE: [] }
  let partIndex = 0
  if (segments.length > 0 && segments[0].type == "text") {
    footnote.FOOTNOTE.push({ FOOTNOTE_REFERENCE: [segments[0].content] })
    partIndex = 1
  }
  while (partIndex < segments.length) {
    const segment = segments[partIndex]
    const hasFootnoteType = segment.type == FOOTNOTE_SEGMENT_TYPE
    const footnoteType: MetadatumType | undefined = hasFootnoteType
      ? (segment.id as MetadatumType)
      : undefined
    const nextSegment = segments[partIndex + 1] ?? ({} as Segment)
    const hasFootnoteContent = nextSegment.type == "text"
    const footnoteContent = hasFootnoteContent ? nextSegment.content : undefined
    footnote.FOOTNOTE.push({ [footnoteType]: [footnoteContent] })
    partIndex += hasFootnoteContent ? 2 : 1
  }
  return footnote
}

const options: SuggestionGroupOptions = {
  segmentType: FOOTNOTE_SEGMENT_TYPE,
  trigger: "@",
  elementClasses: ["bg-yellow-300", "rounded", "px-10", "py-2"],
  callback: (input: string) =>
    Object.entries(FOOTNOTE_LABELS)
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
