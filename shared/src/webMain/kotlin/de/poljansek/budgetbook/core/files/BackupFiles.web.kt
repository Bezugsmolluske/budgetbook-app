package de.poljansek.budgetbook.core.files

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.js

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalEncodingApi::class, ExperimentalWasmJsInterop::class)
private fun triggerDownload(fileName: String, mime: String, base64: String) {
    js(
        """
        {
          var binary = atob(base64);
          var len = binary.length;
          var array = new Uint8Array(len);
          for (var i = 0; i < len; i++) array[i] = binary.charCodeAt(i);
          var blob = new Blob([array], { type: mime });
          var url = URL.createObjectURL(blob);
          var a = document.createElement('a');
          a.href = url;
          a.download = fileName;
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
          URL.revokeObjectURL(url);
        }
        """
    )
}

@OptIn(ExperimentalEncodingApi::class, ExperimentalWasmJsInterop::class)
actual suspend fun saveBackupFile(bytes: ByteArray, suggestedName: String, extension: String) {
    val fileName = "$suggestedName.$extension"
    val mime = if (extension == "json") "application/json" else "text/csv"
    triggerDownload(fileName, mime, Base64.encode(bytes))
}

actual suspend fun pickBackupFile(extension: String): PickedFile? {
    // FileKit dialogs are not available on Wasm; restore from Settings on desktop/mobile.
    return null
}
