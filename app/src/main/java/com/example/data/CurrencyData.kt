package com.example.data

enum class CurrencyRegion(val label: String) {
  ALL("All"),
  POPULAR("Popular"),
  AFRICA("Africa"),
  AMERICAS("Americas"),
  EUROPE("Europe"),
  ASIA_ME("Asia & Middle East"),
  OCEANIA("Oceania")
}

data class GlobalCurrency(
  val code: String,
  val symbol: String,
  val name: String,
  val country: String,
  val flagEmoji: String,
  val region: CurrencyRegion,
  val isPopular: Boolean = false,
  val sampleAmount: String = "150,000"
)

object CurrencyRepository {
  val allCurrencies: List<GlobalCurrency> = listOf(
    // Popular / African Core
    GlobalCurrency("KES", "KSh", "Kenyan Shilling", "Kenya", "🇰🇪", CurrencyRegion.AFRICA, isPopular = true, sampleAmount = "150,000"),
    GlobalCurrency("USD", "$", "US Dollar", "United States / Global", "🇺🇸", CurrencyRegion.AMERICAS, isPopular = true, sampleAmount = "1,500.00"),
    GlobalCurrency("EUR", "€", "Euro", "European Union", "🇪🇺", CurrencyRegion.EUROPE, isPopular = true, sampleAmount = "1,400.00"),
    GlobalCurrency("GBP", "£", "British Pound", "United Kingdom", "🇬🇧", CurrencyRegion.EUROPE, isPopular = true, sampleAmount = "1,200.00"),
    GlobalCurrency("TZS", "TSh", "Tanzanian Shilling", "Tanzania", "🇹🇿", CurrencyRegion.AFRICA, isPopular = true, sampleAmount = "3,900,000"),
    GlobalCurrency("UGX", "USh", "Ugandan Shilling", "Uganda", "🇺🇬", CurrencyRegion.AFRICA, isPopular = true, sampleAmount = "5,600,000"),
    GlobalCurrency("RWF", "FRw", "Rwandan Franc", "Rwanda", "🇷🇼", CurrencyRegion.AFRICA, isPopular = true, sampleAmount = "1,950,000"),
    GlobalCurrency("ZAR", "R", "South African Rand", "South Africa", "🇿🇦", CurrencyRegion.AFRICA, isPopular = true, sampleAmount = "28,000"),
    GlobalCurrency("NGN", "₦", "Nigerian Naira", "Nigeria", "🇳🇬", CurrencyRegion.AFRICA, isPopular = true, sampleAmount = "2,300,000"),
    GlobalCurrency("GHS", "GH₵", "Ghanaian Cedi", "Ghana", "🇬🇭", CurrencyRegion.AFRICA, isPopular = true, sampleAmount = "24,000"),
    GlobalCurrency("ETB", "Br", "Ethiopian Birr", "Ethiopia", "🇪🇹", CurrencyRegion.AFRICA, isPopular = false, sampleAmount = "180,000"),
    GlobalCurrency("EGP", "E£", "Egyptian Pound", "Egypt", "🇪🇬", CurrencyRegion.AFRICA, isPopular = true, sampleAmount = "72,000"),
    GlobalCurrency("ZMW", "ZK", "Zambian Kwacha", "Zambia", "🇿🇲", CurrencyRegion.AFRICA, isPopular = false, sampleAmount = "40,000"),
    GlobalCurrency("BWP", "P", "Botswana Pula", "Botswana", "🇧🇼", CurrencyRegion.AFRICA, isPopular = false, sampleAmount = "20,500"),
    GlobalCurrency("MUR", "Rs", "Mauritian Rupee", "Mauritius", "🇲🇺", CurrencyRegion.AFRICA, isPopular = false, sampleAmount = "68,000"),
    GlobalCurrency("MAD", "DH", "Moroccan Dirham", "Morocco", "🇲🇦", CurrencyRegion.AFRICA, isPopular = false, sampleAmount = "15,200"),
    GlobalCurrency("SOS", "Sh", "Somali Shilling", "Somalia", "🇸🇴", CurrencyRegion.AFRICA, isPopular = false, sampleAmount = "850,000"),
    GlobalCurrency("SSP", "£", "South Sudanese Pound", "South Sudan", "🇸🇸", CurrencyRegion.AFRICA, isPopular = false, sampleAmount = "1,900,000"),
    GlobalCurrency("BDI", "FBu", "Burundian Franc", "Burundi", "🇧🇮", CurrencyRegion.AFRICA, isPopular = false, sampleAmount = "4,300,000"),
    GlobalCurrency("MZN", "MT", "Mozambican Metical", "Mozambique", "🇲🇿", CurrencyRegion.AFRICA, isPopular = false, sampleAmount = "96,000"),
    GlobalCurrency("AOA", "Kz", "Angolan Kwanza", "Angola", "🇦🇴", CurrencyRegion.AFRICA, isPopular = false, sampleAmount = "1,250,000"),
    GlobalCurrency("DZD", "DA", "Algerian Dinar", "Algeria", "🇩🇿", CurrencyRegion.AFRICA, isPopular = false, sampleAmount = "200,000"),
    GlobalCurrency("TND", "DT", "Tunisian Dinar", "Tunisia", "🇹🇳", CurrencyRegion.AFRICA, isPopular = false, sampleAmount = "4,600"),
    GlobalCurrency("CDF", "FC", "Congolese Franc", "DR Congo", "🇨🇩", CurrencyRegion.AFRICA, isPopular = false, sampleAmount = "4,200,000"),

    // Americas
    GlobalCurrency("CAD", "CA$", "Canadian Dollar", "Canada", "🇨🇦", CurrencyRegion.AMERICAS, isPopular = true, sampleAmount = "2,050.00"),
    GlobalCurrency("BRL", "R$", "Brazilian Real", "Brazil", "🇧🇷", CurrencyRegion.AMERICAS, isPopular = true, sampleAmount = "8,500.00"),
    GlobalCurrency("MXN", "Mex$", "Mexican Peso", "Mexico", "🇲🇽", CurrencyRegion.AMERICAS, isPopular = true, sampleAmount = "30,000.00"),
    GlobalCurrency("ARS", "$", "Argentine Peso", "Argentina", "🇦🇷", CurrencyRegion.AMERICAS, isPopular = false, sampleAmount = "1,500,000"),
    GlobalCurrency("CLP", "CLP$", "Chilean Peso", "Chile", "🇨🇱", CurrencyRegion.AMERICAS, isPopular = false, sampleAmount = "1,400,000"),
    GlobalCurrency("COP", "COL$", "Colombian Peso", "Colombia", "🇨🇴", CurrencyRegion.AMERICAS, isPopular = false, sampleAmount = "6,200,000"),
    GlobalCurrency("PEN", "S/", "Peruvian Sol", "Peru", "🇵🇪", CurrencyRegion.AMERICAS, isPopular = false, sampleAmount = "5,600.00"),
    GlobalCurrency("JMD", "J$", "Jamaican Dollar", "Jamaica", "🇯🇲", CurrencyRegion.AMERICAS, isPopular = false, sampleAmount = "230,000"),
    GlobalCurrency("TTD", "TT$", "Trinidad & Tobago Dollar", "Trinidad & Tobago", "🇹🇹", CurrencyRegion.AMERICAS, isPopular = false, sampleAmount = "10,200"),
    GlobalCurrency("CRC", "₡", "Costa Rican Colón", "Costa Rica", "🇨🇷", CurrencyRegion.AMERICAS, isPopular = false, sampleAmount = "780,000"),
    GlobalCurrency("DOP", "RD$", "Dominican Peso", "Dominican Republic", "🇩🇴", CurrencyRegion.AMERICAS, isPopular = false, sampleAmount = "90,000"),
    GlobalCurrency("GTQ", "Q", "Guatemalan Quetzal", "Guatemala", "🇬🇹", CurrencyRegion.AMERICAS, isPopular = false, sampleAmount = "11,600"),
    GlobalCurrency("UYU", "UY$", "Uruguayan Peso", "Uruguay", "🇺🇾", CurrencyRegion.AMERICAS, isPopular = false, sampleAmount = "62,000"),

    // Europe
    GlobalCurrency("CHF", "CHF", "Swiss Franc", "Switzerland", "🇨🇭", CurrencyRegion.EUROPE, isPopular = true, sampleAmount = "1,350.00"),
    GlobalCurrency("SEK", "kr", "Swedish Krona", "Sweden", "🇸🇪", CurrencyRegion.EUROPE, isPopular = false, sampleAmount = "16,000"),
    GlobalCurrency("NOK", "kr", "Norwegian Krone", "Norway", "🇳🇴", CurrencyRegion.EUROPE, isPopular = false, sampleAmount = "16,500"),
    GlobalCurrency("DKK", "kr.", "Danish Krone", "Denmark", "🇩🇰", CurrencyRegion.EUROPE, isPopular = false, sampleAmount = "10,400"),
    GlobalCurrency("PLN", "zł", "Polish Zloty", "Poland", "🇵🇱", CurrencyRegion.EUROPE, isPopular = false, sampleAmount = "6,100"),
    GlobalCurrency("CZK", "Kč", "Czech Koruna", "Czech Republic", "🇨🇿", CurrencyRegion.EUROPE, isPopular = false, sampleAmount = "35,000"),
    GlobalCurrency("HUF", "Ft", "Hungarian Forint", "Hungary", "🇭🇺", CurrencyRegion.EUROPE, isPopular = false, sampleAmount = "560,000"),
    GlobalCurrency("RON", "lei", "Romanian Leu", "Romania", "🇷🇴", CurrencyRegion.EUROPE, isPopular = false, sampleAmount = "7,000"),
    GlobalCurrency("TRY", "₺", "Turkish Lira", "Turkey", "🇹🇷", CurrencyRegion.EUROPE, isPopular = true, sampleAmount = "52,000"),
    GlobalCurrency("ISK", "kr", "Icelandic Króna", "Iceland", "🇮🇸", CurrencyRegion.EUROPE, isPopular = false, sampleAmount = "210,000"),

    // Asia & Middle East
    GlobalCurrency("JPY", "¥", "Japanese Yen", "Japan", "🇯🇵", CurrencyRegion.ASIA_ME, isPopular = true, sampleAmount = "230,000"),
    GlobalCurrency("CNY", "¥", "Chinese Yuan", "China", "🇨🇳", CurrencyRegion.ASIA_ME, isPopular = true, sampleAmount = "10,800"),
    GlobalCurrency("INR", "₹", "Indian Rupee", "India", "🇮🇳", CurrencyRegion.ASIA_ME, isPopular = true, sampleAmount = "125,000"),
    GlobalCurrency("AED", "AED", "UAE Dirham", "United Arab Emirates", "🇦🇪", CurrencyRegion.ASIA_ME, isPopular = true, sampleAmount = "5,500"),
    GlobalCurrency("SAR", "SR", "Saudi Riyal", "Saudi Arabia", "🇸🇦", CurrencyRegion.ASIA_ME, isPopular = true, sampleAmount = "5,600"),
    GlobalCurrency("QAR", "QR", "Qatari Riyal", "Qatar", "🇶🇦", CurrencyRegion.ASIA_ME, isPopular = false, sampleAmount = "5,450"),
    GlobalCurrency("SGD", "S$", "Singapore Dollar", "Singapore", "🇸🇬", CurrencyRegion.ASIA_ME, isPopular = true, sampleAmount = "2,000"),
    GlobalCurrency("HKD", "HK$", "Hong Kong Dollar", "Hong Kong", "🇭🇰", CurrencyRegion.ASIA_ME, isPopular = true, sampleAmount = "11,700"),
    GlobalCurrency("KRW", "₩", "South Korean Won", "South Korea", "🇰🇷", CurrencyRegion.ASIA_ME, isPopular = true, sampleAmount = "2,000,000"),
    GlobalCurrency("MYR", "RM", "Malaysian Ringgit", "Malaysia", "🇲🇾", CurrencyRegion.ASIA_ME, isPopular = false, sampleAmount = "6,800"),
    GlobalCurrency("THB", "฿", "Thai Baht", "Thailand", "🇹🇭", CurrencyRegion.ASIA_ME, isPopular = false, sampleAmount = "53,000"),
    GlobalCurrency("IDR", "Rp", "Indonesian Rupiah", "Indonesia", "🇮🇩", CurrencyRegion.ASIA_ME, isPopular = false, sampleAmount = "24,000,000"),
    GlobalCurrency("PHP", "₱", "Philippine Peso", "Philippines", "🇵🇭", CurrencyRegion.ASIA_ME, isPopular = false, sampleAmount = "87,000"),
    GlobalCurrency("VND", "₫", "Vietnamese Dong", "Vietnam", "🇻🇳", CurrencyRegion.ASIA_ME, isPopular = false, sampleAmount = "38,000,000"),
    GlobalCurrency("PKR", "Rs", "Pakistani Rupee", "Pakistan", "🇵🇰", CurrencyRegion.ASIA_ME, isPopular = false, sampleAmount = "420,000"),
    GlobalCurrency("BDT", "৳", "Bangladeshi Taka", "Bangladesh", "🇧🇩", CurrencyRegion.ASIA_ME, isPopular = false, sampleAmount = "175,000"),
    GlobalCurrency("LKR", "Rs", "Sri Lankan Rupee", "Sri Lanka", "🇱🇰", CurrencyRegion.ASIA_ME, isPopular = false, sampleAmount = "450,000"),
    GlobalCurrency("ILS", "₪", "Israeli New Shekel", "Israel", "🇮🇱", CurrencyRegion.ASIA_ME, isPopular = false, sampleAmount = "5,600"),
    GlobalCurrency("KWD", "KD", "Kuwaiti Dinar", "Kuwait", "🇰🇼", CurrencyRegion.ASIA_ME, isPopular = false, sampleAmount = "460.00"),
    GlobalCurrency("BHD", "BD", "Bahraini Dinar", "Bahrain", "🇧🇭", CurrencyRegion.ASIA_ME, isPopular = false, sampleAmount = "565.00"),
    GlobalCurrency("OMR", "OMR", "Omani Rial", "Oman", "🇴🇲", CurrencyRegion.ASIA_ME, isPopular = false, sampleAmount = "580.00"),
    GlobalCurrency("JOD", "JD", "Jordanian Dinar", "Jordan", "🇯🇴", CurrencyRegion.ASIA_ME, isPopular = false, sampleAmount = "1,060"),
    GlobalCurrency("KZT", "₸", "Kazakhstani Tenge", "Kazakhstan", "🇰🇿", CurrencyRegion.ASIA_ME, isPopular = false, sampleAmount = "730,000"),

    // Oceania
    GlobalCurrency("AUD", "A$", "Australian Dollar", "Australia", "🇦🇺", CurrencyRegion.OCEANIA, isPopular = true, sampleAmount = "2,300.00"),
    GlobalCurrency("NZD", "NZ$", "New Zealand Dollar", "New Zealand", "🇳🇿", CurrencyRegion.OCEANIA, isPopular = true, sampleAmount = "2,500.00"),
    GlobalCurrency("FJD", "FJ$", "Fijian Dollar", "Fiji", "🇫🇯", CurrencyRegion.OCEANIA, isPopular = false, sampleAmount = "3,400.00"),
    GlobalCurrency("PGK", "K", "Papua New Guinean Kina", "Papua New Guinea", "🇵🇬", CurrencyRegion.OCEANIA, isPopular = false, sampleAmount = "6,000.00")
  )

  fun findByCode(code: String): GlobalCurrency {
    return allCurrencies.firstOrNull { it.code.equals(code, ignoreCase = true) }
      ?: allCurrencies.first { it.code == "KES" }
  }
}
