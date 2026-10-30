package dev.frameforge.engine

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import dev.frameforge.IShellService
import kotlinx.coroutines.suspendCancellableCoroutine
import rikka.shizuku.Shizuku
import kotlin.coroutines.resume

/** Binds a Shizuku UserService that executes commands as shell. UNTESTED: needs a device with Shizuku. */
class ShizukuShellRunner(private val ctx: Context) : ShellRunner {
    private var service: IShellService? = null
    private val args = Shizuku.UserServiceArgs(ComponentName(ctx.packageName, ShellService::class.java.name))
        .processNameSuffix("shell").daemon(false).version(1)

    private suspend fun ensure(): IShellService = service ?: suspendCancellableCoroutine { c ->
        Shizuku.bindUserService(args, object : ServiceConnection {
            override fun onServiceConnected(n: ComponentName, b: IBinder) { service = IShellService.Stub.asInterface(b); c.resume(service!!) }
            override fun onServiceDisconnected(n: ComponentName) { service = null }
        })
    }
    override suspend fun exec(command: String) = ensure().exec(command)
}

class ShellService : IShellService.Stub() {
    override fun destroy() { System.exit(0) }
    override fun exec(command: String): String {
        val p = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
        val out = p.inputStream.bufferedReader().readText(); p.waitFor(); return out
    }
}
