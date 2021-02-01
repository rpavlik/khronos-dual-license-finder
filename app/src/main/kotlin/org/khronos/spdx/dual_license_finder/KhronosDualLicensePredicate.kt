/*
 * Copyright 2021, Collabora, Ltd.
 *
 * SPDX-License-Identifier: BSL-1.0
 */

package org.khronos.spdx.dual_license_finder

import org.spdx.library.model.SpdxFile
import org.spdx.library.model.license.AnyLicenseInfo
import org.spdx.library.model.license.LicenseInfoFactory
import org.spdx.library.model.license.SpdxListedLicense

class KhronosDualLicensePredicate : IFilePredicate {
    private val asl2: SpdxListedLicense = LicenseInfoFactory.getListedLicenseById("Apache-2.0")
    private val mit: SpdxListedLicense = LicenseInfoFactory.getListedLicenseById("MIT")
    private val requiredCopyrightText = "Khronos"

    private fun checkLicenseSet(licenseInfo: Collection<AnyLicenseInfo>) = licenseInfo.size == 2
            && licenseInfo.any { it.equivalent(asl2) }
            && licenseInfo.any { it.equivalent(mit) }

    /**
     * Describe this predicate for a human reader
     */
    override fun describe(): String = "licensed '${asl2.licenseId} OR ${mit.licenseId}', with $requiredCopyrightText mentioned in the copyright text"

    /**
     * Return true if this predicate is satisfied
     */
    override fun matches(file: SpdxFile): Boolean = checkLicenseSet(file.licenseInfoFromFiles) && file.copyrightText.contains(requiredCopyrightText)

}