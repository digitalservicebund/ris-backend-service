package de.bund.digitalservice.ris.norms.domain.specification.norm

// import de.bund.digitalservice.ris.norms.domain.entity.Book
// import de.bund.digitalservice.ris.norms.domain.entity.Norm
// import io.mockk.every
// import io.mockk.mockk
// import java.util.*
// import org.assertj.core.api.Assertions.assertThat
// import org.junit.jupiter.api.Test
//
// class HasValidSectionElementsDepthTest {
//
//   @Test
//   fun `it is satisfied if the norm contains a section elements depth of max 9`() {
//     val instance = mockk<Norm>()
//     every { instance.guid } returns UUID.randomUUID()
//
//     val nestedStructure = createNestedStructure(9)
//
//     every { instance.sections } returns listOf(nestedStructure)
//
//     assertThat(hasValidSectionElementsDepth.evaluate(instance).isSatisfied).isTrue()
//   }
//
//   @Test
//   fun `it is not satisfied if the norm contains a section elements depth bigger than 9`() {
//     val instance = mockk<Norm>()
//     every { instance.guid } returns UUID.randomUUID()
//
//     val nestedStructure = createNestedStructure(10)
//
//     every { instance.sections } returns listOf(nestedStructure)
//
//     assertThat(hasValidSectionElementsDepth.evaluate(instance).isSatisfied).isFalse()
//   }
//
//   private fun createNestedStructure(depth: Int): Book {
//     var currentSection = Book("Initial Book", UUID.randomUUID(), "Initial Designation", 1)
//     repeat(depth - 1) {
//       val nestedBook =
//           Book(
//               "Nested Book",
//               UUID.randomUUID(),
//               "Nested Designation",
//               it + 1,
//               listOf(currentSection))
//       currentSection = nestedBook
//     }
//     return currentSection
//   }
// }
