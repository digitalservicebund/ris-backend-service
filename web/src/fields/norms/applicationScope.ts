import { InputField, InputType } from "@/domain"

export const applicationScope: InputField[] = [
  {
    name: "applicationScopeArea",
    type: InputType.TEXT,
    label: "Gebiet",
    inputAttributes: {
      ariaLabel: "Gebiet",
    },
  },
  {
    name: "applicationScopeStartDate",
    type: InputType.DATE,
    label: "Anfangsdatum",
    inputAttributes: {
      ariaLabel: "Anfangsdatum",
      isFutureDate: true,
    },
  },
  {
    name: "applicationScopeEndDate",
    type: InputType.DATE,
    label: "Endedatum",
    inputAttributes: {
      ariaLabel: "Endedatum",
      isFutureDate: true,
    },
  },
]
