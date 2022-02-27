package org.ireader.domain.catalog.service

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.catalog.model.InstallStep
import org.ireader.domain.models.entities.CatalogRemote

interface CatalogInstaller {

    fun install(catalog: CatalogRemote): Flow<InstallStep>

    suspend fun uninstall(pkgName: String): Boolean

}
