import { render, screen } from "@testing-library/vue"
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import DocumentUnit from "@/domain/documentUnit"

describe("documentUnit InfoPanel", () => {
  it("renders documentNumber if given", async () => {
    render(DocumentUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("123", { documentNumber: "foo" }),
      },
    })

    screen.getAllByText("foo")
  })

  it("renders aktenzeichen if given", async () => {
    render(DocumentUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("123", {
          coreData: {
            fileNumbers: ["foo"],
          },
        }),
      },
    })

    screen.getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Aktenzeichen/)
    })
    screen.getAllByText((_content, node) => {
      return !!node?.textContent?.match(/foo/)
    })
  })

  it("renders placeholder for aktenzeichen if not given", async () => {
    render(DocumentUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("123", {
          coreData: {
            decisionDate: "2024-01-31",
            court: {
              type: "baz",
              location: "baz",
              label: "baz",
            },
            fileNumbers: undefined,
          },
          documentNumber: "qux",
        }),
      },
    })

    screen.getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Aktenzeichen/)
    })
    screen.getAllByText((_content, node) => {
      return !!node?.textContent?.match(/-/)
    })
  })

  it("renders Entscheidungsdatum if given", async () => {
    render(DocumentUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("123", {
          coreData: {
            decisionDate: "2024-01-31",
          },
        }),
      },
    })

    screen.getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Entscheidungsdatum/)
    })

    screen.getAllByText((_content, node) => {
      return !!node?.textContent?.match(/31.01.2024/)
    })
  })

  it("renders placeholder for Entscheidungsdatum if not given", async () => {
    render(DocumentUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("123", {
          coreData: {
            fileNumbers: ["foo"],
            court: {
              type: "baz",
              location: "baz",
              label: "baz",
            },
          },
          documentNumber: "qux",
        }),
      },
    })

    screen.getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Entscheidungsdatum -/)
    })
  })

  it("renders Gerichtstyp if given", async () => {
    render(DocumentUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("123", {
          coreData: {
            court: {
              type: "foo",
              location: "foo",
              label: "foo",
            },
          },
        }),
      },
    })
    screen.getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Gericht/)
    })

    screen.getAllByText((_content, node) => {
      return !!node?.textContent?.match(/foo/)
    })
  })

  it("renders placeholder for Gerichtstyp if not given", async () => {
    render(DocumentUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("123", {
          coreData: {
            fileNumbers: ["foo"],
            decisionDate: "2024-01-31",
          },
          documentNumber: "qux",
        }),
      },
    })

    screen.getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Gericht -/)
    })
  })
})
