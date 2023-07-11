import { VNode } from "vue"
import {
  divergentDefinedSummary,
  divergentUndefinedSummary,
} from "@/components/divergentGroup/divergentSummaryFunctions"
import { MetadataSections } from "@/domain/Norm"

export function divergentEntryIntoForceSummarizer(
  data: MetadataSections
): VNode | string {
  if (!data) return ""

  if (data.DIVERGENT_ENTRY_INTO_FORCE_DEFINED) {
    return divergentDefinedSummary(data.DIVERGENT_ENTRY_INTO_FORCE_DEFINED[0])
  } else if (data.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED) {
    return divergentUndefinedSummary(
      data.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED[0]
    )
  } else return ""
}
