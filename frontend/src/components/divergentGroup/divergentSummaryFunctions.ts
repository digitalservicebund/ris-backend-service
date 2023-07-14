import dayjs from "dayjs"
import { createTextVNode, h, VNode } from "vue"
import CheckMark from "@/assets/icons/ckeckbox_regular.svg"
import { Metadata } from "@/domain/Norm"
import { getLabel } from "@/helpers/generalSummarizer"

export const NORM_CATEGORY_TRANSLATIONS = {
  AMENDMENT_NORM: "Änderungsnorm",
  BASE_NORM: "Stammnorm",
  TRANSITIONAL_NORM: "Übergangsnorm",
}

export function divergentDefinedSummary(data: Metadata): VNode {
  if (!data) return createTextVNode("")

  const date = data.DATE?.[0]
    ? dayjs(data.DATE[0]).format("DD.MM.YYYY")
    : undefined
  const categories =
    data?.NORM_CATEGORY?.filter((category) => category != null) ?? []

  if (categories.length === 0 && date) {
    return h("div", {}, date)
  }

  const elements = []

  if (date) {
    elements.push(h("div", {}, date))
  }

  if (date && categories.length > 0) {
    elements.push(h("div", "|"))
  }

  categories.forEach((category) => {
    elements.push(
      h("div", { class: ["flex", "gap-8"] }, [
        h("img", {
          src: CheckMark,
          width: "16",
          alt: "Schwarzes Haken",
        }),
        h("span", {}, NORM_CATEGORY_TRANSLATIONS[category]),
      ]),
    )
  })

  if (elements.length === 0) {
    return createTextVNode("")
  } else {
    return h("div", { class: ["flex", "gap-8"] }, elements)
  }
}
export function divergentUndefinedSummary(data: Metadata): VNode {
  if (!data) return createTextVNode("")

  const undefinedDate = data?.UNDEFINED_DATE?.[0]
  const categories =
    data?.NORM_CATEGORY?.filter((category) => category != null) ?? []

  const elements = []

  if (categories.length === 0 && undefinedDate) {
    return h("div", {}, getLabel(undefinedDate))
  }

  if (undefinedDate) {
    elements.push(h("div", {}, getLabel(undefinedDate)))
  }

  if (undefinedDate && categories.length > 0) {
    elements.push(h("div", "|"))
  }

  categories.forEach((category) => {
    elements.push(
      h("div", { class: ["flex", "gap-8"] }, [
        h("img", {
          src: CheckMark,
          width: "16",
          alt: "Schwarzes Haken",
        }),
        h("span", {}, NORM_CATEGORY_TRANSLATIONS[category]),
      ]),
    )
  })

  return h("div", { class: ["flex", "gap-8"] }, elements)
}
