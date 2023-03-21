import userEvent from "@testing-library/user-event"
import {render, screen, fireEvent} from "@testing-library/vue"
import CitationDateInput from "@/components/CitationDateInput.vue";

function renderComponent(options?: {
    ariaLabel?: string
}) {
    const user = userEvent.setup()
    const props = {
        ariaLabel: options?.ariaLabel ?? "aria-label",
    }
    const utils = render(CitationDateInput, {props})
    return {user, props, ...utils}
}

describe("Radio Buttons", () => {
    it("Shows 2 radio buttons and 1 date input element by default", () => {
        renderComponent()
        const dateRadioButton: HTMLInputElement | null = screen.queryByLabelText("Datum")
        const yearRadioButton: HTMLInputElement | null = screen.queryByLabelText("Jahresangabe")
        const dateInputField: HTMLElement | null = document.getElementById("citationDateInput")

        expect(dateRadioButton).toBeInTheDocument()
        expect(dateRadioButton).toBeVisible()
        expect(dateRadioButton).toBeChecked()

        expect(yearRadioButton).toBeInTheDocument()
        expect(yearRadioButton).toBeVisible()
        expect(yearRadioButton).not.toBeChecked()

        expect(dateInputField).toBeInTheDocument()
        expect(dateInputField).toBeVisible()

        const yearInputField: HTMLElement | null = document.getElementById("citationYearInput")
        expect(yearInputField).not.toBeInTheDocument()
    })

    it("user clicks Year radio button and renders year input element", async () => {
        renderComponent()
        const dateRadioButton: HTMLInputElement | null = screen.queryByLabelText("Datum")
        const yearRadioButton: HTMLInputElement | null = screen.queryByLabelText("Jahresangabe")
        const dateInputField: HTMLElement | null = document.getElementById("citationDateInput")

        expect(dateRadioButton).toBeInTheDocument()
        expect(dateRadioButton).toBeVisible()
        expect(yearRadioButton).toBeInTheDocument()
        expect(yearRadioButton).toBeVisible()

        if (yearRadioButton != null) {
            await fireEvent.click(yearRadioButton);
        }

        expect(yearRadioButton?.checked).toBe(true);
        expect(dateRadioButton).not.toBeChecked()

        expect(dateInputField).not.toBeInTheDocument()
        expect(dateInputField).not.toBeVisible()

        const yearInputField = document.getElementById("citationYearInput") as HTMLInputElement | null
        expect(yearInputField).toBeInTheDocument()
        expect(yearInputField).toBeVisible()

        if (yearInputField != null) {
            await fireEvent.update(yearInputField, "1234");
            expect(yearInputField.value).toBe("1234");
            expect(yearInputField.value.length).toBe(4);
        }
    })

//     My tests only touch limited functionality, and they are too large - break up the tests above into smaller ones (as examples below) -
//     Test that the year input accepts only number keys
//     Test that the year input does not accept non number keys. Assume that the input field is empty and simulate inputting non number keys. Could also maybe check value emited to parent
//     Test that the year input only accepts upto to 4 digits. Try to input 5 or 6 and only the first 4 are taken.
//     TIP = Maybe better not to use "query" search but use "get" instead.
//     Test, depending on the input value if there is one, then the correct input is shown and witch radio button is active. i.e if a date is already stored then date input is shown and the date radio button is selected
//     opposite applies to year e.g. if year then year shown and year radio selected
//     test that when we click the opposite radio button then the opposite field is set to undefined.

})

