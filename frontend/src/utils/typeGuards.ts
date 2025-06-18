import { Decision } from "@/domain/decision"
import { Kind } from "@/domain/documentUnit"
import PendingProceeding from "@/domain/pendingProceeding"

/**
 * Type guard to check if a document is a DocumentUnit.
 * @param doc The document to check.
 * @returns True if the object is a DocumentUnit, false otherwise.
 */
export function isDocumentUnit(
  doc: Decision | PendingProceeding | undefined | null,
): doc is Decision {
  return doc?.kind === Kind.DOCUMENTION_UNIT
}

/**
 * Type guard to check if a document is a PendingProceeding.
 * @param doc The document to check.
 * @returns True if the object is a PendingProceeding, false otherwise.
 */
export function isPendingProceeding(
  doc: Decision | PendingProceeding | undefined | null,
): doc is PendingProceeding {
  return doc?.kind === Kind.PENDING_PROCEEDING
}
