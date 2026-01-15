interface CharInterval {
  start_pos: number
  end_pos: number
}

export interface Extraction {
  id: string // UUID string
  extraction_class: string
  extraction_text: string
  char_interval: CharInterval | null
  attributes: Record<string, unknown> | null
  isSection?: boolean
  targetPath?: string
  normalizedValue?: string
}
