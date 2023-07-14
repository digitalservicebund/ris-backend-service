import { createTextVNode, h, VNode } from "vue"
import CheckMark from "@/assets/icons/ckeckbox_regular.svg"
import { NORM_CATEGORY_TRANSLATIONS } from "@/components/divergentGroup/divergentSummaryFunctions"
import { Metadata } from "@/domain/Norm"

export function documentTypeSummarizer(data?: Metadata): VNode {
  if (!data) return createTextVNode("")

  const propertyNodes = []

  const typeName = data?.TYPE_NAME?.[0]
  if (typeName) propertyNodes.push(h("div", {}, typeName))

  const categories =
    data?.NORM_CATEGORY?.filter((category) => category != null) ?? []
  const templateNames = data?.TEMPLATE_NAME ?? []

  if (typeName && categories.length > 0) propertyNodes.push(h("div", "|"))

  categories.forEach((category) =>
    propertyNodes.push(
      h("div", { class: ["flex", "gap-4"] }, [
        h("img", { src: CheckMark, alt: "checkmark", width: "16" }),
        h("span", NORM_CATEGORY_TRANSLATIONS[category]),
      ]),
    ),
  )

  if ((typeName || categories.length > 0) && templateNames.length > 0)
    propertyNodes.push(h("div", "|"))

  templateNames.forEach((templateName) =>
    propertyNodes.push(
      h(
        "div",
        { class: ["bg-blue-500", "rounded-lg", "px-8", "py-4"] },
        templateName,
      ),
    ),
  )

  if (propertyNodes.length === 0) {
    return createTextVNode("")
  } else {
    return h(
      "div",
      { class: ["flex", "gap-8", "items-center", "flex-wrap"] },
      propertyNodes,
    )
  }
}
