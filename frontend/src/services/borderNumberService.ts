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

function makeBorderNumbersSequentialForCategory(
  longText: string,
  nextBorderNumber: number,
) {
  const updatedBorderNumbers = new Map<string, number>()
  const textXml = new DOMParser().parseFromString(longText, "text/html")
  const borderNumbers = textXml.getElementsByTagName("border-number")
  for (const borderNumber of borderNumbers) {
    const [number] = borderNumber.getElementsByTagName(
      "number",
    ) as HTMLCollectionOf<HTMLElement>
    // innerText is not available in JSDom
    const previousValue = (number.innerHTML ?? "").trim()
    number.innerHTML = `${nextBorderNumber}`
    updatedBorderNumbers.set(previousValue, nextBorderNumber)
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
  makeBorderNumbersSequential: () => {
    performance.mark("border-number-start")
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

    performance.mark("border-number-links-start")

    updateBorderNumberLinks(
      documentUnit.value as DocumentUnit,
      allUpdatedBorderNumbers,
    )

    if (documentUnit.value!.longTexts.dissentingOpinion) {
      const { updatedText } = makeBorderNumbersSequentialForCategory(
        documentUnit.value!.longTexts.dissentingOpinion,
        1,
      )
      documentUnit.value!.longTexts.dissentingOpinion = updatedText
    }
    performance.mark("border-number-finished")
    // console.log(
    //   performance.measure(
    //     "calculate border numbers",
    //     "border-number-start",
    //     "border-number-links-start",
    //   ),
    // )
    // console.log(
    //   performance.measure(
    //     "calculate border links",
    //     "border-number-links-start",
    //     "border-number-finished",
    //   ),
    // )
  },
}

export default borderNumberService
