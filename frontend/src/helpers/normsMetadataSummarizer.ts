import dayjs from "dayjs"
import { createTextVNode, h, VNode } from "vue"
import CheckMark from "@/assets/icons/ckeckbox_regular.svg"

export enum Type {
  STRING,
  DATE,
  CHECKMARK,
  CHIP,
  BOLD_IN_ONE_LINE,
  FOOTNOTE,
}

export class SummarizerDataSet {
  value: string[]
  type: Type
  format: string | undefined
  separator: string | undefined

  constructor(
    value: string[],
    options: { type?: Type; format?: string; separator?: string } = {},
  ) {
    this.value = value
    this.type = options.type ?? Type.STRING
    this.format = options.format
    this.separator = options.separator
  }
}

export function normsMetadataSummarizer(
  data: SummarizerDataSet[],
  metadataTypeSeparator: string = "|",
): VNode {
  const propertyNodes: VNode[] = []
  const footnotes: [VNode | undefined, VNode[]] = [undefined, []]

  data.forEach((data, index) => {
    if (index > 0 && metadataTypeSeparator !== "") {
      propertyNodes.push(
        h("div", { class: ["text-gray-700"] }, metadataTypeSeparator),
      )
    }

    const processStringValue = (
      lastValue: boolean,
      value: string,
      separator: string | undefined,
    ) => {
      propertyNodes.push(
        h("div", {}, value + (separator && !lastValue ? separator : "")),
      )
    }

    const processDateValue = (value: string, format: string | undefined) => {
      const formattedValue = dayjs(value).format(format)
      propertyNodes.push(h("div", {}, formattedValue))
    }

    const processCheckmarkValue = (value: string) => {
      const checkmarkNode = h("img", {
        src: CheckMark,
        alt: "Schwarzes Haken",
        width: "16",
      })
      const valueNode = h("span", value)
      propertyNodes.push(
        h("div", { class: ["flex", "gap-4"] }, [checkmarkNode, valueNode]),
      )
    }

    const processChipValue = (value: string) => {
      propertyNodes.push(
        h(
          "div",
          { class: ["bg-blue-500", "rounded-lg", "px-8", "py-4"] },
          value,
        ),
      )
    }

    const processBoldInOneLineValue = (value: string) => {
      footnotes[0] = h(
        "span",
        { class: ["pr-10", "font-bold"] },
        value.trim().replace(/\n/g, "<br>"),
      )
    }

    const processFootnoteValue = (type: string, content: string) => {
      const typeNode = h(
        "span",
        {
          class: ["bg-yellow-400", "rounded", "px-6", "py-2", "whitespace-pre"],
        },
        type,
      )
      const contentTextNode = h(
        "p",
        { class: ["pl-6", "pr-10", "inline", "whitespace-pre-wrap"] },
        content,
      )
      footnotes[1].push(
        h("span", { class: "leading-loose" }, [typeNode, contentTextNode]),
      )
    }

    switch (data.type) {
      case Type.STRING:
        data.value.forEach((value, index) =>
          processStringValue(
            index === data.value.length - 1,
            value,
            data.separator,
          ),
        )
        break
      case Type.DATE:
        data.value.forEach((value) => processDateValue(value, data.format))
        break
      case Type.CHECKMARK:
        data.value.forEach(processCheckmarkValue)
        break
      case Type.CHIP:
        data.value.forEach(processChipValue)
        break
      case Type.BOLD_IN_ONE_LINE:
        data.value.forEach(processBoldInOneLineValue)
        break
      case Type.FOOTNOTE:
        processFootnoteValue(data.value[0], data.value[1])
        break
    }
  })

  if (footnotes[1].length > 0) {
    return h("div", { class: ["flex", "flex-col", "gap-10"] }, [
      footnotes[0],
      h("div", footnotes[1]),
    ])
  }

  if (propertyNodes.length === 0) {
    return createTextVNode("")
  }

  return h(
    "div",
    { class: ["flex", "gap-8", "items-center", "flex-wrap"] },
    propertyNodes,
  )
}
