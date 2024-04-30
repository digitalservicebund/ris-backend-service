import { Texts } from "@/domain/documentUnit"
import { texts as textsFields } from "@/fields/caselaw"

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

export function useValidBorderNumbers(
  texts: Texts,
  validBorderNumbers: string[],
) {
  return textsFields.map((item) => {
    const divElem = document.createElement("div")
    divElem.innerHTML = texts[item.name as keyof Texts] ?? ""
    const validatedContent = validateBorderNumberLinks(
      divElem,
      validBorderNumbers,
    ).innerHTML
    return {
      id: item.name as keyof Texts,
      name: item.name,
      label: item.label,
      aria: item.label,
      value: validatedContent,
      fieldType: item.fieldType,
      fieldSize: item.fieldSize,
    }
  })
}
