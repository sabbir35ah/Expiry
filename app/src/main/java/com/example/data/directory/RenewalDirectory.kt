package com.example.data.directory

data class RenewalResource(
    val title: String,
    val region: String,
    val categoryName: String,
    val officialUrl: String,
    val description: String,
    val estimatedProcessingTime: String,
    val requiredDocumentsSummary: String,
    val antiPhishingTip: String
)

object RenewalDirectory {

    val verifiedResources = listOf(
        RenewalResource(
            title = "U.S. Passport Renewal (DS-82)",
            region = "United States",
            categoryName = "Passport",
            officialUrl = "https://travel.state.gov/content/travel/en/passports.html",
            description = "Official US Department of State portal for adult passport renewals and status tracking.",
            estimatedProcessingTime = "6 to 8 weeks (Routine), 2 to 3 weeks (Expedited)",
            requiredDocumentsSummary = "Current passport book, 2x2 color photo, DS-82 form, fee check/online pay.",
            antiPhishingTip = "Always ensure web address ends in .gov. Never pay third-party 'expediter' fees without verifying official fees first."
        ),
        RenewalResource(
            title = "UK HM Passport Office (HMPO)",
            region = "United Kingdom",
            categoryName = "Passport",
            officialUrl = "https://www.gov.uk/renew-adult-passport",
            description = "Official UK Government service to renew British passports online with digital photo upload.",
            estimatedProcessingTime = "3 weeks (Standard), 1 week (Fast Track)",
            requiredDocumentsSummary = "Old passport, digital passport photo code, credit/debit card.",
            antiPhishingTip = "Use only GOV.UK. Avoid unofficial sites that charge extra for filling standard forms."
        ),
        RenewalResource(
            title = "Schengen Visa & European Borders",
            region = "European Union",
            categoryName = "Visa & Residence",
            officialUrl = "https://home-affairs.ec.europa.eu/policies/schengen-borders-and-visa_en",
            description = "Official European Commission migration and visa directives for 29 Schengen member states.",
            estimatedProcessingTime = "15 to 45 calendar days",
            requiredDocumentsSummary = "Valid passport (valid 3+ months past departure), travel insurance (€30k min), flight itinerary.",
            antiPhishingTip = "Book appointments directly through the official embassy or accredited provider (VFS / TLScontact)."
        ),
        RenewalResource(
            title = "California DMV Driver License & REAL ID",
            region = "United States (CA)",
            categoryName = "Driving License",
            officialUrl = "https://www.dmv.ca.gov/portal/driver-licenses-id-cards/",
            description = "Online renewal, REAL ID document pre-check, and field office appointments.",
            estimatedProcessingTime = "2 to 3 weeks delivery by mail",
            requiredDocumentsSummary = "SSN, 2 proofs of residency, identity proof for REAL ID gold star.",
            antiPhishingTip = "Beware of text messages demanding fee payment for vehicle/license suspension."
        ),
        RenewalResource(
            title = "New York DMV Online Renewal",
            region = "United States (NY)",
            categoryName = "Driving License",
            officialUrl = "https://dmv.ny.gov/driver-license/renew-license",
            description = "New York State Department of Motor Vehicles license, permit, and ID card renewals.",
            estimatedProcessingTime = "2 weeks delivery",
            requiredDocumentsSummary = "Vision test result (online registry or doctor note), current license ID.",
            antiPhishingTip = "Check for dmv.ny.gov domain."
        ),
        RenewalResource(
            title = "U.S. Trusted Traveler (Global Entry / TSA PreCheck)",
            region = "United States",
            categoryName = "Travel Program",
            officialUrl = "https://ttp.dhs.gov/",
            description = "Department of Homeland Security Trusted Traveler Programs dashboard for expedited border clearance.",
            estimatedProcessingTime = "Renewal can be submitted 1 year prior to expiry; grace period up to 24 months if submitted before expiry.",
            requiredDocumentsSummary = "PASSID / KTN number, updated passport info, background update.",
            antiPhishingTip = "Only access via ttp.dhs.gov with Login.gov credentials."
        ),
        RenewalResource(
            title = "AppleCare+ & Apple Warranty Coverage",
            region = "Global",
            categoryName = "Product Warranty",
            officialUrl = "https://checkcoverage.apple.com/",
            description = "Official serial number lookup for iPhone, Mac, iPad, and Apple Watch warranty and AppleCare+ expiration.",
            estimatedProcessingTime = "Instant lookup",
            requiredDocumentsSummary = "Hardware Serial Number (Settings > General > About).",
            antiPhishingTip = "Never enter your Apple ID password on third-party warranty verification websites."
        ),
        RenewalResource(
            title = "HealthCare.gov Open Enrollment",
            region = "United States",
            categoryName = "Insurance Policy",
            officialUrl = "https://www.healthcare.gov",
            description = "Official US Health Insurance Marketplace to renew or change coverage during open enrollment.",
            estimatedProcessingTime = "Open enrollment Nov 1 - Jan 15 (effective Jan 1 or Feb 1)",
            requiredDocumentsSummary = "Income estimates (W2/1099), household tax filings, existing policy ID.",
            antiPhishingTip = "Only use healthcare.gov or your designated state exchange."
        )
    )
}
