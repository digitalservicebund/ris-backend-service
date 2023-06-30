import { render, screen } from "@testing-library/vue"
import type { VNode } from "vue"
import { h } from "vue"

import DataSetSummary from "@/shared/components/DataSetSummary.vue"

function renderComponent<T>(options?: {
  data?: T | T[]
  summarizer?: (dataEntry: T) => string | VNode
}) {
  const props = {
    data: options?.data ?? ["foo", "bar"],
    summarizer: options?.summarizer,
  }

  render(DataSetSummary, { props })
}

describe("DataSetSummary", () => {
  it("keeps string values as they are for the default summary", () => {
    renderComponent({ data: ["some test value"] })

    const summary = screen.queryByText("some test value", { exact: true })

    expect(summary).toBeInTheDocument()
  })

  it("displays a summary for each entry in the dataset in correct order", () => {
    renderComponent({ data: ["first value", "second value", "third value"] })

    const firstSummary = screen.getByText("first value", { exact: true })
    const secondSummary = screen.getByText("second value", { exact: true })
    const thirdSummary = screen.getByText("third value", { exact: true })

    expect(firstSummary).toBeInTheDocument()
    expect(secondSummary).toBeInTheDocument()
    expect(thirdSummary).toBeInTheDocument()
    expect(firstSummary.compareDocumentPosition(secondSummary)).toBe(
      Node.DOCUMENT_POSITION_FOLLOWING
    )
    expect(secondSummary.compareDocumentPosition(thirdSummary)).toBe(
      Node.DOCUMENT_POSITION_FOLLOWING
    )
  })

  it("summarizes simple data set entries using the string conversion", () => {
    renderComponent({ data: ["foo", 1, true] })

    const stringSummary = screen.queryByText("foo", { exact: true })
    const numberSummary = screen.queryByText("1", { exact: true })
    const booleanSummary = screen.queryByText("true", { exact: true })

    expect(stringSummary).toBeInTheDocument()
    expect(numberSummary).toBeInTheDocument()
    expect(booleanSummary).toBeInTheDocument()
  })

  it(" summarizes data set entries that are lists with a comma separator", () => {
    renderComponent({ data: [["foo", "bar", "baz"]] })

    const summary = screen.queryByText("foo, bar, baz")

    expect(summary).toBeInTheDocument()
  })

  it("summarizes data set entries that are an object using their values and a pipe separator", () => {
    renderComponent({ data: [{ foo: 1, bar: 2, baz: 3 }] })

    const summary = screen.queryByText("1 | 2 | 3")

    expect(summary).toBeInTheDocument()
  })

  it("summarizes complex nested data set structures of mixed types", () => {
    renderComponent({ data: [{ foo: "foo", bar: [1, 2], baz: true }] })

    const summary = screen.queryByText("foo | 1, 2 | true")

    expect(summary).toBeInTheDocument()
  })

  it("summarizes data sets that are not a list as single entry", () => {
    renderComponent({ data: { foo: "foo", bar: "bar" } })

    const summary = screen.queryByText("foo | bar")

    expect(summary).toBeInTheDocument()
  })

  it("skips missing data set parts for summary", () => {
    renderComponent({ data: { foo: undefined, bar: "bar" } })

    const summary = screen.queryByText("bar", { exact: true })

    expect(summary).toBeInTheDocument()
  })

  it("uses custom string summarizer function if given for custom summaries", () => {
    const summarizer = (dataEntry: string) => `the value is: ${dataEntry}`
    renderComponent({ data: ["foo"], summarizer })

    const summary = screen.queryByText("the value is: foo")

    expect(summary).toBeInTheDocument()
  })

  it("uses custom VNode summarizer function if given for complex summaries", () => {
    const summarizer = (dataEntry: string) =>
      h("div", { "data-testid": "special-identifier" }, dataEntry as string)

    renderComponent({ data: ["foo"], summarizer })

    const summary = screen.queryByTestId("special-identifier")

    expect(summary).toBeInTheDocument()
  })
})
