import { render } from "@testing-library/vue"
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import DocumentUnit from "@/domain/documentUnit"

describe("documentUnit InfoPanel", () => {
  it("renders documentNumber if given", async () => {
    const { getAllByText } = render(DocumentUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("123", { documentNumber: "foo" }),
      },
    })

    getAllByText("foo")
  })

  it("renders aktenzeichen if given", async () => {
    const { getAllByText } = render(DocumentUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("123", {
          coreData: {
            fileNumber: "foo",
          },
        }),
      },
    })

    getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Aktenzeichen/)
    })
    getAllByText((_content, node) => {
      return !!node?.textContent?.match(/foo/)
    })
  })

  it("renders placeholder for aktenzeichen if not given", async () => {
    const { getAllByText } = render(DocumentUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("123", {
          coreData: {
            decisionDate: "2024-01-31",
            courtType: "baz",
            fileNumber: undefined,
          },
          documentNumber: "qux",
        }),
      },
    })

    getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Aktenzeichen/)
    })
    getAllByText((_content, node) => {
      return !!node?.textContent?.match(/-/)
    })
  })

  it("renders Entscheidungsdatum if given", async () => {
    const { getAllByText } = render(DocumentUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("123", {
          coreData: {
            decisionDate: "2024-01-31",
          },
        }),
      },
    })

    getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Entscheidungsdatum/)
    })

    getAllByText((_content, node) => {
      return !!node?.textContent?.match(/31.01.2024/)
    })
  })

  it("renders placeholder for Entscheidungsdatum if not given", async () => {
    const { getAllByText } = render(DocumentUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("123", {
          coreData: {
            fileNumber: "foo",
            courtType: "baz",
          },
          documentNumber: "qux",
        }),
      },
    })

    getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Entscheidungsdatum -/)
    })
  })

  it("renders Gerichtstyp if given", async () => {
    const { getAllByText } = render(DocumentUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("123", {
          coreData: {
            courtType: "foo",
          },
        }),
      },
    })
    getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Gerichtstyp/)
    })

    getAllByText((_content, node) => {
      return !!node?.textContent?.match(/foo/)
    })
  })

  it("renders placeholder for Gerichtstyp if not given", async () => {
    const { getAllByText } = render(DocumentUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("123", {
          coreData: {
            fileNumber: "foo",
            decisionDate: "2024-01-31",
          },
          documentNumber: "qux",
        }),
      },
    })

    getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Gerichtstyp -/)
    })
  })
})
