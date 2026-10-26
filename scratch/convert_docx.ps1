$word = New-Object -ComObject Word.Application
$word.Visible = $false
$doc = $word.Documents.Open("D:\HAUI_UNIVEERSITY\N04_UngDungBanSachThunderBook.docx")
$doc.SaveAs([ref]"D:\HAUI_UNIVEERSITY\AppMangaFGroup\scratch\report_text.txt", [ref]2)
$doc.Close()
$word.Quit()
[System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) | Out-Null
Write-Host "Done"
