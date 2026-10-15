package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class ExpiryStatus {
    EXPIRED,
    CRITICAL,    // <= 30 days
    UPCOMING,    // 31 - 90 days
    ACTIVE       // > 90 days
}

enum class DocumentCategory(
    val key: String,
    val displayName: String,
    val color: Color,
    val renewalLeadTimeNotice: String,
    val suggestedOfficialUrl: String,
    val checklist: List<String>
) {
    PASSPORT(
        key = "passport",
        displayName = "Passport",
        color = CatPassport,
        renewalLeadTimeNotice = "Many destinations enforce the '6-Month Rule' (must have at least 6 months validity from departure).",
        suggestedOfficialUrl = "https://travel.state.gov/content/travel/en/passports.html",
        checklist = listOf(
            "Check country entry validity requirements (3 or 6 months rule)",
            "Get official 2x2 inch passport photos (no glasses/filters)",
            "Locate current/expired physical passport book",
            "Fill DS-82 renewal form or local passport agency online portal",
            "Pay government renewal & expedite fee if traveling soon"
        )
    ),
    VISA(
        key = "visa",
        displayName = "Visa & Residence",
        color = CatVisa,
        renewalLeadTimeNotice = "Visa processing often takes 4 to 8 weeks. Start renewal before current legal stay expires.",
        suggestedOfficialUrl = "https://home-affairs.ec.europa.eu/policies/schengen-borders-and-visa_en",
        checklist = listOf(
            "Verify remaining days on single vs multi-entry limit",
            "Obtain proof of accommodation and sufficient funds",
            "Book consulate or visa application center appointment early",
            "Check valid travel insurance coverage length"
        )
    ),
    DRIVING_LICENSE(
        key = "driving_license",
        displayName = "Driving License",
        color = CatLicense,
        renewalLeadTimeNotice = "Driving with an expired license can result in heavy fines and insurance invalidation.",
        suggestedOfficialUrl = "https://www.dmv.ca.gov/portal/driver-licenses-id-cards/",
        checklist = listOf(
            "Check if eligible for online renewal vs vision test requirement",
            "Verify home address on file",
            "Upgrade to REAL ID if domestic flight travel required",
            "Retain temporary paper permit while plastic card ships"
        )
    ),
    NATIONAL_ID(
        key = "national_id",
        displayName = "National ID / Card",
        color = CatNationalId,
        renewalLeadTimeNotice = "Required for domestic banking, tax filings, and biometric identity checks.",
        suggestedOfficialUrl = "https://www.usa.gov/renew-id",
        checklist = listOf(
            "Bring 2 proof of address documents (utility bills, lease)",
            "Bring existing identity document or birth certificate",
            "Complete municipal or state government appointment"
        )
    ),
    INSURANCE(
        key = "insurance",
        displayName = "Insurance Policy",
        color = CatInsurance,
        renewalLeadTimeNotice = "Auto, health, or home policies may cancel coverage if lapse in payment or renewal occurs.",
        suggestedOfficialUrl = "https://www.healthcare.gov",
        checklist = listOf(
            "Compare rate changes and updated deductible options",
            "Ensure auto-pay payment card on file has not expired",
            "Download updated digital proof of insurance card to wallet"
        )
    ),
    CERTIFICATION(
        key = "certification",
        displayName = "Professional Cert",
        color = CatCert,
        renewalLeadTimeNotice = "Continuing education units (CEU) must often be submitted 30 days prior to expiry.",
        suggestedOfficialUrl = "",
        checklist = listOf(
            "Confirm completed continuing education credits / CPE hours",
            "Submit renewal log to accrediting board",
            "Pay recertification assessment fee"
        )
    ),
    WARRANTY(
        key = "warranty",
        displayName = "Product Warranty",
        color = CatWarranty,
        renewalLeadTimeNotice = "File any pending defect claims before limited manufacturer warranty terminates.",
        suggestedOfficialUrl = "https://checkcoverage.apple.com/",
        checklist = listOf(
            "Inspect device/appliance for hardware faults or battery wear",
            "Keep original purchase receipt / invoice uploaded",
            "Schedule authorized service before coverage expiration"
        )
    ),
    VEHICLE(
        key = "vehicle",
        displayName = "Vehicle Reg / Inspection",
        color = CatVehicle,
        renewalLeadTimeNotice = "Vehicle registration and emissions smog check must be completed before month-end.",
        suggestedOfficialUrl = "https://dmv.ny.gov/registration/how-renew-registration",
        checklist = listOf(
            "Complete mandatory emissions/smog inspection",
            "Verify active vehicle liability insurance on record",
            "Pay county registration fee and apply new windshield sticker"
        )
    ),
    CONTRACT(
        key = "contract",
        displayName = "Lease / Contract",
        color = CatContract,
        renewalLeadTimeNotice = "Most tenancy leases require 30 to 60 days written notice to renew or terminate.",
        suggestedOfficialUrl = "",
        checklist = listOf(
            "Check required notice window in agreement",
            "Send written notice to landlord or vendor",
            "Request updated terms or rate negotiations"
        )
    ),
    MEMBERSHIP(
        key = "membership",
        displayName = "Membership / Club",
        color = CatOther,
        renewalLeadTimeNotice = "Verify renewal discounts or cancel prior to automatic recurring charge.",
        suggestedOfficialUrl = "",
        checklist = listOf(
            "Check auto-renewal status",
            "Verify membership perks & benefits utilized"
        )
    ),
    OTHER(
        key = "other",
        displayName = "Other Document",
        color = CatOther,
        renewalLeadTimeNotice = "Track deadlines and keep relevant records in one place.",
        suggestedOfficialUrl = "",
        checklist = listOf(
            "Review terms and renewal steps",
            "Keep relevant contact info handy"
        )
    );

    companion object {
        fun fromKey(key: String?): DocumentCategory {
            if (key.isNullOrBlank()) return OTHER
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) }
                ?: entries.firstOrNull { it.name.equals(key, ignoreCase = true) }
                ?: OTHER
        }
    }
}
