import { expect } from "@playwright/test"
import jsdom from "jsdom"
import { openNorm } from "./e2e-utils"
import { testWithImportedNorm } from "./fixtures"
import { normData as norm } from "./testdata/norm_basic"
import { FieldType, fillInputField } from "./utilities"
import { MetadataSectionName, MetadatumType } from "@/domain/Norm"

testWithImportedNorm(
  "Check if XML can be retrieved by ELI and content is correct",
  async ({ page, normData, guid, request }) => {
    // Open frame data
    await openNorm(
      page,
      normData.metadataSections?.NORM?.[0]?.OFFICIAL_LONG_TITLE?.[0] ?? "",
      guid
    )
    await page.locator("a:has-text('Rahmen')").click()

    // Update page of print announcement so that this norm we be retrieved for sure by the eli
    const officialReferencesExpandable = page.locator("#officialReferences")
    await officialReferencesExpandable.click()
    const listEntries =
      officialReferencesExpandable.getByLabel("Listen Eintrag")
    listEntries
      .nth(0)
      .getByRole("button", { name: "Eintrag bearbeiten" })
      .click()
    const newRandomGazette = Math.random().toString(36).slice(2, 7)
    await fillInputField(
      page,
      FieldType.TEXT,
      "printAnnouncementGazette",
      newRandomGazette
    )
    const finishButton = officialReferencesExpandable.getByRole("button", {
      name: "Fertig",
    })
    await finishButton.click()
    await page.locator("[aria-label='Rahmendaten Speichern Button']").click()
    await expect(
      page.locator("text=Zuletzt gespeichert um").first()
    ).toBeVisible()
    await page.reload()

    // retrieve by new eli
    const eliInputValue = await page.inputValue("input#eli")
    await page.goto(`/api/v1/norms/xml/${eliInputValue}`)

    const response = await request.get(`/api/v1/norms/xml/${eliInputValue}`)
    expect(response.ok()).toBeTruthy()
    expect(response.headers()["content-type"]).toBe("application/xml")

    const xmlAsString = await response.text()
    const xmlDOM = new jsdom.JSDOM(xmlAsString, { contentType: "text/html" })

    xmlDOM.window.document
      .querySelectorAll("akn\\:FRBRthis")
      .forEach((frbrThis) => {
        // eslint-disable-next-line jest-dom/prefer-to-have-attribute
        expect(frbrThis.getAttribute("value")).toMatch(
          new RegExp(`^${eliInputValue}`)
        )
      })

    xmlDOM.window.document
      .querySelectorAll("akn\\:FRBRdate")
      .forEach((frbrDate) => {
        // Our fixtures contain dates in the format a user would use (ie. DD.MM.YYYY),
        // so we need to convert them back into the format required by the XML.
        let announcementDate = ""
        if (norm.metadataSections?.ANNOUNCEMENT_DATE?.[0]?.DATE?.[0]) {
          const [day, month, year] =
            norm.metadataSections?.ANNOUNCEMENT_DATE?.[0]?.DATE?.[0].split(".")
          announcementDate = `${year}-${month}-${day}`
        }

        // eslint-disable-next-line jest-dom/prefer-to-have-attribute
        expect(frbrDate.getAttribute("date")).toBe(announcementDate)
      })

    // eslint-disable-next-line jest-dom/prefer-to-have-attribute
    expect(
      xmlDOM.window.document
        .querySelector("akn\\:FRBRnumber")
        ?.getAttribute("value")
    ).toBe(
      `s${norm.metadataSections?.OFFICIAL_REFERENCE?.[0]?.PRINT_ANNOUNCEMENT?.[0]?.PAGE?.[0]}`
    )

    // eslint-disable-next-line jest-dom/prefer-to-have-attribute
    expect(
      xmlDOM.window.document
        .querySelector("akn\\:FRBRname")
        ?.getAttribute("value")
    ).toBe(newRandomGazette)

    const proprietary =
      xmlDOM.window.document.querySelector("akn\\:proprietary")
    expect(proprietary?.querySelector("meta\\:typ")?.textContent?.trim()).toBe(
      "verordnung"
    )
    expect(proprietary?.querySelector("meta\\:form")?.textContent?.trim()).toBe(
      "stammform"
    )
    expect(
      proprietary?.querySelector("meta\\:fassung")?.textContent?.trim()
    ).toBe("verkuendungsfassung")
    expect(proprietary?.querySelector("meta\\:art")?.textContent?.trim()).toBe(
      "rechtsetzungsdokument"
    )
    expect(
      proprietary?.querySelector("meta\\:initiant")?.textContent?.trim()
    ).toBe("bundestag")
    expect(
      proprietary
        ?.querySelector("meta\\:bearbeitendeInstitution")
        ?.textContent?.trim()
    ).toBe("bundesrat")

    expect(
      xmlDOM.window.document
        .querySelector("akn\\:docTitle")
        ?.textContent?.trim()
    ).toBe(
      norm.metadataSections?.[MetadataSectionName.NORM]?.[0]?.[
        MetadatumType.OFFICIAL_LONG_TITLE
      ]?.[0]
    )
    expect(
      xmlDOM.window.document
        ?.querySelector("akn\\:shortTitle")
        ?.textContent?.trim()
    ).toBe(
      norm.metadataSections?.[MetadataSectionName.NORM]?.[0]?.[
        MetadatumType.OFFICIAL_SHORT_TITLE
      ]?.[0]
    )

    xmlDOM.window.document
      .querySelectorAll("akn\\:article")
      .forEach((article, articleIndex) => {
        expect(article.querySelector("akn\\:marker")?.textContent?.trim()).toBe(
          norm.articles[articleIndex].marker
        )
        expect(
          article.querySelector("akn\\:heading")?.textContent?.trim()
        ).toBe(norm.articles[articleIndex].title)

        article
          .querySelectorAll("akn\\:paragraph")
          .forEach((paragraph, paragraphIndex) => {
            if (
              norm.articles[articleIndex].paragraphs[paragraphIndex].marker !==
              undefined
            ) {
              expect(
                paragraph.querySelector("akn\\:marker")?.textContent?.trim()
              ).toBe(
                norm.articles[articleIndex].paragraphs[paragraphIndex].marker
              )
            }
            expect(
              paragraph
                .querySelector("akn\\:p")
                ?.textContent?.trim()
                .replace(/\n/, "")
            ).toBe(norm.articles[articleIndex].paragraphs[paragraphIndex].text)
          })
      })
  }
)
