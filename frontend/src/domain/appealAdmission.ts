import { AppealAdmitter } from "@/domain/appealAdmitter"

export type AppealAdmission =
  | {
      admitted: false
    }
  | {
      admitted: true
      by?: AppealAdmitter
    }
