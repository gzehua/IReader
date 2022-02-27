package org.ireader.core_ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import tachiyomi.core.prefs.Preference

class ColorPreference(
    private val preference: Preference<Int>,
) : Preference<Color> {

    override fun key(): String {
        return preference.key()
    }

    override fun get(): Color {
        return if (isSet()) {
            Color(preference.get())
        } else {
            Color.Unspecified
        }
    }

    override fun set(value: Color) {
        if (value != Color.Unspecified) {
            preference.set(value.toArgb())
        } else {
            preference.delete()
        }
    }

    override fun isSet(): Boolean {
        return preference.isSet()
    }

    override fun delete() {
        preference.delete()
    }

    override fun defaultValue(): Color {
        return Color.Unspecified
    }

    override fun changes(): Flow<Color> {
        return preference.changes()
            .map { get() }
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<Color> {
        return preference.changes().map { get() }.stateIn(scope, SharingStarted.Eagerly, get())
    }

}

fun Preference<Int>.asColor(): ColorPreference {
    return ColorPreference(this)
}
