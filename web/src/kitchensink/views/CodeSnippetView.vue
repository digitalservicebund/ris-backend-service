<script lang="ts" setup>
import CodeSnippet from "@/components/CodeSnippet.vue"

const xml =
  '<?xml version="1.0" encoding="UTF-8"?>\n' +
  '<juris-r datenpool="" doknr="KORE2022007657">\n' +
  "<metadaten>\n" +
  "<gericht>\n" +
  "<gertyp>AnU</gertyp>\n" +
  "<gerort>Berlin</gerort>\n" +
  "</gericht>\n" +
  "<entsch-datum>01.12.1992</entsch-datum>\n" +
  "<aktenzeichen>KORE2022007657</aktenzeichen>\n" +
  "<doktyp>AnU</doktyp>\n" +
  "</metadaten>\n" +
  "<paratrubriken>\n" +
  "<zuordnung\n" +
  "<aspekt>Entscheidungsname abc</aspekt>\n" +
  "<begriff>Begriff abc</begriff>\n" +
  "</zuordnung>\n" +
  "<zuordnung>\n" +
  "<aspekt>Eingangsart abc</aspekt>\n" +
  "<begriff>Begriff abc</begriff>\n" +
  "</zuordnung>\n" +
  "</paratrubriken>\n" +
  "</juris-r>"
</script>

<template>
  <CodeSnippet title="XML" :xml="xml" />
</template>
