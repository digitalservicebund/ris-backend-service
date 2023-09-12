package utils

import com.google.gson.Gson
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller.EditNormFrameControllerTest

fun convertEditNormRequestTestSchemaToJson(
    editNormRequestSchema: EditNormFrameControllerTest.NormFramePropertiesTestRequestSchema,
): String {
  return Gson().toJson(editNormRequestSchema)
}
