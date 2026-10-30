package dev.frameforge.engine

/** Runs a shell command with shell-level privileges (via Shizuku). */
interface ShellRunner { suspend fun exec(command: String): String }
