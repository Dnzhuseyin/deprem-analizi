package com.example.depremapp.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.depremapp.data.DamageReportForm
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PdfGenerator(private val context: Context) {
    
    private val pageWidth = 595 // A4 width in points
    private val pageHeight = 842 // A4 height in points
    private val margin = 40f
    private var currentY = margin
    
    private val titlePaint = Paint().apply {
        color = Color.BLACK
        textSize = 20f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    
    private val headerPaint = Paint().apply {
        color = Color.BLACK
        textSize = 16f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    
    private val normalPaint = Paint().apply {
        color = Color.BLACK
        textSize = 12f
        isAntiAlias = true
    }
    
    private val labelPaint = Paint().apply {
        color = Color.DKGRAY
        textSize = 11f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    
    private val linePaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 1f
        isAntiAlias = true
    }
    
    /**
     * Generates PDF from DamageReportForm data
     */
    fun generatePdf(formData: DamageReportForm): File? {
        return try {
            val pdfDocument = PdfDocument()
            var pageNumber = 1
            
            // Create first page
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            currentY = margin
            
            // Title
            drawText(canvas, "HASAR TESPİT RAPORU", titlePaint, true)
            currentY += 10f
            drawLine(canvas)
            currentY += 20f
            
            // 1. İdari Bilgiler
            if (currentY > pageHeight - 100) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin
            }
            
            drawSectionHeader(canvas, "1. İDARİ BİLGİLER")
            drawField(canvas, "İl", formData.il)
            drawField(canvas, "İlçe", formData.ilce)
            drawField(canvas, "Belde", formData.belde)
            drawField(canvas, "Mahalle", formData.mahalle)
            drawField(canvas, "Köy", formData.koy)
            drawField(canvas, "Mezra", formData.mezra)
            currentY += 10f
            
            // 2. Nüfus ve Hane Bilgileri
            if (currentY > pageHeight - 100) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin
            }
            
            drawSectionHeader(canvas, "2. NÜFUS VE HANE BİLGİLERİ")
            drawField(canvas, "Nüfus", formData.nufus)
            drawField(canvas, "Hane Sayısı", formData.hane)
            currentY += 10f
            
            // 3. Afet Bilgileri
            if (currentY > pageHeight - 100) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin
            }
            
            drawSectionHeader(canvas, "3. AFET BİLGİLERİ")
            drawField(canvas, "Afetin Türü", formData.afetinTuru)
            drawField(canvas, "Afetin Tarihi", formData.afetinTarihi)
            drawField(canvas, "Sayfa No", formData.sayfaNo)
            currentY += 10f
            
            // 4. Yapı Konum Bilgileri
            if (currentY > pageHeight - 150) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin
            }
            
            drawSectionHeader(canvas, "4. YAPI KONUM BİLGİLERİ")
            drawField(canvas, "Cadde/Sokak", formData.caddeSokak)
            drawField(canvas, "Afetzede Soyadı", formData.afetzedesiSoyadi)
            drawField(canvas, "Baba Adı", formData.babaAdi)
            drawField(canvas, "Yapı Adı", formData.yapiAdi)
            drawField(canvas, "TEDAŞ No", formData.tedasNo)
            drawField(canvas, "GPS Koordinat", formData.gpsKoordinat)
            currentY += 10f
            
            // 5. Yapı Özellikleri
            if (currentY > pageHeight - 200) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin
            }
            
            drawSectionHeader(canvas, "5. YAPI ÖZELLİKLERİ")
            drawField(canvas, "Mimari Proje", booleanToString(formData.mimariProje))
            drawField(canvas, "Kaçıncı Kat", formData.kacinciKat)
            drawField(canvas, "Bodrum Kat", booleanToString(formData.bodrum))
            drawField(canvas, "Bodrum 1", booleanToString(formData.bodrum1))
            drawField(canvas, "Zemin Kat", booleanToString(formData.zemin))
            drawField(canvas, "1. Normal Kat", booleanToString(formData.normal1))
            drawField(canvas, "2. Normal Kat", booleanToString(formData.normal2))
            drawField(canvas, "3. Normal Kat", booleanToString(formData.normal3))
            drawField(canvas, "Çatı Katı", booleanToString(formData.catiKati))
            currentY += 10f
            
            // 6. Yapı Sistemi
            if (currentY > pageHeight - 100) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin
            }
            
            drawSectionHeader(canvas, "6. YAPI SİSTEMİ")
            drawField(canvas, "Yapıdaki Sistem", formData.yapidakiSistem.displayName)
            currentY += 10f
            
            // 7. Hasar Durumu
            if (currentY > pageHeight - 100) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin
            }
            
            drawSectionHeader(canvas, "7. HASAR DURUMU")
            drawField(canvas, "Hasar Seviyesi", formData.hasarDurumu.displayName)
            drawField(canvas, "Taşıyıcı Sistem Hasar", formData.tasiyiciSistem.displayName)
            drawField(canvas, "Taşıma Gücü Kaybı", booleanToString(formData.tasimaGucuKaybi))
            currentY += 10f
            
            // 8. Açıklamalar
            if (currentY > pageHeight - 150) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin
            }
            
            drawSectionHeader(canvas, "8. AÇIKLAMALAR")
            drawMultilineField(canvas, formData.aciklamalar)
            currentY += 10f
            
            // 9. İmza Bilgileri
            if (currentY > pageHeight - 150) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin
            }
            
            drawSectionHeader(canvas, "9. İMZA BİLGİLERİ")
            drawField(canvas, "Adı Soyadı 1", formData.adiSoyadi1)
            drawField(canvas, "Mesleği 1", formData.meslegi1)
            drawField(canvas, "Birimi 1", formData.birimi1)
            drawField(canvas, "Adı Soyadı 2", formData.adiSoyadi2)
            drawField(canvas, "Mesleği 2", formData.meslegi2)
            drawField(canvas, "Birimi 2", formData.birimi2)
            drawField(canvas, "Rapor Tarihi", formData.raporTarihi)
            
            // Footer
            currentY = pageHeight - 40f
            drawText(canvas, "Bu rapor ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("tr")).format(Date())} tarihinde otomatik olarak oluşturulmuştur.", 
                normalPaint.apply { textSize = 9f }, true)
            
            pdfDocument.finishPage(page)
            
            // Save PDF
            val fileName = "HasarRaporu_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
            val file = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Android 10+ - Use scoped storage
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            } else {
                // Android 9 and below
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName)
            }
            
            file.parentFile?.mkdirs()
            
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            
            pdfDocument.close()
            file
            
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    private fun drawText(canvas: Canvas, text: String, paint: Paint, centered: Boolean = false) {
        val x = if (centered) {
            (pageWidth - paint.measureText(text)) / 2
        } else {
            margin
        }
        canvas.drawText(text, x, currentY, paint)
        currentY += paint.textSize + 8f
    }
    
    private fun drawSectionHeader(canvas: Canvas, title: String) {
        drawLine(canvas)
        currentY += 5f
        drawText(canvas, title, headerPaint)
        currentY += 5f
    }
    
    private fun drawField(canvas: Canvas, label: String, value: String) {
        if (value.isNotEmpty()) {
            canvas.drawText("$label:", margin, currentY, labelPaint)
            canvas.drawText(value, margin + 150f, currentY, normalPaint)
            currentY += normalPaint.textSize + 10f
        }
    }
    
    private fun drawMultilineField(canvas: Canvas, text: String) {
        if (text.isEmpty()) return
        
        val maxWidth = pageWidth - 2 * margin
        val words = text.split(" ")
        var currentLine = ""
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val testWidth = normalPaint.measureText(testLine)
            
            if (testWidth > maxWidth) {
                if (currentLine.isNotEmpty()) {
                    canvas.drawText(currentLine, margin, currentY, normalPaint)
                    currentY += normalPaint.textSize + 8f
                }
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, margin, currentY, normalPaint)
            currentY += normalPaint.textSize + 8f
        }
    }
    
    private fun drawLine(canvas: Canvas) {
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, linePaint)
        currentY += 5f
    }
    
    private fun booleanToString(value: Boolean?): String {
        return when (value) {
            true -> "Evet"
            false -> "Hayır"
            null -> "Bilinmiyor"
        }
    }
}

