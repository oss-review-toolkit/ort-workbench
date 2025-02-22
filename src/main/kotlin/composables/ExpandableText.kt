package org.ossreviewtoolkit.workbench.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material.RichText

@Composable
fun ExpandableText(text: String, unexpandedHeight: Dp = 20.dp, fontFamily: FontFamily? = null) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val modifier = if (expanded) Modifier.wrapContentHeight() else Modifier.height(unexpandedHeight)
    Row(modifier = modifier.animateContentSize()) {
        Text(text, modifier = Modifier.weight(1f), fontFamily = fontFamily)

        val image = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore
        Icon(image, "expand", modifier = Modifier.clickable { expanded = !expanded })
    }
}

@Composable
fun ExpandableMarkdown(text: String, unexpandedHeight: Dp = 20.dp) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val modifier = if (expanded) Modifier.wrapContentHeight() else Modifier.height(unexpandedHeight)
    Row(modifier = modifier.animateContentSize()) {
        RichText(modifier = Modifier.weight(1f)) {
            Markdown(text)
        }

        val image = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore
        Icon(image, "expand", modifier = Modifier.clickable { expanded = !expanded })
    }
}
