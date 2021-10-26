// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File
import java.io.FileNotFoundException

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    var input by remember { mutableStateOf("") }
    var oldName by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val logTxt = remember { mutableStateOf(mutableListOf<String>()) }

    val focusRequesters: List<FocusRequester> = remember {
        (0 until 3).map { FocusRequester() }
    }

    val onCommit: () -> Unit = {
        if (input.isBlank()) {
            error = "请输入路径"
        } else if (newName.isBlank()) {
            error = "请输入新文件名"
        } else {
            error = ""
            message = ""
            try {
                val result = rename(input, newName, oldName)
                logTxt.value = result.logTxt
                if (result.findFile) {
                    message = "操作完成"
                } else {
                    error = "没有找到原文件"
                }
            } catch (e: Exception) {
                error = e.localizedMessage
            }
        }
    }

    DesktopMaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                modifier = Modifier.focusRequester(focusRequesters[0])
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyUp) {
                            if (keyEvent.key == Key.Enter) {
                                focusRequesters[0].freeFocus()
                                focusRequesters[1].requestFocus()
                                return@onKeyEvent true
                            }
                        }
                        false
                    },
                value = input,
                onValueChange = {
                    input = it
                    error = ""
                    message = ""
                },
                maxLines = 1,
                placeholder = { Text(text = "请输入路径", color = Color.Gray, fontSize = 18.sp) },
                textStyle = TextStyle(color = Color.Black, fontSize = 18.sp),

                )
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                modifier = Modifier.focusRequester(focusRequesters[1]).onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyUp) {
                        if (keyEvent.key == Key.Enter) {
                            focusRequesters[1].freeFocus()
                            focusRequesters[2].requestFocus()
                            return@onKeyEvent true
                        }
                    }
                    false
                },
                value = oldName,
                onValueChange = {
                    oldName = it
                    error = ""
                    message = ""
                },
                maxLines = 1, placeholder = { Text(text = "原文件名(置空则取第一个文件)", color = Color.Gray, fontSize = 18.sp) },
                textStyle = TextStyle(color = Color.Black, fontSize = 18.sp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                modifier = Modifier.focusRequester(focusRequesters[2]).onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyUp) {
                        if (keyEvent.key == Key.Enter) {
                            focusRequesters[2].freeFocus()
                            onCommit.invoke()
                            return@onKeyEvent true
                        }
                    }
                    false
                },
                value = newName,
                onValueChange = {
                    newName = it
                    error = ""
                    message = ""
                },
                maxLines = 1, placeholder = { Text(text = "请输入新文件名", color = Color.Gray, fontSize = 18.sp) },
                textStyle = TextStyle(color = Color.Black, fontSize = 18.sp),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onCommit) {
                Text(text = "确定")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = message, color = Color.Green, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(text = error, color = Color.Red, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                itemsIndexed(logTxt.value, itemContent = { _, item ->
                    Text(text = item)
                    Spacer(modifier = Modifier.height(2.dp))
                })
            }
        }
    }
}


private fun rename(
    path: String,
    newName: String,
    oldName: String
): Result {
    val logTxt: MutableList<String> = mutableListOf()
    val dir = File(path)
    var findFile = false
    if (dir.exists()) {
        val listFiles = dir.listFiles()
        listFiles?.forEach { file ->
            if (!file.isDirectory) {
                if (oldName.isNotBlank()) {
                    if (oldName == file.name) {
                        findFile = true
                        doRename(logTxt, file, newName)
                    }
                } else {
                    findFile = true
                    doRename(logTxt, file, newName)
                }
            } else {
                val result = rename(file.path, newName, oldName)
                logTxt.addAll(result.logTxt)
                findFile = result.findFile
            }
        }
    } else {
        throw FileNotFoundException("输入的路径有误")
    }
    return Result(logTxt, findFile)
}

private fun doRename(
    logTxt: MutableList<String>,
    file: File,
    newName: String
) {
    logTxt.add("找到${file.path}")
    val newFile = File(file.parent, newName)
    if (!newFile.exists()) {//存在就不要再重命名了，避免图片被覆盖
        file.renameTo(newFile)
        logTxt.add("已修改为${newFile.path}")
    }
}


data class Result(val logTxt: MutableList<String>, val findFile: Boolean)

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication, title = "重命名Drawable",
        icon = painterResource(resourcePath = "icon_sign.png")
    ) {
        App()
    }
}
