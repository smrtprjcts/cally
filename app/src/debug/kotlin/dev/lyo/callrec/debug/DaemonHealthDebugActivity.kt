// SPDX-License-Identifier: GPL-3.0-or-later
package dev.lyo.callrec.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.lyo.callrec.ui.theme.CallrecTheme

class DaemonHealthDebugActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CallrecTheme { DaemonHealthDebugScreen() } }
    }
}
