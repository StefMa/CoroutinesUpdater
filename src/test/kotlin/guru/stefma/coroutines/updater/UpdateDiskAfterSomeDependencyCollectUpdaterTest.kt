package guru.stefma.coroutines.updater

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalCoroutinesApi::class)
internal class UpdateDiskAfterSomeDependencyCollectUpdaterTest {

    private var someDependencyUpdate = MutableSharedFlow<String>()

    private val fakeSomeDependency = object : SomeDependency {
        override val aflow: Flow<String> = someDependencyUpdate
    }

    private val fakeDiskUpdater = object : DiskUpdater {
        var writeToDiskCalled = false
        override fun writeToDisk(textToWrite: String) {
            writeToDiskCalled = true
        }
    }

    @Test
    internal fun `ENDLESS - after emit of some dependency diskUpdater should write to disk`() = runTest {
        val updater = UpdateDiskAfterSomeDependencyCollectUpdater(
            someDependency = fakeSomeDependency,
            diskUpdater = fakeDiskUpdater
        )

        updater.update()

        someDependencyUpdate.emit("Saves this please")

        assert(fakeDiskUpdater.writeToDiskCalled)
    }


    @Test
    internal fun `ENDLESS2 - after emit of some dependency diskUpdater should write to disk`() = runTest {
        val updater = UpdateDiskAfterSomeDependencyCollectUpdater(
            someDependency = fakeSomeDependency,
            diskUpdater = fakeDiskUpdater
        )

        launch {
            updater.update()
        }

        delay(1)
        someDependencyUpdate.emit("Saves this please")

        assert(fakeDiskUpdater.writeToDiskCalled)
    }


    @Test
    internal fun `ENDLESS3 - after emit of some dependency diskUpdater should write to disk`() = runTest {
        val updater = UpdateDiskAfterSomeDependencyCollectUpdater(
            someDependency = fakeSomeDependency,
            diskUpdater = fakeDiskUpdater
        )

        launch(Dispatchers.Unconfined) {
            updater.update()
        }

        someDependencyUpdate.emit("Saves this please")

        assert(fakeDiskUpdater.writeToDiskCalled)
    }

    @Test
    internal fun `FAILING - after emit of some dependency diskUpdater should write to disk`() = runTest {
        val updater = UpdateDiskAfterSomeDependencyCollectUpdater(
            someDependency = fakeSomeDependency,
            diskUpdater = fakeDiskUpdater
        )

        launch {
            updater.update()
        }

        someDependencyUpdate.emit("Saves this please")

        assert(fakeDiskUpdater.writeToDiskCalled)
    }

    @Test
    internal fun `WORKING - after emit of some dependency diskUpdater should write to disk`() = runTest {
        val updater = UpdateDiskAfterSomeDependencyCollectUpdater(
            someDependency = fakeSomeDependency,
            diskUpdater = fakeDiskUpdater
        )

        val updateJob = launch {
            updater.update()
        }

        delay(1)
        someDependencyUpdate.emit("Saves this please")
        updateJob.cancel()

        assert(fakeDiskUpdater.writeToDiskCalled)
    }

    @Test
    internal fun `WORKING2 - after emit of some dependency diskUpdater should write to disk`() = runTest {
        val updater = UpdateDiskAfterSomeDependencyCollectUpdater(
            someDependency = fakeSomeDependency,
            diskUpdater = fakeDiskUpdater
        )

        val updateJob = launch {
            updater.update()
        }

        val emitJob = launch {
            someDependencyUpdate.emit("Saves this please")
            updateJob.cancel()
        }

        emitJob.join()

        assert(fakeDiskUpdater.writeToDiskCalled)
    }

    @Test
    internal fun `WORKING3 - after emit of some dependency diskUpdater should write to disk`() = runTest {
        val updater = UpdateDiskAfterSomeDependencyCollectUpdater(
            someDependency = fakeSomeDependency,
            diskUpdater = fakeDiskUpdater
        )

        val job = launch(Dispatchers.Unconfined) {
            updater.update()
        }

        someDependencyUpdate.emit("Saves this please")
        job.cancel()

        assert(fakeDiskUpdater.writeToDiskCalled)
    }

    @Test
    internal fun `WORKING4 - after emit of some dependency diskUpdater should write to disk`() = runTest {
        val updater = UpdateDiskAfterSomeDependencyCollectUpdater(
            someDependency = fakeSomeDependency,
            diskUpdater = fakeDiskUpdater
        )

        val updateJob = launch {
            updater.update()
        }

        suspendCoroutine {
            thread {
                Thread.sleep(1)
                it.resume(Unit)
            }
        }
        someDependencyUpdate.emit("Saves this please")
        updateJob.cancel()

        assert(fakeDiskUpdater.writeToDiskCalled)
    }
}