package com.nxzef.wc.platform

// Returns (absolutePath, fileName, fileBytes) or null if cancelled
expect fun pickPdfFile(): Triple<String, String, ByteArray>?
