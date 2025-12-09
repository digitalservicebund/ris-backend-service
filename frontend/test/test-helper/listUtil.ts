import { userEvent } from "@testing-library/user-event"
import { screen } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"

export const clickOrderedListSubButton = async (subButtonLabel: string) => {
  const orderedListMenu = screen.getByLabelText("Nummerierte Liste")
  await userEvent.click(orderedListMenu)
  const subButton = screen.getByLabelText(subButtonLabel)
  await userEvent.click(subButton)
  await flushPromises()
}
