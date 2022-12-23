import { InputField, InputType } from "@/domain"

export const referenceNumber: InputField[] = [
  {
    name: "referenceNumber",
    type: InputType.TEXT,
    label: "Aktenzeichen",
    inputAttributes: {
      ariaLabel: "Aktenzeichen",
    },
  },
]
