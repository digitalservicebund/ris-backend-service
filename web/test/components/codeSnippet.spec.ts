import { render } from "@testing-library/vue"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import { createVuetify } from "vuetify/lib/framework.mjs"
import CodeSnippet from "@/components/CodeSnippet.vue"

describe("CodeSnippet", () => {
  const vuetify = createVuetify({ components, directives })

  const TITLE = "foo"
  const XML =
    '<?xml version="1.0"?>\n<!DOCTYPE juris-r SYSTEM "juris-r.dtd">\n<juris-r>\n<metadaten>\n<gericht>\n<gertyp>Gerichtstyp</gertyp>\n<gerort>Gerichtssitz</gerort>\n</gericht>\n</metadaten>\n<textdaten>\n<titelzeile>\n<body>\n<div>\n<p>Titelzeile</p>\n</div>\n</body>\n</titelzeile>\n<leitsatz>\n<body>\n<div>\n<p>Leitsatz</p>\n</div>\n</body>\n</leitsatz>\n<osatz>\n<body>\n<div>\n<p>Orientierungssatz</p>\n</div>\n</body>\n</osatz>\n<tenor>\n<body>\n<div>\n<p>Tenor</p>\n</div>\n</body>\n</tenor>\n<tatbestand>\n<body>\n<div>\n<p>Tatbestand</p>\n<br/>\n</div>\n</body>\n</tatbestand>\n<entscheidungsgruende>\n<body>\n<div>\n<p>Entscheidungsgründe</p>\n</div>\n</body>\n</entscheidungsgruende>\n<gruende>\n<body>\n<div>\n<p>Gründe</p>\n</div>\n</body>\n</gruende>\n</textdaten>\n</juris-r>'
  it("renders with title and xml", () => {
    const { getAllByText, getByText } = render(CodeSnippet, {
      global: { plugins: [vuetify] },
      props: {
        title: TITLE,
        xml: XML,
      },
    })
    getByText(TITLE)
    const lines = XML.split("\n")
    lines.forEach((line, index) => {
      getAllByText(line)
      getByText(index + 1)
    })
  })

  it("renders with invalid xml", () => {
    const INVALID_XML = "Foo"
    const { getByText } = render(CodeSnippet, {
      global: { plugins: [vuetify] },
      props: {
        title: TITLE,
        xml: INVALID_XML,
      },
    })
    getByText(TITLE)
    expect(() => getByText(INVALID_XML)).toThrow()
  })
})
