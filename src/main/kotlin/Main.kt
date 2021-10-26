// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File
import java.io.FileNotFoundException

@Composable
@Preview
fun App() {
    var input by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    DesktopMaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(value = input, onValueChange = {
                input = it
                error = ""
                message = ""
            }, placeholder = { Text(text = "请输入路径", color = Color.Gray, fontSize = 18.sp) },
                textStyle = TextStyle(color = Color.Black, fontSize = 18.sp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextField(value = newName, onValueChange = {
                newName = it
                error = ""
                message = ""
            }, placeholder = { Text(text = "请输入新文件名", color = Color.Gray, fontSize = 18.sp) },
                textStyle = TextStyle(color = Color.Black, fontSize = 18.sp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                if (input.isBlank()) {
                    error = "请输入路径"
                    return@Button
                }
                if (newName.isBlank()) {
                    error = "请输入新文件名"
                    return@Button
                }
                error = ""
                message = ""
                try {
                    rename(input, newName)
                    message = "操作完成"
                } catch (e: Exception) {
                    error = e.localizedMessage
                }
            }) {
                Text(text = "确定")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = message, color = Color.Green, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(text = error, color = Color.Red, fontSize = 15.sp)
        }
    }
}

private fun rename(path: String, newName: String): MutableList<String> {
    val nameList: MutableList<String> = mutableListOf()
    val dir = File(path)
    if (dir.exists()) {
        val listFiles = dir.listFiles()
        listFiles?.forEach { file ->
            if (!file.isDirectory) {
                nameList.add(file.name)
                file.renameTo(File(file.parent, newName))
            } else {
                nameList.addAll(rename(file.path, newName))
            }
        }
    } else {
        throw FileNotFoundException("输入的路径有误")
    }
    return nameList
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication, title = "重命名Drawable",
        icon = painterResource(resourcePath = "icon_sign.png")
    ) {
        App()
    }
}
