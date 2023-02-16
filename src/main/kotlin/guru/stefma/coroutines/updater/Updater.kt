package guru.stefma.coroutines.updater

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

interface Updater {
    suspend fun update()
}

internal class UpdateDiskAfterSomeDependencyCollectUpdater(
    private val someDependency: SomeDependency,
    private val diskUpdater: DiskUpdater
) : Updater {
    override suspend fun update() {
        someDependency.aflow
            .onEach { diskUpdater.writeToDisk(it) }
            .collect()
    }
}

interface SomeDependency {
    val aflow: Flow<String>
}

interface DiskUpdater {
    fun writeToDisk(textToWrite: String)
}