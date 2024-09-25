package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.config.ConverterConfig;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.XmlExporterException;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.Norm;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({ConverterConfig.class})
@Slf4j
class XmlExporterTest {
  @Autowired private ObjectMapper objectMapper;

  @Test
  void testExporter() throws XmlExporterException {
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .coreData(generateCoreData())
            .documentNumber("document number")
            .previousDecisions(generatePreviousDecisions())
            .ensuingDecisions(generateEnsuingDecisions())
            .shortTexts(generateShortTexts())
            .longTexts(generateLongTexts())
            .status(generateStatus())
            .contentRelatedIndexing(generateContentRelatedIndexing())
            .build();

    String encryptedXml =
        new JurisXmlExporterWrapper(objectMapper).generateEncryptedXMLString(documentationUnit);

    assertThat(encryptedXml)
        .isEqualTo(
            "hoj9Xi74aXi9dPWMdaJm3noJt/m8BEO8DAYMRnMGQvpxxtnuRDwB+x8bVG6O0BTpTokyk+hWClr6pwQ5Bm5Xj3RjUxtH/6nHhB3PU/J77JGZ0VuD55w4Acxx7lTFqNyvmnlqTsKAxNbpxvuXXfXbUws4BPS0cK8U/+AogETwocA1D/jUg3Z2gewiGpYIoFxPHSGqtna1zb5iSNV8z1/Zc431SNttNiz+80IHSrFpHLwOF+gk5gx7HmazOVKoEhyQwsti6I6gC+/sygng3EFtpTKqUJiXslHEh/qy5+tS9ZK1+SpAyCIkHDs0KXQXjJi2f0Uq0zq61hW8IdQFQiss/4eIoIjgurGet/f0FJQQsdicl1WmOzwoduPzPpLUeCAMW/O7pNt4KhB8gSpG4dXEeXlVtS6f7N6xwSlcfRmGMhmiyB1nOwQFtpT3GzMV4fo/T2Yeno3MHVaU3et2ayDoI8CpvzfaR2JE2ziJDnacaj3OkenRRm7MKR+X7r7qNaA/kf2uJWnyNq8syULvFw1nN2zpchF3TxBN5xE69lhY7lVZ3K7DsYHS4lLJcTif2iJwCf/1ps1OnHgi0wzltNFE2dt0dqkwR8ljKxK4o9cXeNge68d9kae0h50f1w1iSuYpy1iApbwq81gtfOOnbtKb7VP0z1KrKbbwWA9A+2YPAZJHVNGCJ6FRddSQA/4lC4vueIPzjljm1HGL0Dlsb0wZ6Y3k/yajnIMQ34/drlyafFT5mJAZbrgqwOnp0j2oTOSdIe7G1dZOY/P+wBQDfIVR18ZeE/n8smTJ/II1YAVgwga6A1OpXAW4S7L1WBe7MtKQqm/QxBdiHDuh4mtvXSbOtAPpjxIdNai3zA40RmyC8eJ3XwZV8yJ1JWZmeCwQnitzEmPj0TmSTmQh0pVnzNivHRKR+SnoL0F3P9LhSLHyvjyDxZWItxtI8aQ6AyvRLAlsB7iYxGLkStagVOqihSqL/unpEIns3poAEBncdXSdbMeBm3fS+wm4xZ784vDliXbDEaTW6iv973vg5ckZMlawVV2090TRsclXXdYYmyCOd/B/gNdHfmrG4roR9742qB5LIRn1PjnWo5zu58arLjLT0XaGGYgl27JwXTpI4Bxae6bAO3nr5PNf2NybjZEZttIgq8IjxVcXaVtdoVL1Kv5SAlyituekreNyKYCIp4FaHlbkI1PwwfjgEneARgJaftc8l5fRp10NulfeLrdS6ZeXQ7J8KZLQ3yTjzDJRTdjBmrMePezK+wOVE1yayD2Vf2pMtROCjaFlCoR9IvcHOUCE24SGZlzD+Y/jc/w0I1C8kT4HuJjEYuRK1qBU6qKFKov+6ekQiezemgAQGdx1dJ1sx4Gbd9L7CbjFnvzi8OWJdsMRpNbqK/3ve+DlyRkyVrBVXbT3RNGxyVdd1hibII538H+A10d+asbiuhH3vjaoHkshGfU+OdajnO7nxqsuMtPRdd1Z926gk5aTaI/RXDXpLcA7eevk81/Y3JuNkRm20iC/Y3mdWimjqADycDQNNrARITRzirEtXl9CJXGi/2O6TGbfmq50am1zfoh1s1kEGFDhXV1o724KUnNeorRLyMdnSFqTJ8iYlLjH3yE60cewjcNMG70roZqvgx6cr/jtDGKIsSs4PehQi8zIGT4JeyjPOdjQcnfA3n2UnrPAfXt5YApeMT28vgQ8hKdrESeXS7hfiNE/gyGEZU1Ikpn1hBU5g42IQhssdmaAI84Z/RW84WEix4xIMLfAh0qGVco6pdRsIZGmkhEerWkFnK0+YvDUUSstIv0bxKgc+pg7Zm3NABj4Bpq8UuZZRgi74e9TIRHEiAU/tqSkE5gT0HuAcKSb2k4412CDglwVY7NI+fh27P1lAfSEIUG1+YBc1Sa3h36lnkF3/ENXskXFv/STB53SJstVViq0JNB1M8zMIdItdmfiUj8jJQ71WhRgPUHRGwQw4N+VCXTg4UOd35DXPKFbMrJrFAoMlW7ihjxySY9txL9jeZ1aKaOoAPJwNA02sBEhNHOKsS1eX0IlcaL/Y7pMZt+arnRqbXN+iHWzWQQYUOFdXWjvbgpSc16itEvIx2dIWpMnyJiUuMffITrRx7CNPPSX0bq531MSy9U/io4AZf0Oo/5gD5ybanj4cMKVAeeKXDkCNiGKvegxnf+PMesEHPl0N+3fGxyoJFZXXnoOhVsaF4VlttQDdXprZrB4v+TaDuUqCLdZA4M9z8SpepoYRP0xxWmYCvlnzbAgIFZpzryJ65o4L3xpMpJD3B1oDCKi0pvvEwcT/TxjUzvwinHcRkh3KEtBPZILveYDpHVR1dFhPxYoMA6WA0gB2k9FPR/mtEqGTCteuF1rj/Z7aCRaotKb7xMHE/08Y1M78Ipx3Pf91kz1uVtGrATbw0flvLhl0NfaoTdrjyuLbBFSJkGFCrDnQJk2s7b2D/D1F3fZp1A6RFAju8IwnC4ywDWxnA3cIZxcb2qL4Z6UTcc76kNT7mJvXo2V6UzHbiMPNFVLidHkTCG21jt79l0rXFZ9PMK15IKRcchoBk1zIpt9ngDnYS7pBqyP2cGjZujB9OYT0qzd1pB6BBVktibPyXJIVZTKxV8+FB+YUpX1WCWvVrKq53lBH/XRmW9sPjLLtBs2rH+e0YVC0gRykb0Txc8d91GksY5H/4r6H15JxwwujlSFmS8w0JfSM0qqwfkoqfHSd8juZQhxa9xXPttCsGYrH+YHhk1t8qOmwqhiBAlvxrn7Qh5A3CURvMQGAKgzpwiJkFX0n8WmJaG1iwldZ2AGgLZPZh6ejcwdVpTd63ZrIOgj4oZUkThmSazhX2SvGDbvP4FIv4fBAe02L3e02eJr48B0cMIak4dcb6F5nABbsafP5DUsPU5Z+ExdtX5XsJ/l0OKGVJE4Zkms4V9krxg27z+Ro+S/A/3FCwcF3uMXTqzxdHDCGpOHXG+heZwAW7Gnz9u/Mfvlted6SSM/+eYtNS/dLsEIUcHwmrrz3LoTXCSieRFrdk5nrM4ZK50ClxvT7WrHUCWrP5qUxFjp7dag7EbBcMngsg0xyEGSgkXNc1joIibCyeP9n9u9P9figKMKec1OUH2lFnRjcO0Asyz2IEMBfYiee4tpdZJ+3PK9ozAuonabUE3XMdb/47jSOHqHzXlvkw8D76ZMVuPSsrEZ2VTCo6NZKzxpaJomvaOEXqpkoxdf9PbPTlTb8Jm4BPhPJlqedT4m1dZbYM1JhHKo5HXHXlI6F08EM/rGm/Ozo5ZMKpD31XJ1EP82+hRW73mlfriTEcUgfLwNzmJVwOOnv1m5MH2OXVidfhScQULJeIAwnYpeZRdZI4h+fhqipYIkvn1LJCAxtx0c/uFCaGBH8rvIoxqZ/OUV1X15k/VwX2lL8N9gYq6e7hwMi5X7VFawuqFV9fXyVx5bDwxz5njT/RlsfObx6ZdeJvB/COXYefPnPENN1SBGcRH0C9Aosk2cX8qeQGMlSY1TSw4xPGF7k2ZDM52o+7kJkDL6QSFnYhrAH4aDwE0Hv/GvkhAMWpfJaEJtg3m7WQ35WkJ2D7Q7iPeRBnis9Z7OAkw6Z9+RQe7OSqvu+tYWXEGxetPWq/mvhrXkgpFxyGgGTXMim32eAOd2m6X01aUlfZ1b/Mx7sLcH7I5spUJjy9CSC3tra+HpI9EtRHP6dIXX18WqnAtYI2Ba6cbv5l280+/dCIFXQBPRyJDRo6NZFDdp+Vh4pD8UPRvAwKYUF34/F86BbVVXgjt2Z3WNShbp9UC+7pCvAeXO5nLPCikUzKfzdmZ8m/IorfWtdiJ0PLrIt9SBOXMObicQG1GVMFP9T+JILHsyb6ecRKis7MFEqdkvrz1w0kaWjEJISHmRh9GUq1Qg6IjtBv5PZh6ejcwdVpTd63ZrIOgj4oZUkThmSazhX2SvGDbvP3e4MItw4wGh42C1/r5+s8YSDTfh4+1jsKYh/icvh1wjH1EFw0TCW4WbFPc4TLfSypb+5Z/uKlav9lFBoHWT3zHgxNT8ce5s/X9I7laHzlv+DfQStu6jIKKzX0LOrAhabCOnbogOW97j6iSjOCLhF/e5MatNPE1XmsWvRbeSRtZgJWvKXGLkOxiEuM51UFAxP4fu9zuWmXZ2r4S9uP18G3sFX6rEi35wBW17UVu5A2qZAfqd8AinbqH/6FdCC4My6s73h3xTGnxZD4odG8HHWK+9x6CpIlTzyjdsJB04mUa61XrLCwDI+Y5GZ8Sr8h/AD8Lp37+fox/w40tMMj3RTzQrcY0GSXhR09fjBYxDwp+kKb1dbegJKiKIo6ij16zf0KtrgBvMO9nUT2fxTZEhi9OuMw2+yKzEuz+Aa1qvUud+A79/QBVXOK2VooNU6P45cyOd9w/y6+3VZ/etlvfXwL4+q+N6NW7h2gGSxcQvnSqGmS8w0JfSM0qqwfkoqfHSd27fgXTAiNHIhvLm65liD6HRWzPz7OEj+kjSPcjWb+01x8oSH+OMXSZbHBFWcBs4H4YL8jq95Pa8+62b0OsU1qfkWBMFUnGpJmMt6NkcxgoSeZNJckwPEEss8anCCPfBxvCVR5iR6ymGdmyJEtFCA6BApc0t2xSOuqlyiukJUDZVfSThoN54pBFuAACq07qquoaRRboichKcz2JiKMpupAWN7ufGVKmsu9hZimnBL3C2v0zNQ+r9ecyHgfgT3ra1OtKkN/DvsZe0FMiSVTIEbgRWcnyvYX0VJPq5rPtVxvgMH6j8/bcJf6FTxqtsqPCQv4+h0DUbTB7lZVnBxJMuUjDooKU1RUdzmpnSl2QuoiWhqWWADmggHaihShiBZvaEyGj+md6TJWrE5oDTHrDpM0NtaAeDnGyC3+azM/JIMFKeMKC+KZH3Gzf5ukEZWz1YE3Dyfez25jtoY6BaUex7FG1hCDhM/LcxqUbbpxFo98PYzU3HfK7CDUap4AE2y74iVDnx5rqWFGBGrtcNVF4nZUL082B3TK+syZlerk8wmYhEvgcitEykHfQufaGKLc7QXLI4XWyFO9UQvx8RLavPCPAjrWrDHCWw02z5jotuolSYATUgYi9QhTAOHYeMcbHHLHcXpUt+zOAafg0WQ5z8fZscjj+qJPpUd/pNzS+KJUpBQws3CdMKI5RmnmDKDaa6ddKV+5P7Nsfp9Cwr2dfJ95+GwyJ849/scWoXdBtI7D6586u/ZSS4roNFaHeK7lt7DM0wBLnMBR09bK/Ola2i7KJFV2gh5nF6bCas0peJDG4knX/GsYdhOphhlN7eTxwnA+hZa9oZ0AYW43rbxeFONfO6k2evXwvMk6rVLgB5/My8cEaRvlkO6urEM47MVX25ipk9rYs6k4l013AVj0hvsSFnjmchuPeddCJ+DPBq+CUmR2cedmlyb7En/OyW1457AY21wMT9hknqO///rT31no/q1VCKmc8ugMQfys7wt5ihaRXSYNU3Nn2J1VW/EApfbciausMARcU61rB0ulJfPglGa7KH3+m3gaNLQIxVJMM6REk4zrExUDra3q41vdRJveOwN3acfL9n5MLqzq8f+J4wJlVLhDqvssVCf9bqpd+gRnWSW/exw8lC+1L7icyJBkrEujI2Ib6Bd4zWo2uit+m5MatNPE1XmsWvRbeSRtZgJWvKXGLkOxiEuM51UFAxP4dPaW8bgYH3DHATkGjrg417MAlwPB2MrxryjRjkwL9IxAc5piUPy0VzyENKM5abGbN2mUVCvQLOmMKBgoCN16+QNiJC0Z4XyTWY9ZPmiJeIYM9xuOCZ7iWYPI0LLalOisLDCx0pXW9iwy/pl6AbMiI8yFuwFrsIL8klO4JLJJ53mVLjqwiaNFi/IT3ivMDNCWKt2M6hArgZbGY880UKbhYOLqZLoGu7qlc7zodUJXSw3rhzAeA8TWZ8Jp4VfhSbint1QwjgxBT6qrNoEy+5niyLATZoy0H0NRJ44D3qVuIfmwuuNAAhqxsJoMFl9Z8k3AaAxPvaB0qnmAkL5B6nlhLwy1QQkWXP0icjR+8PNv2MjbXAxP2GSeo7//+tPfWej+rVUIqZzy6AxB/KzvC3mKFpFdJg1Tc2fYnVVb8QCl9tyJq6wwBFxTrWsHS6Ul8+CUZrsoff6beBo0tAjFUkwzpYPKZyQ1MumDMgtptpJTry47A3dpx8v2fkwurOrx/4njAmVUuEOq+yxUJ/1uql36BGdZJb97HDyUL7UvuJzIkGTAIxVrTnbZJpAP4KW1qo5Lkxq008TVeaxa9Ft5JG1mAla8pcYuQ7GIS4znVQUDE/wkGniV6LgCQEoAnWGzLT+WGkxYN3kcFTLO5jko6RPZnEBzmmJQ/LRXPIQ0ozlpsZs3aZRUK9As6YwoGCgI3Xr1wzj9vEJghi1DxbDcK9nppgz3G44JnuJZg8jQstqU6K6tUYezPsH038V8sfuaef939w1pkslNlTMxygILqh+y1ybhEvliVBS72ISVgpulWXokg7uKe0NQNsoWtDsZm5VcYtanNrUQeu9ju/ulAifAbuqG78KWBRQkJiJdJb0NKhgBLxOn81hyY00T8uHAKxx5bf7NcTj/bg4Qrj23SU5Dutw417aP0uvVUraS+SjMSyszMEACnydLT0CePZrYgYVopEIjdgA98by/b1LxhsMFzEmUfYmd6KVhrrLwHQ8qGKvnjRoxA/h35095ZfbfBYHjU5pU/MQIQFA3oGOiBCpX8vQOKGyL7jLDok7a8OYc1wiwFREk9UqV1/k2cGxMR5EwYzTrhZr8Q4likMRQTPRFn+c8nPiM0ZFpN7tq7WxkUk7HXpD4RzJjnambXiv82hEhauYldqSZeGocH5OFGHY8/cukVhnK2XtLjl7Hn5xjWeXKbJeH8jv3B938C54ZSHIy9A4obIvuMsOiTtrw5hzXBmGKXCQUsH3qICaXVb86qAbLnyXOVZtp/5KsswTzp18+2Wa4i7oc6FDAp2MBKmdj98NxaX/snUYqxeDG7jEESuCl4xPby+BDyEp2sRJ5dLuKzN2HCvuWrl4AqDMPGUFl3jDV3irgqsMqzrV8Zbh7mReRqMt8b8uHLURN/iWSGWaAu+S/lcMf7bT0QISYaqZbHDIbfEBHdkTgO97D/re6z/TOYqqfYnvIM6+8E5D766QGyVsqqf6Efw5iikvJnBWJns6JzpcLf0D/JCpo1/SipnwlV9MbcO20fzxrIo4lcPx6nh7M99eKWvZgIyXojQNfAvQOKGyL7jLDok7a8OYc1wnNtOsFTLOfyFe0NdM9mdrafHODlWqFzo+43Aq7xAn7d5lmA60rGEuacfCKSQHFOMbJUmsX9wyegk0HVokpBw9foUOD90u9NjUZ7DJJeeT2oNjsBwrTitqfU8/6/d9ZLusUkqHVw642Z0l5eom0g/mpDodAefzH0ijNInVJZ3Xjr+fQ/hjlA7rA8PGxi/uPgt6EgtsBA7p43L9Sa1W/NCbcSZR9iZ3opWGusvAdDyoYqb2zNuCtD4yJDmfhBBetRdTMFUOWQu4fRinIYbtZUtRO0xMV5/jLv8Lo8dllhUsRhBA6OLcHs68XL21fjARzjlNoU4DvxNtsVlY22xZqATmVixx8f+8MI6CzcJcpI6FVbL2+z6/tMzZglAdwNSr9ttlRdLA10VDqieldQ+aS7f3HmWYDrSsYS5px8IpJAcU4yIYFdFH3RGyqrfUC2aw4D4zX7QxBdIXZmQ9m6Yce0i4U1JtpS85i5xCsgDSBJbID8PYqmpTanSDblzfBZCcAUc1YFPLhZvwRFx0AzchH+eyVw/Twm4FlrImtDQ+36KioAwhRWAoCdKBL32YXSXj2OGokg7uKe0NQNsoWtDsZm5Veny53z36lV7QOMrOGnzedCD1OxGZE6C5gZYahTgfGqNctYzCA6G/SvuYhhAahxkaJRTBCfeDhM5WFQkWrlxC+giKtva7ls+yaChkC3n/Ryuqrq0VdBZxyCVBIdr/bYWSXpCEtYWIprdyxKkMsWF6nEw2bHcgcHMMCpqbHvGAZaeIvKPP2lJyozn1CBMStiEh6s3qM3ayj6Ok6mBRxQpDOAxPaF+g47l+mWcVkJWk4gg5xWyPhUoPkheE1trifO3cfQbHbfCZ6DFrcLc+HC6fN8M9aXTMSYPC4kgKFzi8v82noMvBTKyEMBK3YarzU8FUP+29cYRpsCTQW6d43kZUBYyxM7p84Cl1fxEmBhYrW/+yeCFbQuZYplSYen7j1NGQRK273ZE8uQ5gs45exV/P3K308LnwBOShIpveWhIzF2AzXI7YRiFshcdW2H1q4FOby9A4obIvuMsOiTtrw5hzXCSM+CF+TWDbejcKDGQo786QEW7U9QJZqB/se+UDvgF7f5zyc+IzRkWk3u2rtbGRSTsdekPhHMmOdqZteK/zaESFq5iV2pJl4ahwfk4UYdjz/5qbDF2LqUn13eyBiMYvVrsQ11vp0qD8O/xwr05+HXi5xWyPhUoPkheE1trifO3cS1oVaMeqU3yvDxPjpR2eKhsufJc5Vm2n/kqyzBPOnXz7ZZriLuhzoUMCnYwEqZ2P3w3Fpf+ydRirF4MbuMQRK4KXjE9vL4EPISnaxEnl0u4n9ZII+0dqJpAPo+nzxu/KXysRsVJlOzcpKyWTtafsn6Q6HQHn8x9IozSJ1SWd146w1tBrlkWD2i8WPdEeiNfJsMht8QEd2ROA73sP+t7rP9M5iqp9ie8gzr7wTkPvrpAbJWyqp/oR/DmKKS8mcFYmezonOlwt/QP8kKmjX9KKmdROSYcKr38CpqATNrgNTJPuh+7Nu1XvRPXC2RG9r6LxXfC0y+8kqWpwh1wx1lR/K9Rxi3e2eY2vk1Imv9Rr9wL/nPJz4jNGRaTe7au1sZFJOx16Q+EcyY52pm14r/NoRIWrmJXakmXhqHB+ThRh2PPGBl+qZi7aFRKKFu3Mta+bYmGVNNYYXSf7OvKutctpUY6+WiwKeiUEr6aLHAjYYycMTJWaDdBbQuq64V5OB5ia2ic2DeDqm6sGkoE7KaFPoe63MbhS/0ky7mw/MxWwJlcFEee8vL4ahiAaCoo4PU98cfKinFZts+2SLvoFU2QMkzWYAp2tIzZVHluupPEXGu/90x2IVAmZMgUpsd/2FYORxmEh+lXoQUM0euCfhJZRmwvQOKGyL7jLDok7a8OYc1w8ctdE+FZeA1luTON0cftiJzXCUn14Zqu8TogDmje3BjSdxwk2e58fHWqYhXNzS2rxKFjCwwKJ6ozoo3DP7VqAYOyfiyvQVTsMHoUiCqLiSOUps3Y3lo+dOq1opjOevEfD5+yabcqaIdk/MuyZeHXyxiap+t8teV1/+jg9w2NH/OAEvE6fzWHJjTRPy4cArHHESANTYtamz6DBx6qxYSBveH17DIjOQcPU9ffE9+36+PoSC2wEDunjcv1JrVb80JtxJlH2JneilYa6y8B0PKhipvbM24K0PjIkOZ+EEF61F1MwVQ5ZC7h9GKchhu1lS1E0nvirBGXYXPSGL4sCSm4/kJHg9+OoAkUNITzs2UVRph5Goy3xvy4ctRE3+JZIZZoHCFtlttB4/w9FgFpU5JqMXNu1lgKy/86rIEChawWOImzMwQAKfJ0tPQJ49mtiBhWikQiN2AD3xvL9vUvGGwwXMSZR9iZ3opWGusvAdDyoYppQgiDRz4d5cdXVaaW/dCtYujggO1hvU1mrLqu9Sluc0jEyR2eWnLy8SSuToLnNZFTJdotvET9VsJHWS/lTiDA2ktC0d8N+Y+yjRpcg1+UnNJ3HCTZ7nx8dapiFc3NLavEoWMLDAonqjOijcM/tWoBg7J+LK9BVOwwehSIKouJI5SmzdjeWj506rWimM568R8bsaMvw1sE0qYFhhCz1EtM8+lNl0DU9W9Xx/l68aRMPecVsj4VKD5IXhNba4nzt3Gabw1Nd2SY75zzSnKkhkot6n7EKND0sxXBrdvmAbDEXLMzBAAp8nS09Anj2a2IGFaKRCI3YAPfG8v29S8YbDBcxJlH2JneilYa6y8B0PKhimlCCINHPh3lx1dVppb90K10NB9NN648pXEI+MdkS25Fw/8v9y94WNsS4LmN1OOj0Y6HHCK/1bxwsYX1/bVNjWB6C+GOKXXIVyIDU6FVV+h9y9vs+v7TM2YJQHcDUq/bbZUXSwNdFQ6onpXUPmku39x5lmA60rGEuacfCKSQHFOMhG/qo0aM0CWR9lLwUKUmdZs3lzaENXCBh6GNoUgkKe0UzWIuc6RYB1HNnIfjpGP5d8LTL7ySpanCHXDHWVH8r+aT6V46jT9nzDdX8MHEG4nCZmtBtiO74gA7hzo+ZCVDokg7uKe0NQNsoWtDsZm5Vbq9+4mkekWtmDq8d+qsj4SUgDe2wiba6h7+2cvdIkLsoGF/ZbymmDLrDnEv73qOqzr5aLAp6JQSvposcCNhjJzhzfdhS0MAbWC0xdXvvg17klpx3Z6EiAJbZ0vjGPwh3qq6tFXQWccglQSHa/22Fkl6QhLWFiKa3csSpDLFhepxMNmx3IHBzDAqamx7xgGWng7oZH5BGg+Htz1YIrY97u5c20uKY0qFKLXjk82C/XeXlFMEJ94OEzlYVCRauXEL6DHPxTOyb1WHsoagdVrBnprSdxwk2e58fHWqYhXNzS2rxKFjCwwKJ6ozoo3DP7VqAYOyfiyvQVTsMHoUiCqLiSOUps3Y3lo+dOq1opjOevEf6fkN0U+gcWIfxRRSq5ONQNQ5RNaK4Nh0tzXd4/IU1bAeZu2jSc/VpdgMSiyhAgi97oU1vSaF0smz+O6nvbPN8PPLAsC1M1YfQ+Uaj8UlguYQ5PwyTJQHkDn/oG1r5K5wLokMQTBmSUSWq5k9Qqmgo2hBecNt1X8mbSaITRaGtLCvRdo2CJ2LeMfQM2BubTtAuewNmhJw3jVyHXG8j5E0POKGVJE4Zkms4V9krxg27z+FtmaaCocxGHQ3Z1RWyj1Q1E4a5zSM/cKavBQQ3JfZjzdoxjSAU0d4Nnq8qX8B1TWTWGNgzwpi0zUpffmGlZyYDTMQlZm9xsRyAB4D1IO6VUgUO0AOaMe1w0ODfybZXgh5VvF4ogfwNbGBrjL43Zq3uR+E6DFIFw8qCN8Efsa6meG9x8VDhf+7cbBlfUclDxzyaaCNQrUTOHJ5G0po7/08ZsPyGCiXuIepllYs5PG0PC0BmpZPWlg9w7AbztzVKnoXiZ5jz7HMF8YL6jLhJipErAv0JRIBr2wJiIyIncXrxOCGkcmJYUS73f+y3IEF7ySdFKOtiU9h76VxUISrHMuwyE3l9mtKYXkhB2NEKHAKy8NOSmZfaEf938hxvEWrNe+U5e3DKxb2etk5MbFgZPmQClIv7zyLmsSUEZTv5wHk2d/0TDXzg4gwPOH2q96J3hKW/uWf7ipWr/ZRQaB1k98xMybLnJUlr5fPLtqQmWA6t6SF7cMb2y433rYfZiQ58304zoyzDTIcItslmzSqeh4pNyAuE+Vm5ehbxbdqsy3QwfPW0Co3hzlxVw0L1s01p/rHXGw5YVLVxhivq9eUENuXeVbxeKIH8DWxga4y+N2at33c24rc6QdD+9TXZ5GRKPbrp+aqhr4efPq/1N2fEqO5hx4SXjSUKO0hRWPapepSfeaDQnNeRLlDTT4LO5E6hWwtefRzB3JblwQBwW4A/XRX7EBIWlwCgFeCWlfO3d4f+KtYN5YM8pEaA72EKLyZn8HfIBJT7zwlCBaQb6N4IkjLiXGc3uhVZYbG+Ksrn15uWY7kxk14+OKnDu/ndBTm1pESJd0DYosU8CbonjeRhHbR/2G3kzrDwxUDXAg24C+rpQ9R/99cS1JocUifdRGm9haZVjHyJilpk8f8KdKH5FuJeVbxeKIH8DWxga4y+N2at8M+O+917QqL2hzUIcN8ixI8+GeLJoroYyDvQUrVpn0IyE3l9mtKYXkhB2NEKHAKy8NOSmZfaEf938hxvEWrNe8YZZPPMAxGTLeumEItPx8+tkkp55/R7+DJwl9FEEO5glGEnF381yrd1P/5ngDDjoScAbgTyJj5k5LQ5ao8BDqOz8g9mAdNUTFq8ElKDi65kw+F/LmfG6iXfKb20vyKdEI0j63/72PIrXSsrZVvcXP+a6YZsNesPFdDBsBsh6l2MfRjEH3qISW+55vFtJV8CT5TK0orI0/4O0Dw8Jed3AJJkpt0mJd+dip2LgF2T3k21MLd3GnM1uymymJ4iBsBP1Fxjyspgk/1gQIXFrJ4Kjk+fdGpd0lsDYDfIhKw9psY2l/caVU3yeK+hqoP8/OjSm9BEoiM4BBuWhwUcrlXwrR8eMiVc4OvntF64xvnAVKAnDPgyBr+j/+V8y02oQkoXIRil5tzIMYYNCZoJ6MBdx1s7kGOJoElPA//uMd2vJU5qg2NG5Chec1fNmcCM7mBdN0vm8O+d+4iUpkwvq5DcC59tWoOA7i0ZX6CqUeh/F6w01JxPK0ofx9jEey50DIt+AKLPeNjtx57IvqHu2SbQFHMJctQeK4P1gGUfAcvGqZ72w==");
  }

  private ContentRelatedIndexing generateContentRelatedIndexing() {
    return ContentRelatedIndexing.builder()
        .activeCitations(generateActiveCitations())
        .norms(generateNorms())
        .keywords(generateKeywords())
        .fieldsOfLaw(generateFieldOfLaws())
        .jobProfiles(generateJobProfiles())
        .dismissalTypes(List.of("type 1", "type 2"))
        .dismissalGrounds(List.of("ground 1", "ground 2"))
        .collectiveAgreements(List.of("agreement 1", "agreement 2"))
        .hasLegislativeMandate(true)
        .build();
  }

  private List<FieldOfLaw> generateFieldOfLaws() {
    List<Norm> norms =
        List.of(
            Norm.builder()
                .abbreviation("field of law norm abbreviation 1")
                .singleNormDescription("field of law norm description 1")
                .build(),
            Norm.builder()
                .abbreviation("field of law norm abbreviation 2")
                .singleNormDescription("field of law norm description 2")
                .build());
    return List.of(
        FieldOfLaw.builder()
            .identifier("norm identifier 1")
            .text("norm text 1")
            .norms(norms)
            .build(),
        FieldOfLaw.builder().identifier("norm identifier 2").text("norm text 2").build());
  }

  private List<String> generateKeywords() {
    return List.of("keyword 1", "keyword 2");
  }

  private List<NormReference> generateNorms() {
    List<SingleNorm> singleNorms1 =
        List.of(
            SingleNorm.builder()
                .singleNorm("single norm 1")
                .dateOfVersion(LocalDate.parse("1999-01-17"))
                .dateOfRelevance("1999")
                .build(),
            SingleNorm.builder().singleNorm("single norm 2").build(),
            SingleNorm.builder().dateOfVersion(LocalDate.parse("1985-07-26")).build(),
            SingleNorm.builder().dateOfRelevance("1973").build());
    return List.of(
        NormReference.builder()
            .singleNorms(singleNorms1)
            .normAbbreviation(
                NormAbbreviation.builder().abbreviation("norm abbreviation 1").build())
            .build(),
        NormReference.builder()
            .singleNorms(Collections.emptyList())
            .normAbbreviationRawValue("norm abbreviation raw value")
            .build());
  }

  private List<ActiveCitation> generateActiveCitations() {
    return List.of(
        ActiveCitation.builder()
            .citationType(
                CitationType.builder().jurisShortcut("ct1").label("citation type 1").build())
            .fileNumber("active citation file number 1")
            .documentType(
                DocumentType.builder()
                    .jurisShortcut("acdt1")
                    .label("active citation document type 1")
                    .build())
            .court(
                Court.builder()
                    .type("active citation court type 1")
                    .location("active citation court location 1")
                    .build())
            .decisionDate(LocalDate.parse("2001-07-22"))
            .documentNumber("active citation document number")
            .build(),
        ActiveCitation.builder()
            .citationType(
                CitationType.builder().jurisShortcut("ct2").label("citation type2").build())
            .fileNumber("active citation file number 2")
            .documentType(
                DocumentType.builder()
                    .jurisShortcut("acdt2")
                    .label("active citation document type 2")
                    .build())
            .court(
                Court.builder()
                    .type("active citation court type 2")
                    .location("active citation court location 2")
                    .build())
            .decisionDate(LocalDate.parse("2005-11-29"))
            .build());
  }

  private List<String> generateJobProfiles() {
    return List.of("job profile 1", "job profile 2");
  }

  private Status generateStatus() {
    LocalDateTime localDateTime = LocalDateTime.of(2020, Month.MAY, 6, 17, 35);
    ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("UTC"));

    return Status.builder()
        .publicationStatus(PublicationStatus.PUBLISHED)
        .withError(false)
        .createdAt(zonedDateTime.toInstant())
        .build();
  }

  private ShortTexts generateShortTexts() {
    return ShortTexts.builder()
        .decisionName("decision name")
        .headline("headline")
        .guidingPrinciple("guiding principle")
        .headnote("headnote")
        .otherHeadnote("other headnote")
        .build();
  }

  private LongTexts generateLongTexts() {
    return LongTexts.builder()
        .tenor("tenor")
        .reasons("reasons")
        .caseFacts("case facts")
        .decisionReasons("decision reasons")
        .dissentingOpinion("dissenting opinion")
        .otherLongText("other long text")
        // outline is missing here because otherHeadnote and outline must not be filled both at the
        // same time
        .build();
  }

  private List<EnsuingDecision> generateEnsuingDecisions() {
    return List.of(
        EnsuingDecision.builder()
            .fileNumber("pending decision file number")
            .court(
                Court.builder()
                    .type("pending decision court type")
                    .location("pending decision court location")
                    .build())
            .documentType(
                DocumentType.builder()
                    .jurisShortcut("peddt")
                    .label("pending decision document type")
                    .build())
            .note("pending decision note")
            .pending(true)
            .build(),
        EnsuingDecision.builder()
            .fileNumber("ensuing decision file number")
            .court(
                Court.builder()
                    .type("ensuing decision court type")
                    .location("ensuing decision court location")
                    .build())
            .decisionDate(LocalDate.parse("2005-06-17"))
            .documentType(
                DocumentType.builder()
                    .jurisShortcut("eddt")
                    .label("ensuing decision document type")
                    .build())
            .note("ensuing decision note")
            .pending(false)
            .documentNumber("ensuing decision document number")
            .build());
  }

  private List<PreviousDecision> generatePreviousDecisions() {
    return List.of(
        PreviousDecision.builder()
            .fileNumber("previous decision file number 1")
            .deviatingFileNumber("previous decision deviating file number 1")
            .court(
                Court.builder()
                    .type("previous decision court type 1")
                    .location("previous decision court location 1")
                    .build())
            .decisionDate(LocalDate.parse("2005-06-17"))
            .documentType(
                DocumentType.builder()
                    .jurisShortcut("pddt1")
                    .label("previous decision document type 1")
                    .build())
            .documentNumber("previous decision document number 1")
            .build(),
        PreviousDecision.builder()
            .fileNumber("previous decision file number 2")
            .court(
                Court.builder()
                    .type("previous decision court type 2")
                    .location("previous decision court location 2")
                    .build())
            .decisionDate(LocalDate.parse("2013-08-12"))
            .documentType(
                DocumentType.builder()
                    .jurisShortcut("pddt2")
                    .label("previous decision document type 2")
                    .build())
            .build());
  }

  private CoreData generateCoreData() {
    return CoreData.builder()
        .court(Court.builder().type("court type").location("court location").build())
        .leadingDecisionNormReferences(
            List.of("leading decision norm reference 1", "leading decision norm reference 2"))
        .documentationOffice(
            DocumentationOffice.builder().abbreviation("documentation office").build())
        .inputTypes(List.of("input type 1", "input type 2"))
        .deviatingDecisionDates(
            List.of(LocalDate.parse("2011-01-09"), LocalDate.parse("2011-01-07")))
        .deviatingEclis(List.of("deviating ecli 1", "deviating ecli 2"))
        .previousProcedures(List.of("procedure 1", "procedure 2"))
        .deviatingCourts(List.of("deviating court 1", "deviating court 2"))
        .deviatingFileNumbers(List.of("deviating file number 1", "deviating file number 2"))
        .fileNumbers(List.of("file number 1", "file number 2"))
        .region("region")
        .legalEffect("ja")
        .documentType(DocumentType.builder().jurisShortcut("dt").label("document type").build())
        .appraisalBody("appraisal body")
        .ecli("ecli")
        .decisionDate(LocalDate.parse("2011-01-08"))
        .procedure(Procedure.builder().label("procedure 3").build())
        .build();
  }
}
