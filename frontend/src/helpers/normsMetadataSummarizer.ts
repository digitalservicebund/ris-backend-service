import dayjs from "dayjs"
import { createTextVNode, h, VNode } from "vue"
import IconCheck from "~icons/ic/baseline-check"

function processDateValue(value: string, format: string | undefined): VNode {
  const formattedValue = dayjs(value).format(format)
  return h("div", {}, formattedValue)
}

function processStringValue(
  lastValue: boolean,
  value: string,
  separator: string | undefined,
): VNode {
  return h("div", {}, value + (separator && !lastValue ? separator : ""))
}

function processCheckmarkValue(value: string): VNode {
  const checkmarkNode = h(IconCheck)
  const valueNode = h("span", value)
  return h("div", { class: ["flex", "gap-4"] }, [checkmarkNode, valueNode])
}

export enum Type {
  STRING,
  DATE,
  CHECKMARK,
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
  metadataTypeSeparator = "|",
): VNode {
  const propertyNodes: VNode[] = []

  data.forEach((data, index) => {
    if (index > 0 && metadataTypeSeparator !== "") {
      propertyNodes.push(
        h("div", { class: ["text-gray-700"] }, metadataTypeSeparator),
      )
    }

    switch (data.type) {
      case Type.STRING:
        data.value.forEach((value, index) =>
          propertyNodes.push(
            processStringValue(
              index === data.value.length - 1,
              value,
              data.separator,
            ),
          ),
        )
        break
      case Type.DATE:
        data.value.forEach((value) =>
          propertyNodes.push(processDateValue(value, data.format)),
        )
        break
      case Type.CHECKMARK:
        data.value.forEach((value) =>
          propertyNodes.push(processCheckmarkValue(value)),
        )
        break
    }
  })

  if (propertyNodes.length === 0) {
    return createTextVNode("")
  }

  return h(
    "div",
    { class: ["flex", "gap-8", "items-center", "flex-wrap"] },
    propertyNodes,
  )
}
