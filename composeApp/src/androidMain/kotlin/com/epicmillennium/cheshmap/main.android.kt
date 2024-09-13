package com.epicmillennium.cheshmap

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

lateinit var appContext: Context // Android specific context

// Android specific intent flow
@Suppress("ObjectPropertyName")
var _androidIntentFlow: MutableSharedFlow<Intent> = MutableSharedFlow()
val androidIntentFlow: SharedFlow<Intent> = _androidIntentFlow  // read-only shared flow received on Android side