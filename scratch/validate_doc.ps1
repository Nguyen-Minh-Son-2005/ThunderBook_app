$word = New-Object -ComObject Word.Application
$word.Visible = $false
$doc = $word.Documents.Open('D:\HAUI_UNIVEERSITY\N04_UngDungBanSachThunderBook.docx')
Write-Host "Word opened document OK!"
Write-Host "Paragraphs: $($doc.Paragraphs.Count)"
Write-Host "Tables: $($doc.Tables.Count)"
$saveOption = 0
$doc.Close([ref]$saveOption)
$word.Quit()
[System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) | Out-Null
Write-Host "Done validating Word document!"
