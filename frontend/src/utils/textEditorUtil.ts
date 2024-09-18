export default class TextEditorUtil {
  public static getEditorContentIfPresent(
    value: string | undefined,
  ): string | undefined {
    if (value == undefined || value.trim().length > 0) return undefined
    const divElem = document.createElement("div")
    divElem.innerHTML = value
    const hasImgElem = divElem.getElementsByTagName("img").length > 0
    const hasTable = divElem.getElementsByTagName("table").length > 0
    const hasInnerText = divElem.innerText.trim().length > 0
    return hasInnerText || hasImgElem || hasTable ? value : undefined
  }
}
