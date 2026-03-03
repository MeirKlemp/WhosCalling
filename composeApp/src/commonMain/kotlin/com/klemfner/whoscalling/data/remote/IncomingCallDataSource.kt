package com.klemfner.whoscalling.data.remote

import com.klemfner.whoscalling.domain.model.IncomingCall
import kotlinx.coroutines.flow.Flow

interface IncomingCallDataSource {
    fun observeIncomingCall(): Flow<IncomingCall?>
}
