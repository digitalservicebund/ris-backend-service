import fs from "fs"
import { tmpdir } from "os"
import path from "path"
import internal from "stream"
import { APIRequestContext, expect } from "@playwright/test"
import jsZip from "jszip"
import { Page } from "playwright"
import {
  AnyField,
  FieldType,
  FieldValue,
  FootnoteInputType,
  MetadataInputSection,
} from "~/e2e/norms/utilities"

const VERSION_TAG = "v0.15.0"
const REMOTE_JURIS_TEST_FILE_FOLDER_URL = `raw.githubusercontent.com/digitalservicebund/ris-norms-juris-converter/${VERSION_TAG}/src/test/resources/juris`

async function getLocalJurisTestFileFolderPath(): Promise<string> {
  const folderPath = path.join(
    tmpdir(),
    "ris-norms_juris-test-files",
    VERSION_TAG,
  )
  await fs.promises.mkdir(folderPath, { recursive: true })
  return folderPath
}

async function downloadJurisTestFile(
  request: APIRequestContext,
  fileName: string,
  localPath: string,
): Promise<void> {
  const username = process.env.GH_PACKAGES_REPOSITORY_USER
  const password = process.env.GH_PACKAGES_REPOSITORY_TOKEN
  const remoteUrl = `https://${username}:${password}@${REMOTE_JURIS_TEST_FILE_FOLDER_URL}/${fileName}`
  const response = await request.get(remoteUrl)

  if (!response.ok()) {
    console.error(`Download of the following Juris file failed: ${fileName}`, {
      status: response.status(),
      text: await response.text(),
    })
  }

  await expect(response).toBeOK()

  const body = await response.body()
  await fs.promises.writeFile(localPath, body)
}

export async function loadJurisTestFile(
  request: APIRequestContext,
  fileName: string,
): Promise<{ filePath: string; fileContent: Buffer }> {
  const folderPath = await getLocalJurisTestFileFolderPath()
  const filePath = path.join(folderPath, fileName)

  if (!fs.existsSync(filePath)) {
    await downloadJurisTestFile(request, fileName, filePath)
  }

  const fileContent = await fs.promises.readFile(filePath)
  return { filePath, fileContent }
}

export async function importNormViaApi(
  request: APIRequestContext,
  fileContent: Buffer,
  fileName: string,
): Promise<{ guid: string }> {
  const response = await request.post(`/api/v1/norms`, {
    headers: { "Content-Type": "application/zip", "X-Filename": fileName },
    data: fileContent,
  })

  await expect(response).toBeOK()

  const body = await response.text()
  return JSON.parse(body)
}

export async function importTestData(
  request: APIRequestContext,
  norm: object,
): Promise<{ guid: string }> {
  const response = await request.post(`/api/v1/norms/test-data`, {
    headers: { "Content-Type": "application/json" },
    data: norm,
  })

  await expect(response).toBeOK()

  const body = await response.text()
  return JSON.parse(body)
}

export const openNorm = async (page: Page, guid: string) => {
  await page.goto(`/norms/norm/${guid}`)
}

export async function getDownloadedFileContent(page: Page, filename: string) {
  const [download] = await Promise.all([
    page.waitForEvent("download"),
    page.locator('a:has-text("Zip Datei speichern"):not(:disabled)').click(),
  ])

  expect(download.suggestedFilename()).toBe(filename)
  expect(
    (await fs.promises.stat((await download.path()) as string)).size,
  ).toBeGreaterThan(0)
  const readable = await download.createReadStream()
  const chunks = []
  for await (const chunk of readable as internal.Readable) {
    chunks.push(chunk)
  }

  return Buffer.concat(chunks)
}

export async function getMetaDataFileAsString(
  content: Buffer,
): Promise<string> {
  return jsZip.loadAsync(content).then(function (zip) {
    const metadataFileName = Object.keys(zip.files)
      .filter(
        (filename) => filename.endsWith(".xml") && !filename.includes("BJNE"),
      )
      .pop()
    return zip.files[metadataFileName as string]
      .async("string")
      .then((xmlContent) => xmlContent.replace(/ {2}|\r\n|\n|\r/gm, ""))
  })
}

export async function expectFootnoteSummaryToContainMetadata(
  page: Page,
  section: MetadataInputSection,
): Promise<void> {
  const parentDiv = page.locator(`#${section.id}`)
  const liElements = parentDiv.locator("li")
  for (let i = 0; i < (await liElements.count()); i++) {
    const referenceAndFootnotes = liElements.nth(i).locator("div > span")
    const referenceAndFootnotesCount = await referenceAndFootnotes.count()

    const summaryValues = []
    for (let j = 0; j < referenceAndFootnotesCount; j++) {
      const content = (await referenceAndFootnotes.nth(j).innerText()).trim()
      summaryValues.push(content)
    }

    expect(referenceAndFootnotesCount).toBe(summaryValues.length)
    for (let x = 0; x < referenceAndFootnotesCount; x++) {
      const footnotes = section.fields?.[0].values?.[i] as (
        | FootnoteInputType
        | undefined
      )[]
      const optionalLabelAndContent =
        (footnotes[x]?.label.length ?? 0) > 0
          ? footnotes[x]?.label + "\n\n" + footnotes[x]?.content
          : footnotes[x]?.content
      expect(summaryValues).toContain(optionalLabelAndContent)
    }
  }
}

export async function expectSummaryToContainMetadata(
  page: Page,
  section: MetadataInputSection,
): Promise<void> {
  if (section.heading === "Fußnoten") {
    return await expectFootnoteSummaryToContainMetadata(page, section)
  }
  const parentDiv = page.locator(`#${section.id}`)
  const liElements = parentDiv.locator("li")

  for (let i = 0; i < (await liElements.count()); i++) {
    let childs = liElements.nth(i).locator("div > div")
    if ((await childs.count()) === 0) {
      childs = liElements.nth(i).locator("span")
    }

    const allvalues = section.fields?.map((field) => field?.values?.[i] ?? null)

    const expectedFieldList: [AnyField | undefined, FieldValue][] = []
    for (let k = 0; k < (allvalues ?? []).length; k++) {
      const field = section.fields?.[k]
      const expectedValue: FieldValue | null = allvalues?.[k] ?? null
      if (
        field?.type === FieldType.RADIO ||
        (field?.type === FieldType.CHECKBOX && expectedValue === false)
      ) {
        continue
      }
      if (expectedValue !== null) {
        if (field?.type === FieldType.CHIPS && Array.isArray(expectedValue)) {
          for (const value of expectedValue) {
            expectedFieldList.push([field, value as string])
          }
        } else {
          expectedFieldList.push([field, expectedValue])
        }
      }
    }

    const summaryValues = []
    for (let j = 0; j < (await childs.count()); j++) {
      const content = (await childs.nth(j).innerText()).trim()
      if (content !== "|") {
        if (
          section.heading === "Amtliche Fundstelle" &&
          content.endsWith(",")
        ) {
          summaryValues.push(content.slice(0, -1))
        } else if (content !== "Amtsblatt der EU") {
          summaryValues.push(content)
        }
      }
    }

    expect(expectedFieldList.length).toBe(summaryValues.length)
    for (let i = 0; i < summaryValues.length; i++) {
      const field = expectedFieldList[i][0]
      const fieldValue = expectedFieldList[i][1]
      let value
      switch (field?.type) {
        case FieldType.CHECKBOX:
          if (field.label === "Beschlussfassung mit qualifizierter Mehrheit") {
            value = "Beschlussfassung mit qual. Mehrheit"
          } else {
            value = field.label
          }
          break
        case FieldType.DROPDOWN:
          switch (fieldValue) {
            case "UNDEFINED_FUTURE":
              value = "unbestimmt (zukünftig)"
              break
            case "UNDEFINED_UNKNOWN":
              value = "unbestimmt (unbekannt)"
              break
            case "UNDEFINED_NOT_PRESENT":
              value = "nicht vorhanden"
          }
          break
        case FieldType.CHIPS:
          if (summaryValues[i].endsWith(",")) {
            summaryValues[i] = summaryValues[i].slice(0, -1)
          }
          value = fieldValue
          break
        default:
          value = fieldValue
      }
      if (section.heading === "Sachgebiet") {
        value = field?.label + " " + value
      }
      expect(summaryValues[i]).toBe(value)
    }
  }
}

export async function saveNormFrame(page: Page) {
  await page
    .locator("[aria-label='Rahmendaten Speichern Button']:not(:disabled)")
    .click()
  await expect(
    page.locator("[aria-label='Rahmendaten Speichern Button']:not(:disabled)"),
  ).toBeVisible()
}
