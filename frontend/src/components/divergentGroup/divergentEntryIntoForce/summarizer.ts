import { createTextVNode, VNode } from "vue"
import {
  divergentDefinedSummary,
  divergentUndefinedSummary,
} from "@/components/divergentGroup/divergentSummaryFunctions"
import { MetadataSections } from "@/domain/norm"

export function divergentEntryIntoForceSummarizer(
  data?: MetadataSections,
): VNode {
  if (!data) return createTextVNode("")

  if (data.DIVERGENT_ENTRY_INTO_FORCE_DEFINED) {
    return divergentDefinedSummary(data.DIVERGENT_ENTRY_INTO_FORCE_DEFINED[0])
  } else if (data.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED) {
    return divergentUndefinedSummary(
      data.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED[0],
    )
  } else return createTextVNode("")
}
