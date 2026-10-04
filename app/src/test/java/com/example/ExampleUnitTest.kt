package com.example

import com.example.generator.CsvQrParser
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `csv parser parses semicolon separated restaurant data correctly`() {
    val items = CsvQrParser.parse(CsvQrParser.SAMPLE_RESTAURANT_CSV)
    assertTrue(items.size >= 5)
    val first = items.first()
    assertEquals("Table 01 - Terrasse", first.title)
    assertTrue(first.content.contains("mon-resto.fr"))
    assertTrue(first.isDynamic)
    assertEquals("TABLE 01", first.frameText)
  }

  @Test
  fun `csv parser handles comma separated products`() {
    val items = CsvQrParser.parse(CsvQrParser.SAMPLE_PRODUCTS_CSV)
    assertEquals(4, items.size)
    assertTrue(items.all { it.isValid })
  }

  @Test
  fun `predefined style templates contain minimalist, brand and tech categories`() {
    val templates = com.example.data.model.PredefinedStyleTemplates.templates
    assertTrue(templates.size >= 8)
    assertTrue(templates.any { it.category == com.example.data.model.StyleCategory.MINIMALIST })
    assertTrue(templates.any { it.category == com.example.data.model.StyleCategory.BRAND_FOCUSED })
    assertTrue(templates.any { it.category == com.example.data.model.StyleCategory.TECH_INSPIRED })
    
    val sapphire = templates.first { it.id == "brand_sapphire" }
    val sampleQr = com.example.data.model.QrCodeEntity(title = "Test", rawContent = "https://test.com")
    val styled = sapphire.applyTo(sampleQr)
    assertEquals("#2563EB", styled.eyeColorHex)
    assertEquals("ROUNDED", styled.moduleShape)
  }

  @Test
  fun `vcard contact serializes multiple phones and emails into standard vcard3 format`() {
    val contact = com.example.data.model.VCardContact(
      prefix = "Dr.",
      firstName = "Claire",
      lastName = "Moreau",
      phones = listOf(
        com.example.data.model.VCardPhoneItem(number = "0611223344", type = com.example.data.model.PhoneType.CELL),
        com.example.data.model.VCardPhoneItem(number = "0144556677", type = com.example.data.model.PhoneType.WORK),
        com.example.data.model.VCardPhoneItem(number = "0144556699", type = com.example.data.model.PhoneType.FAX)
      ),
      emails = listOf(
        com.example.data.model.VCardEmailItem(email = "pro@clinic.fr", type = com.example.data.model.EmailType.WORK),
        com.example.data.model.VCardEmailItem(email = "claire@gmail.com", type = com.example.data.model.EmailType.HOME)
      ),
      hasCompany = true,
      company = "Clinique Parisienne",
      jobTitle = "Chirurgien Chef",
      hasAddress = true,
      street = "10 Rue de la Paix",
      city = "Paris",
      postalCode = "75002"
    )

    val vcardStr = contact.toVCard3()
    assertTrue(vcardStr.contains("BEGIN:VCARD"))
    assertTrue(vcardStr.contains("VERSION:3.0"))
    assertTrue(vcardStr.contains("FN:Dr. Claire Moreau"))
    assertTrue(vcardStr.contains("TEL;TYPE=CELL:0611223344"))
    assertTrue(vcardStr.contains("TEL;TYPE=WORK:0144556677"))
    assertTrue(vcardStr.contains("EMAIL;TYPE=WORK:pro@clinic.fr"))
    assertTrue(vcardStr.contains("EMAIL;TYPE=HOME:claire@gmail.com"))
    assertTrue(vcardStr.contains("ORG:Clinique Parisienne;"))
    assertTrue(vcardStr.contains("ADR;TYPE=WORK:;;10 Rue de la Paix;Paris;;75002;France"))
    assertTrue(vcardStr.contains("END:VCARD"))

    // Test reverse parsing
    val parsed = com.example.data.model.VCardContact.fromRawVCard(vcardStr)
    assertEquals("Moreau", parsed.lastName)
    assertEquals("Claire", parsed.firstName)
    assertEquals(3, parsed.phones.size)
    assertEquals(2, parsed.emails.size)
    assertEquals("Clinique Parisienne", parsed.company)
  }
}
