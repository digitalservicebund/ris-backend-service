import { Decision } from "@/domain/decision"
import { DocumentationUnit } from "@/domain/documentationUnit"
import { Kind } from "@/domain/documentationUnitKind"
import PendingProceeding from "@/domain/pendingProceeding"

/**
 * Type guard to check if a document is a Decision.
 * @param doc The document to check.
 * @returns True if the DocumentationUnit is a Decision, false otherwise.
 */
export function isDecision(
  doc: DocumentationUnit | undefined | null,
): doc is Decision {
  return doc?.kind === Kind.DECISION
}

/**
 * Type guard to check if a document is a PendingProceeding.
 * @param doc The document to check.
 * @returns True if the DocumentationUnit is a PendingProceeding, false otherwise.
 */
export function isPendingProceeding(
  doc: DocumentationUnit | undefined | null,
): doc is PendingProceeding {
  return doc?.kind === Kind.PENDING_PROCEEDING
}
