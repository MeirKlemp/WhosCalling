package com.klemfner.whoscalling.data.remote

class DebugErrorLog {
    private val _errors = mutableListOf<String>()
    val errors: List<String> get() = synchronized(_errors) { _errors.toList() }

    fun log(throwable: Throwable) {
        synchronized(_errors) {
            _errors.add(throwable.stackTraceToString())
        }
    }

    fun clear() {
        synchronized(_errors) {
            _errors.clear()
        }
    }
}
