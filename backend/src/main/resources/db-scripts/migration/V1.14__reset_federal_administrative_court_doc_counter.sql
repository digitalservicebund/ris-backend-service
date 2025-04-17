-- Reset document counter for BVerwG with new pattern
DELETE
FROM document_number
WHERE documentation_office_abbreviation = 'BVerwG'
  AND year = '2025'
