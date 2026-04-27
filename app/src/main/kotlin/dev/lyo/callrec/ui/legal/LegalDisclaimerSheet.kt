// SPDX-License-Identifier: GPL-3.0-or-later
package dev.lyo.callrec.ui.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.lyo.callrec.R
import kotlinx.coroutines.launch

/**
 * First-run legal placeholder, also reachable from Settings → "Юридичне
 * попередження" for a fresh re-read.
 *
 * Two modes via [requireAck]:
 *  - `true` (consent flow): blocks back-press, swipe-down and scrim-tap. Only
 *    the explicit "Зрозуміло" button can dismiss the sheet.
 *  - `false` (settings re-read): behaves like a normal informational sheet.
 *
 * Locking is implemented by a triple-defense: `confirmValueChange` rejects the
 * Hidden state, `shouldDismissOnBackPress = false`, and the drag handle is
 * removed so the affordance no longer hints "swipe to dismiss".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalDisclaimerSheet(
    requireAck: Boolean,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            if (requireAck) sheetValue != SheetValue.Hidden else true
        },
    )
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = if (requireAck) {
            { /* swallow scrim/system-driven dismissals while consent is pending */ }
        } else onDismiss,
        sheetState = sheetState,
        dragHandle = if (requireAck) null else {
            { BottomSheetDefaults.DragHandle() }
        },
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = !requireAck,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = if (requireAck) 16.dp else 0.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Gavel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.legal_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Text(
                stringResource(R.string.legal_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Bullet(stringResource(R.string.legal_bullet_ua))
            Bullet(stringResource(R.string.legal_bullet_other))
            Bullet(stringResource(R.string.legal_bullet_no_beep))
            Bullet(stringResource(R.string.legal_bullet_share))

            Text(
                stringResource(R.string.legal_footer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        sheetState.hide()
                        if (requireAck) onAccept() else onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(
                        if (requireAck) R.string.legal_action_accept
                        else R.string.legal_action_close,
                    ),
                )
            }
        }
    }
}

@Composable
private fun Bullet(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            "•",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
