import { userEvent } from "@testing-library/user-event"
import { screen } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"

export const clickTableBorderSubButton = async (subButtonLabel: string) => {
  const tableBorderMenu = screen.getByLabelText("Tabellenrahmen")
  await userEvent.click(tableBorderMenu)
  const subButton = screen.getByLabelText(subButtonLabel)
  await userEvent.click(subButton)
}

export const clickTableCellAlignmentSubButton = async (
  subButtonLabel: string,
) => {
  const tableBorderMenu = screen.getByLabelText(
    "Vertikale Ausrichtung in Tabellen",
  )
  await userEvent.click(tableBorderMenu)
  const subButton = screen.getByLabelText(subButtonLabel)
  await userEvent.click(subButton)
}

export const insertTable = async () => {
  const tableMenu = screen.getByLabelText("Tabelle", { exact: true })
  await userEvent.click(tableMenu)

  const insertButton = screen.getByLabelText("Tabelle einfügen")
  await userEvent.click(insertButton)
  await flushPromises()
}

export const getFirstCellHTML = () => {
  const editorContent = screen.getByTestId("Gründe")
  const firstCell =
    editorContent.querySelector("th") || editorContent.querySelector("td")
  if (!firstCell) throw new Error("No table cell found in the document.")

  return firstCell.getAttribute("style") || ""
}

export const getOrderedListHTML = () => {
  const editorContent = screen.getByTestId("Gründe")
  const orderedList = editorContent.querySelector("ol")
  if (!orderedList) throw new Error("No ordered list found in the document.")

  return orderedList.getAttribute("style") || ""
}

export const getOrderedListType = () => {
  const editorContent = screen.getByTestId("Gründe")
  const orderedList = editorContent.querySelector("ol")
  if (!orderedList) throw new Error("No ordered list found in the document.")

  return orderedList.getAttribute("type") || ""
}

export const hasOrderedList = () => {
  const editorContent = screen.getByTestId("Gründe")
  return !!editorContent.querySelector("ol")
}
