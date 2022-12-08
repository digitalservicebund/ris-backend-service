package norms.utils

import com.google.gson.Gson
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.EditNormFrameController

fun convertNormFrameRequestSchemaToJson(normFramePropertiesRequestSchema: EditNormFrameController.NormFramePropertiesRequestSchema): String {
    return Gson().toJson(normFramePropertiesRequestSchema)
}
