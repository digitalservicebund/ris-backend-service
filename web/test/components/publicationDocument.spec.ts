import { fireEvent, render, screen } from "@testing-library/vue"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import { createVuetify } from "vuetify/lib/framework.mjs"
import PublicationDocument from "@/components/PublicationDocument.vue"

describe("PublicationDocument", () => {
  const vuetify = createVuetify({ components, directives })

  const RECEIVER_EMAIL = "dokmbx@juris.de"
  const EMAIL_SUBJECT = 'id=OVGNW name="knorr" da=r dt=b df=r'
  const LAST_PUBLICATION_DATE = "24.07.2022 16:53 Uhr"
  const ISSUES = ["Aktenzeichen", "Entscheidungsname", "Gericht"]
  const IS_FIRST_TIME_PUBLICATION = true
  const HAS_ERROR = true
  const XML =
    '<?xml version="1.0"?>\n<!DOCTYPE juris-r SYSTEM "juris-r.dtd">\n<juris-r>\n<metadaten>\n<gericht>\n<gertyp>Gerichtstyp</gertyp>\n<gerort>Gerichtssitz</gerort>\n</gericht>\n</metadaten>\n<textdaten>\n<titelzeile>\n<body>\n<div>\n<p>Titelzeile</p>\n</div>\n</body>\n</titelzeile>\n<leitsatz>\n<body>\n<div>\n<p>Leitsatz</p>\n</div>\n</body>\n</leitsatz>\n<osatz>\n<body>\n<div>\n<p>Orientierungssatz</p>\n</div>\n</body>\n</osatz>\n<tenor>\n<body>\n<div>\n<p>Tenor</p>\n</div>\n</body>\n</tenor>\n<tatbestand>\n<body>\n<div>\n<p>Tatbestand</p>\n<br/>\n</div>\n</body>\n</tatbestand>\n<entscheidungsgruende>\n<body>\n<div>\n<p>Entscheidungsgründe</p>\n</div>\n</body>\n</entscheidungsgruende>\n<gruende>\n<body>\n<div>\n<p>Gründe</p>\n</div>\n</body>\n</gruende>\n</textdaten>\n</juris-r>'

  it("renders with published XML and has error", async () => {
    const { getAllByText, getByText } = render(PublicationDocument, {
      global: { plugins: [vuetify] },
      props: {
        issues: ISSUES,
        receiverEmail: RECEIVER_EMAIL,
        emailSubject: EMAIL_SUBJECT,
        lastPublicationDate: LAST_PUBLICATION_DATE,
        xml: XML,
        isFirstTimePublication: !IS_FIRST_TIME_PUBLICATION,
        hasValidationError: HAS_ERROR,
      },
    })

    const lines = XML.split("\n")
    lines.forEach((line, index) => {
      getAllByText(line)
      getByText(index + 1)
    })
    getByText(RECEIVER_EMAIL)
    getByText(EMAIL_SUBJECT)
    getByText(`${ISSUES.length} Pflichtfelder nicht befüllt`)
    getByText("Leider ist ein Fehler aufgetreten.")
    getByText("Die Dokumentationseinheit kann nicht veröffentlich werden.")
    getByText(`Letzte Veröffenlichung am ${LAST_PUBLICATION_DATE}`)
    const buttons = screen.getAllByRole("button")
    const showErrorDetails = buttons.find(
      (button) =>
        !button.outerHTML.includes("Dokumentationseinheit veröffenlichen")
    )
    const publishButton = buttons.find((button) =>
      button.outerHTML.includes("Dokumentationseinheit veröffenlichen")
    )
    expect(showErrorDetails).toBeTruthy()
    expect(publishButton).toBeTruthy()
    // expect((publishButton as HTMLElement).hasAttribute("disabled")).toBeTruthy()
    // if (showErrorDetails != null) {
    //   await fireEvent.click(showErrorDetails)
    //   ISSUES.forEach((issue) => {
    //     getByText(issue)
    //   })
    // }
  })

  it("renders without published XML validation errors", async () => {
    const { getByText, getAllByText, emitted } = render(PublicationDocument, {
      global: { plugins: [vuetify] },
      props: {
        issues: ["succeed"],
        receiverEmail: RECEIVER_EMAIL,
        emailSubject: EMAIL_SUBJECT,
        lastPublicationDate: LAST_PUBLICATION_DATE,
        xml: XML,
        isFirstTimePublication: !IS_FIRST_TIME_PUBLICATION,
        hasValidationError: !HAS_ERROR,
      },
    })
    const lines = XML.split("\n")
    lines.forEach((line, index) => {
      getAllByText(line)
      getByText(index + 1)
    })
    getByText(RECEIVER_EMAIL)
    getByText(EMAIL_SUBJECT)
    getByText("0 Fehler")
    getByText(`Letzte Veröffenlichung am ${LAST_PUBLICATION_DATE}`)
    const buttons = screen.getAllByRole("button")
    const publishButton = buttons.find((button) =>
      button.outerHTML.includes("Dokumentationseinheit veröffenlichen")
    )
    expect(publishButton).toBeTruthy()
    if (publishButton) {
      expect(
        (publishButton as HTMLElement).hasAttribute("disabled")
      ).toBeFalsy()
      await fireEvent.click(publishButton)
      expect(emitted().publishADocument).toBeTruthy()
    }
  })

  it("renders with not yet published document", async () => {
    const { getByText, emitted } = render(PublicationDocument, {
      global: { plugins: [vuetify] },
      props: {
        issues: ISSUES,
        receiverEmail: RECEIVER_EMAIL,
        emailSubject: EMAIL_SUBJECT,
        lastPublicationDate: "",
        xml: XML,
        isFirstTimePublication: IS_FIRST_TIME_PUBLICATION,
        hasValidationError: !HAS_ERROR,
      },
    })
    const labelInfo = document.querySelectorAll(".text-icon p")[1]
    expect(labelInfo.textContent?.trim()).toEqual(
      "Durch Klick auf Veröffentlichen wird die Plausibilitätsprüfung ausgelöst."
    )
    expect(labelInfo).toBeTruthy()
    getByText("Diese Dokumentationseinheit wurde bisher nicht veröffentlicht")
    const buttons = screen.getAllByRole("button")
    const publishButton = buttons.find((button) =>
      button.outerHTML.includes("Dokumentationseinheit veröffenlichen")
    )
    expect(publishButton).toBeTruthy()
    if (publishButton) {
      expect(
        (publishButton as HTMLElement).hasAttribute("disabled")
      ).toBeFalsy()
      await fireEvent.click(publishButton)
      expect(emitted().publishADocument).toBeTruthy()
    }
  })
})
