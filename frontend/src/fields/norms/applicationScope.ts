import { InputField, InputType } from "@/shared/components/input/types"

export const applicationScope: InputField[] = [
  {
    name: "applicationScopeArea",
    id: "applicationScopeArea",
    type: InputType.TEXT,
    label: "Gebiet",
    inputAttributes: {
      ariaLabel: "Gebiet",
    },
  },
  {
    name: "applicationScopeStartDate",
    id: "applicationScopeStartDate",
    type: InputType.DATE,
    label: "Anfangsdatum",
    inputAttributes: {
      ariaLabel: "Anfangsdatum",
      isFutureDate: true,
    },
  },
  {
    name: "applicationScopeEndDate",
    id: "applicationScopeEndDate",
    type: InputType.DATE,
    label: "Endedatum",
    inputAttributes: {
      ariaLabel: "Endedatum",
      isFutureDate: true,
    },
  },
]
