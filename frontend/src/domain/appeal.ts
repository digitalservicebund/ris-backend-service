import { DropdownItem } from "@/components/input/types"

export type Appeal = {
  id?: string
  appellants?: Appellant[]
  revisionDefendantStatuses?: AppealStatus[]
  revisionPlaintiffStatuses?: AppealStatus[]
  jointRevisionDefendantStatuses?: AppealStatus[]
  jointRevisionPlaintiffStatuses?: AppealStatus[]
  nzbDefendantStatuses?: AppealStatus[]
  nzbPlaintiffStatuses?: AppealStatus[]
  appealWithdrawal?: AppealWithdrawal
  pkhPlaintiff?: PkhPlaintiff
}

export type Appellant = {
  id: string
  value: string
}

export type AppealStatus = {
  id: string
  value: string
}

export enum AppealWithdrawal {
  JA = "JA",
  NEIN = "NEIN",
  KEINE_ANGABE = "KEINE_ANGABE",
}
export const appealWithdrawalItems: DropdownItem[] = [
  {
    label: "Ja",
    value: AppealWithdrawal.JA,
  },
  {
    label: "Nein",
    value: AppealWithdrawal.NEIN,
  },
  {
    label: "Keine Angabe",
    value: AppealWithdrawal.KEINE_ANGABE,
  },
]
export enum PkhPlaintiff {
  JA = "JA",
  NEIN = "NEIN",
  KEINE_ANGABE = "KEINE_ANGABE",
}
export const pkhPlaintiffItems: DropdownItem[] = [
  {
    label: "Ja",
    value: PkhPlaintiff.JA,
  },
  {
    label: "Nein",
    value: PkhPlaintiff.NEIN,
  },
  {
    label: "Keine Angabe",
    value: PkhPlaintiff.KEINE_ANGABE,
  },
]
