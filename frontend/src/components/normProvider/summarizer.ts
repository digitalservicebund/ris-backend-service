import { h } from "vue"
import CheckMark from "@/assets/icons/ckeckbox_regular.svg"
import { Metadata } from "@/domain/Norm"

export function normProviderSummarizer(data: Metadata) {
  if (!data) return ""

  const entity = data.ENTITY?.[0]
  const decidingBody = data.DECIDING_BODY?.[0]
  const isResolutionMajority = data.RESOLUTION_MAJORITY?.[0]

  const summaryLine = [entity, decidingBody]
    .filter((value) => value != "" && value != null)
    .join(" | ")

  if (isResolutionMajority) {
    return h("div", { class: ["flex", "gap-8"] }, [
      h(
        "span",
        summaryLine.length == 0 ? summaryLine : summaryLine.concat(" | ")
      ),
      h("img", {
        src: CheckMark,
        width: "16",
        alt: "Schwarzes Haken",
      }),
      h("span", "Beschlussfassung mit qual. Mehrheit"),
    ])
  } else {
    return summaryLine
  }
}
