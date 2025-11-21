import { userEvent } from "@testing-library/user-event"
import { screen } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"

export const clickTableSubButton = async (subButtonLabel: string) => {
  const tableBorderMenu = screen.getByLabelText("Tabellenrahmen")
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
