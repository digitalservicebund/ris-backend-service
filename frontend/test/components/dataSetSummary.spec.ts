import { render, screen } from "@testing-library/vue"
import type { VNode } from "vue"
import { h } from "vue"

import DataSetSummary from "@/components/DataSetSummary.vue"

function renderComponent(options?: {
  data?: unknown | unknown[]
  summarizer?: (dataEntry: unknown) => string | VNode
}) {
  const props = {
    data: options?.data ?? ["foo", "bar"],
    summarizer: options?.summarizer,
  }

  render(DataSetSummary, { props })
}

describe("DataSetSummary", () => {
  it("keeps string values as they are for the default summary", async () => {
    renderComponent({ data: ["some test value"] })

    const summary = await screen.findByText("some test value")

    expect(summary).toBeInTheDocument()
  })

  it("displays a summary for each entry in the dataset in correct order", async () => {
    renderComponent({ data: ["first value", "second value", "third value"] })

    const firstSummary = await screen.findByText("first value")
    const secondSummary = await screen.findByText("second value")
    const thirdSummary = await screen.findByText("third value")

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

  it("summarizes simple data set entries using the string conversion", async () => {
    renderComponent({ data: ["foo", 1, true] })

    const stringSummary = await screen.findByText("foo")
    const numberSummary = await screen.findByText("1")
    const booleanSummary = await screen.findByText("true")

    expect(stringSummary).toBeInTheDocument()
    expect(numberSummary).toBeInTheDocument()
    expect(booleanSummary).toBeInTheDocument()
  })

  it(" summarizes data set entries that are lists with a comma separator", async () => {
    renderComponent({ data: [["foo", "bar", "baz"]] })

    const summary = await screen.findByText("foo, bar, baz")

    expect(summary).toBeInTheDocument()
  })

  it("summarizes data set entries that are an object using their values and a pipe separator", async () => {
    renderComponent({ data: [{ foo: 1, bar: 2, baz: 3 }] })

    const summary = await screen.findByText("1 | 2 | 3")

    expect(summary).toBeInTheDocument()
  })

  it("summarizes complex nested data set structures of mixed types", async () => {
    renderComponent({ data: [{ foo: "foo", bar: [1, 2], baz: true }] })

    const summary = await screen.findByText("foo | 1, 2 | true")

    expect(summary).toBeInTheDocument()
  })

  it("summarizes data sets that are not a list as single entry", async () => {
    renderComponent({ data: { foo: "foo", bar: "bar" } })

    const summary = await screen.findByText("foo ar")

    expect(summary).toBeInTheDocument()
  })

  it("uses custom string summarizer function if given for custom summaries", async () => {
    const summarizer = (dataEntry: unknown) => `the value is: ${dataEntry}`
    renderComponent({ data: ["foo"], summarizer })

    const summary = await screen.findByText("the value is: foo")

    expect(summary).toBeInTheDocument()
  })

  it("uses custom VNode summarizer function if given for complex summaries", async () => {
    const summarizer = (dataEntry: unknown) =>
      h("div", { "data-testid": "special-identifier" }, dataEntry as string)

    renderComponent({ data: ["foo"], summarizer })

    const summary = await screen.findByTestId("special-identifier")

    expect(summary).toBeInTheDocument()
  })
})
