package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort.Query
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort.QueryParameter
import org.springframework.data.relational.core.query.Criteria

class NormsCriteriaBuilder : NormsMapper {

    fun getSearchCriteria(query: Query): Criteria {
        var criteria = Criteria.empty()
        query.parameters.forEach { criteria = criteria.or(getFieldCriteria(it)) }

        return Criteria.from(criteria)
    }

    private fun getFieldCriteria(queryParameter: QueryParameter): Criteria {
        if (queryParameter.value == null) {
            return Criteria.where(queryFieldToDbColumn(queryParameter.field)).isNull
        }

        if (queryParameter.isFuzzyMatch) {
            return Criteria.where(queryFieldToDbColumn(queryParameter.field)).like("%${queryParameter.value}%")
        }

        return Criteria.where(queryFieldToDbColumn(queryParameter.field)).`is`(queryParameter.value)
    }
}
