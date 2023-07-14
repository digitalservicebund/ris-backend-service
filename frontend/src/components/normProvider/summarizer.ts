import { createTextVNode, h, VNode } from "vue"
import CheckMark from "@/assets/icons/ckeckbox_regular.svg"
import { Metadata } from "@/domain/Norm"

export function normProviderSummarizer(data?: Metadata): VNode {
  if (!data) return createTextVNode("")

  const entity = data.ENTITY?.[0]
  const decidingBody = data.DECIDING_BODY?.[0]
  const isResolutionMajority = data.RESOLUTION_MAJORITY?.[0]

  const propertyNodes = []
  if (entity) propertyNodes.push(h("div", {}, entity))

  if (entity && decidingBody) {
    propertyNodes.push(h("div", "|"))
    propertyNodes.push(h("div", {}, decidingBody))
  } else if (decidingBody) {
    propertyNodes.push(h("div", {}, decidingBody))
  }

  if (isResolutionMajority) {
    if (entity || decidingBody) {
      propertyNodes.push(h("div", "|"))
    }
    propertyNodes.push(
      h("div", { class: ["flex", "gap-4"] }, [
        h("img", { src: CheckMark, alt: "checkmark", width: "16" }),
        h("span", "Beschlussfassung mit qual. Mehrheit"),
      ]),
    )
  }

  if (propertyNodes.length === 0) {
    return createTextVNode("")
  } else {
    return h("div", { class: ["flex", "gap-8"] }, propertyNodes)
  }
}
