import { ShortTexts, LongTexts } from "@/domain/documentUnit"
import { shortTextFields, longTextFields } from "@/fields/caselaw"

const validateBorderNumberLinks = (
  divElem: HTMLDivElement,
  validBorderNumbers: string[],
) => {
  const linkTags = Array.from(
    divElem.getElementsByTagName("border-number-link"),
  )

  linkTags.forEach((linkTag) => {
    const borderNumber = linkTag.getAttribute("nr")
    const hasBorderNumber = borderNumber
      ? validBorderNumbers.includes(borderNumber)
      : false
    linkTag.setAttribute("valid", hasBorderNumber.toString())
  })
  return divElem
}

export function useShortTextsWithValidBorderNumberLinks(
  texts: ShortTexts,
  validBorderNumbers: string[],
) {
  return shortTextFields.map((item) => {
    const divElem = document.createElement("div")
    divElem.innerHTML = texts[item.name as keyof ShortTexts] ?? ""
    const validatedContent = validateBorderNumberLinks(
      divElem,
      validBorderNumbers,
    ).innerHTML
    return {
      id: item.name as keyof ShortTexts,
      name: item.name,
      label: item.label,
      aria: item.label,
      value: validatedContent,
      fieldType: item.fieldType,
      fieldSize: item.fieldSize,
    }
  })
}

export function useLongTexts(texts: LongTexts) {
  return longTextFields.map((item) => {
    return {
      id: item.name as keyof LongTexts,
      name: item.name,
      label: item.label,
      aria: item.label,
      value: texts[item.name as keyof LongTexts] ?? "",
      fieldType: item.fieldType,
      fieldSize: item.fieldSize,
    }
  })
}
