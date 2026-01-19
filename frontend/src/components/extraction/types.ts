interface CharInterval {
  startPos: number
  endPos: number
}

export interface Extraction {
  id: string // UUID string
  extractionClass: string
  extractionText: string
  charInterval: CharInterval | null
  attributes: Record<string, unknown> | null
  isSection?: boolean
  targetPath?: string
  normalizedText?: string
}
