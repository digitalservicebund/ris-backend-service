import { storeToRefs } from "pinia"
import DocumentUnit, { LongTexts, ShortTexts } from "@/domain/documentUnit"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

type ShortTextKeys = keyof ShortTexts
type LongTextKeys = keyof Omit<LongTexts, "participatingJudges">
const orderedCategoriesWithBorderNumbers: LongTextKeys[] = [
  "reasons",
  "caseFacts",
  "decisionReasons",
  "otherLongText",
]

/**
 * Will take all border numbers in the long text and renumber them sequentially starting from the given number.
 *
 * @param longText text for which the border numbers will be updated
 * @param nextBorderNumber The first border number in this text will start with this number
 */
function makeBorderNumbersSequentialForCategory(
  longText: string,
  nextBorderNumber: number,
) {
  const updatedBorderNumbers = new Map<string, number>()
  const textXml = new DOMParser().parseFromString(longText, "text/html")
  const borderNumbers = textXml.getElementsByTagName("border-number")
  for (const borderNumber of borderNumbers) {
    const [numberElement] = <HTMLCollectionOf<HTMLElement>>(
      borderNumber.getElementsByTagName("number")
    )
    // innerText is not available in JSDom
    const previousLinkTarget = (numberElement.innerHTML ?? "").trim()
    numberElement.innerHTML = `${nextBorderNumber}`
    updatedBorderNumbers.set(previousLinkTarget, nextBorderNumber)
    nextBorderNumber++
  }
  const updatedText = textXml.body.innerHTML
  return { nextBorderNumber, updatedText, updatedBorderNumbers }
}

/**
 * Update all links in given text according to the updated border numbers.
 * @param text short- or longtext to be updated
 * @param updatedBorderNumbers border numbers that have been changed from (key) -> to (value)
 */
function updateBorderNumberLinksForText(
  text: string,
  updatedBorderNumbers: Map<string, number>,
) {
  const textXml = new DOMParser().parseFromString(text, "text/html")
  const borderNumberLinks = textXml.getElementsByTagName(
    "border-number-link",
  ) as HTMLCollectionOf<HTMLElement>

  for (const link of borderNumberLinks) {
    const linkNumber = link.getAttribute("nr")
    if (linkNumber && updatedBorderNumbers.has(linkNumber)) {
      const newLinkNumber = `${updatedBorderNumbers.get(linkNumber)}`
      link.textContent = newLinkNumber
      link.setAttribute("nr", newLinkNumber)
    }
  }

  return textXml.body.innerHTML
}

/**
 * Iterates over all short and long texts to update the border number links.
 * @param documentUnit of which border numbers were already updated
 * @param updatedBorderNumbers border numbers that have been changed from (key) -> to (value)
 */
function updateBorderNumberLinks(
  documentUnit: DocumentUnit,
  updatedBorderNumbers: Map<string, number>,
) {
  const shortTextNames = Object.keys(documentUnit.shortTexts) as ShortTextKeys[]
  for (const shortTextName of shortTextNames) {
    const shortText = documentUnit.shortTexts[shortTextName]
    if (shortText) {
      documentUnit.shortTexts[shortTextName] = updateBorderNumberLinksForText(
        shortText,
        updatedBorderNumbers,
      )
    }
  }

  const longTextNames = Object.keys(documentUnit.longTexts).filter(
    (key) => key !== "participatingJudges",
  ) as LongTextKeys[]
  for (const longTextName of longTextNames) {
    const longText = documentUnit.longTexts[longTextName]
    if (longText) {
      documentUnit.longTexts[longTextName] = updateBorderNumberLinksForText(
        longText,
        updatedBorderNumbers,
      )
    }
  }
}

const borderNumberService = {
  /**
   * Updates the border numbers of all long texts with border numbers see {@linkcode orderedCategoriesWithBorderNumbers}
   * and makes them sequential.
   *
   * Updates all existing border number links that reference a border number that was updated previously.
   */
  makeBorderNumbersSequential: () => {
    const { documentUnit } = storeToRefs(useDocumentUnitStore())
    let nextBorderNumberCount = 1
    let allUpdatedBorderNumbers = new Map<string, number>()
    for (const category of orderedCategoriesWithBorderNumbers) {
      const longText = documentUnit.value!.longTexts[category]
      if (longText) {
        const { nextBorderNumber, updatedText, updatedBorderNumbers } =
          makeBorderNumbersSequentialForCategory(
            longText,
            nextBorderNumberCount,
          )
        nextBorderNumberCount = nextBorderNumber
        documentUnit.value!.longTexts[category] = updatedText
        allUpdatedBorderNumbers = new Map([
          ...allUpdatedBorderNumbers,
          ...updatedBorderNumbers,
        ])
      }
    }

    if (allUpdatedBorderNumbers.size) {
      updateBorderNumberLinks(
        documentUnit.value as DocumentUnit,
        allUpdatedBorderNumbers,
      )
    }

    // Dissenting opinion should start from 0 again and not influence any other long-texts or links.
    if (documentUnit.value!.longTexts.dissentingOpinion) {
      const { updatedText } = makeBorderNumbersSequentialForCategory(
        documentUnit.value!.longTexts.dissentingOpinion,
        1,
      )
      documentUnit.value!.longTexts.dissentingOpinion = updatedText
    }
  },
}

export default borderNumberService
