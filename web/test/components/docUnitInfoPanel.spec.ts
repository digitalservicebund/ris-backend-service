import { render } from "@testing-library/vue"
import { createVuetify } from "vuetify"
import DocUnitInfoPanel from "@/components/DocUnitInfoPanel.vue"
import DocUnit from "@/domain/docUnit"

describe("docUnit InfoPanel", () => {
  const vuetify = createVuetify()

  it("renders documentNumber if given", async () => {
    const { getAllByText } = render(DocUnitInfoPanel, {
      props: { docUnit: new DocUnit("123", { documentnumber: "foo" }) },
      global: { plugins: [vuetify] },
    })

    getAllByText("foo")
  })

  it("renders aktenzeichen if given", async () => {
    const { getAllByText } = render(DocUnitInfoPanel, {
      props: { docUnit: new DocUnit("123", { docketNumber: "foo" }) },
      global: { plugins: [vuetify] },
    })

    getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Aktenzeichen foo/)
    })
  })

  it("renders placeholder for aktenzeichen if not given", async () => {
    const { getAllByText } = render(DocUnitInfoPanel, {
      props: {
        docUnit: new DocUnit("123", {
          entscheidungsdatum: "bar",
          gerichtstyp: "baz",
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
      props: { docUnit: new DocUnit("123", { entscheidungsdatum: "foo" }) },
      global: { plugins: [vuetify] },
    })

    getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Entscheidungsdatum foo/)
    })
  })

  it("renders placeholder for Entscheidungsdatum if not given", async () => {
    const { getAllByText } = render(DocUnitInfoPanel, {
      props: {
        docUnit: new DocUnit("123", {
          docketNumber: "foo",
          gerichtstyp: "baz",
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
      props: { docUnit: new DocUnit("123", { gerichtstyp: "foo" }) },
      global: { plugins: [vuetify] },
    })

    getAllByText((_content, node) => {
      return !!node?.textContent?.match(/Gerichtstyp foo/)
    })
  })

  it("renders placeholder for Gerichtstyp if not given", async () => {
    const { getAllByText } = render(DocUnitInfoPanel, {
      props: {
        docUnit: new DocUnit("123", {
          docketNumber: "foo",
          entscheidungsdatum: "bar",
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
