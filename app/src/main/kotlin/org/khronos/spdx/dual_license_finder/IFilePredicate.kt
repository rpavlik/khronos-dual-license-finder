/*
 * Copyright 2021, Collabora, Ltd.
 *
 * SPDX-License-Identifier: BSL-1.0
 */

package org.khronos.spdx.dual_license_finder

import org.spdx.library.model.SpdxFile

interface IFilePredicate {
    /**
     * Describe this predicate for a human reader.
     *
     * Please start with a lowercase letter, and do not include ending punctuation.
     */
    fun describe() : String

    /**
     * Return true if this predicate is satisfied
     */
    fun matches(file: SpdxFile) : Boolean
}