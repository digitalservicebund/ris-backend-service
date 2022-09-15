import { render } from "@testing-library/vue"
import { createVuetify } from "vuetify"
import DocUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import DocumentUnit from "@/domain/documentUnit"

describe("documentUnit InfoPanel", () => {
  const vuetify = createVuetify()

  it("renders documentNumber if given", async () => {
    const { getAllByText } = render(DocUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("123", { documentnumber: "foo" }),
      },
      global: { plugins: [vuetify] },
    })

    getAllByText("foo")
  })

  it("renders aktenzeichen if given", async () => {
    const { getAllByText } = render(DocUnitInfoPanel, {
      props: { documentUnit: new DocumentUnit("123", { fileNumber: "foo" }) },
      global: { plugins: [vuetify] },
    })

    getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Aktenzeichen foo/)
    })
  })

  it("renders placeholder for aktenzeichen if not given", async () => {
    const { getAllByText } = render(DocUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("123", {
          decisionDate: "bar",
          courtType: "baz",
          documentnumber: "qux",
        }),
      },
      global: { plugins: [vuetify] },
    })

    getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Aktenzeichen  - /)
    })
  })

  it("renders Entscheidungsdatum if given", async () => {
    const { getAllByText } = render(DocUnitInfoPanel, {
      props: { documentUnit: new DocumentUnit("123", { decisionDate: "foo" }) },
      global: { plugins: [vuetify] },
    })

    getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Entscheidungsdatum foo/)
    })
  })

  it("renders placeholder for Entscheidungsdatum if not given", async () => {
    const { getAllByText } = render(DocUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("123", {
          fileNumber: "foo",
          courtType: "baz",
          documentnumber: "qux",
        }),
      },
      global: { plugins: [vuetify] },
    })

    getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Entscheidungsdatum  -/)
    })
  })

  it("renders Gerichtstyp if given", async () => {
    const { getAllByText } = render(DocUnitInfoPanel, {
      props: { documentUnit: new DocumentUnit("123", { courtType: "foo" }) },
      global: { plugins: [vuetify] },
    })

    getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Gerichtstyp foo/)
    })
  })

  it("renders placeholder for Gerichtstyp if not given", async () => {
    const { getAllByText } = render(DocUnitInfoPanel, {
      props: {
        documentUnit: new DocumentUnit("123", {
          fileNumber: "foo",
          decisionDate: "bar",
          documentnumber: "qux",
        }),
      },
      global: { plugins: [vuetify] },
    })

    getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Gerichtstyp  -/)
    })
  })
})
