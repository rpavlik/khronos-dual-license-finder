/*
 * Copyright 2021, Collabora, Ltd.
 *
 * SPDX-License-Identifier: BSL-1.0
 */
package org.khronos.spdx.dual_license_finder

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.types.file
import org.spdx.library.ModelCopyManager
import org.spdx.library.SpdxConstants
import org.spdx.library.model.SpdxDocument
import org.spdx.library.model.SpdxFile
import org.spdx.library.model.license.AnyLicenseInfo
import org.spdx.library.model.license.LicenseInfoFactory
import org.spdx.library.model.license.SpdxListedLicense
import org.spdx.storage.ISerializableModelStore
import org.spdx.storage.simple.InMemSpdxStore
import org.spdx.tagvaluestore.TagValueStore
import java.io.File


class Scanner : CliktCommand() {
    private val files: List<File> by argument(help = "Path to an SPDX Tag/Value data file like produced by reuse spdx")
            .file(mustExist = true)
            .multiple(required = true)
    private val asl2: SpdxListedLicense = LicenseInfoFactory.getListedLicenseById("Apache-2.0")
    private val mit: SpdxListedLicense = LicenseInfoFactory.getListedLicenseById("MIT")

    private val copyManager: ModelCopyManager = ModelCopyManager()
    private val store: ISerializableModelStore = TagValueStore(InMemSpdxStore())

    data class ParsedDoc(val documentUri: String, val spdxDocument: SpdxDocument)

    private fun checkLicenseSet(licenseInfo: Collection<AnyLicenseInfo>) = licenseInfo.size == 2
            && licenseInfo.any { it.equivalent(asl2) }
            && licenseInfo.any { it.equivalent(mit) }

    private fun parseDoc(infile: File): ParsedDoc {
        val documentUri = infile.inputStream().use {
            return@use store.deSerialize(it, false)
        }
        val doc = SpdxDocument(store, documentUri, copyManager, false)
        doc.verify().let {
            if (it.isNotEmpty()) {
                println("Failed verification: $it")
                throw RuntimeException("Verification error")
            }
        }
        return ParsedDoc(documentUri, doc)
    }

    fun process(infile: File) {

        with(parseDoc(infile)) {
            spdxDocument.documentDescribes
                    .filter { it.type == SpdxConstants.CLASS_SPDX_FILE }
                    .map { it as SpdxFile }
                    .filter {
                        val licenseInfo = it.licenseInfoFromFiles
                        return@filter checkLicenseSet(licenseInfo)
                                && it.copyrightText.contains("Khronos")
                    }
                    .forEach {
                        println(it.name.get().replace("\\", "/"))
                    }
        }
    }

    /**
     * Perform actions after parsing is complete and this command is invoked.
     *
     * This is called after command line parsing is complete. If this command is a subcommand, this will only
     * be called if the subcommand is invoked.
     *
     * If one of this command's subcommands is invoked, this is called before the subcommand's arguments are
     * parsed.
     */
    override fun run() {
        val showFilename = files.size > 1
        files.forEach {
            if (showFilename)
                println(it.name)
            process(it)
        }
    }

}

fun main(args: Array<String>) = Scanner().main(args)
//    app.load("e:/src-ssd/openxr-maintainer-scripts/OpenXR-SDK-Source/OpenXR-SDK-Source.spdx")
