package com.klemfner.whoscalling.fake

import com.klemfner.whoscalling.data.remote.IncomingCallDataSource
import com.klemfner.whoscalling.domain.model.IncomingCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeIncomingCallDataSource : IncomingCallDataSource {
    private val incomingCall = MutableStateFlow<IncomingCall?>(null)

    fun emit(call: IncomingCall?) {
        incomingCall.value = call
    }

    override fun observeIncomingCall(): Flow<IncomingCall?> = incomingCall
}
